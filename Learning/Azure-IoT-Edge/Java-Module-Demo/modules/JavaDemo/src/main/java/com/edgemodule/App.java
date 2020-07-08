package com.edgemodule;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

//#region add: import extra packages

import java.io.StringReader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;

//#endregion

public class App {
    private static MessageCallbackMqtt msgCallback = new MessageCallbackMqtt();
    private static EventCallback eventCallback = new EventCallback();
    private static final String INPUT_NAME = "input1";
    private static final String OUTPUT_NAME = "output1";
    
    //#region add: set initial threshold

    private static final String TEMP_THRESHOLD = "TemperatureThreshold";
    private static AtomicLong tempThreshold = new AtomicLong(25);

    //#endregion

    protected static class EventCallback implements IotHubEventCallback {
        @Override
        public void execute(IotHubStatusCode status, Object context) {
            if (context instanceof Message) {
                System.out.println("Send message with status: " + status.name());
            } else {
                System.out.println("Invalid context passed");
            }
        }
    }

    protected static class MessageCallbackMqtt implements MessageCallback {
        private int counter = 0;

        @Override
        public IotHubMessageResult execute(Message msg, Object context) {
            this.counter += 1;
            
            String msgString = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
            System.out.println(String.format("Received message %d: %s", this.counter, msgString));
            
            if (context instanceof ModuleClient) {
                //#region add: deserialize the received json message and filter it, then modify the response message and send it 
                //        (can also do something after client.sendEventAsync)
                
                try (JsonReader jsonReader = Json.createReader(new StringReader(msgString))) {
                    final JsonObject msgObject = jsonReader.readObject();
                    double temperature = msgObject.getJsonObject("machine").getJsonNumber("temperature").doubleValue();
                    long threshold = App.tempThreshold.get();
                    
                    if (temperature >= threshold) { //use threshold to decide whether trigger message update procedure
                        ModuleClient client = (ModuleClient) context;
                        System.out.println(String.format("Temperature above threshold %d. Sending message: %s", threshold, msgString));
                        
                        //client.sendEventAsync(msg, eventCallback, msg, App.OUTPUT_NAME);
                        
                        // create customerized message and send it into IoT Hub
                        Message newMsg = new Message(msgString);
                        newMsg.setContentEncoding("UTF-8");
                        newMsg.setContentTypeFinal("application/json");
                        
                        newMsg.setProperty("routeTag", "Temperature-Message");
                        newMsg.setProperty("version", "1.2.8");
                        newMsg.setProperty("environment", "Windows10");
                        
                        // newMsg.setProperty("messageTitle", String.format("Temperature\tAlert\tover\t%d\tdegree", threshold)); // need to be encode to escape invalid character
                        newMsg.setProperty("messageNo", Integer.toString(this.counter));
                        newMsg.setProperty("machineTemperature", Double.toString(temperature));
                        client.sendEventAsync(newMsg, eventCallback, msg, App.OUTPUT_NAME);
                        System.out.println("A new json message has been sent to the cloud storage.");
                        // System.out.println(String.format("The message's Content Type is %s, outeTag is %s.", newMsg.getContentType(), newMsg.getProperty("routeTag")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //#endregion
            }

            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class ConnectionStatusChangeCallback implements IotHubConnectionStatusChangeCallback {
        @Override
        public void execute(IotHubConnectionStatus status,
                            IotHubConnectionStatusChangeReason statusChangeReason,
                            Throwable throwable, Object callbackContext) {
            String statusStr = "Connection Status: %s";
            switch (status) {
                case CONNECTED:
                    System.out.println(String.format(statusStr, "Connected"));
                    break;
                case DISCONNECTED:
                    System.out.println(String.format(statusStr, "Disconnected"));
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    System.exit(1);
                    break;
                case DISCONNECTED_RETRYING:
                    System.out.println(String.format(statusStr, "Retrying"));
                    break;
                default:
                    break;
            }
        }
    }

    //#region add: define callback when changing the device twin property from vscode is fired

    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback {
        @Override
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to device twin operation with status " + status.name());
        }
    }
    
    protected static class OnProperty implements TwinPropertyCallBack {
        @Override
        public void TwinPropertyCallBack(Property property, Object context) {
            if (!property.getIsReported()) {
                if (property.getKey().equals(App.TEMP_THRESHOLD)) {
                    try {
                        long threshold = Math.round((double) property.getValue());
                        App.tempThreshold.set(threshold);
                    } catch (Exception e) {
                        System.out.println("Faile to set TemperatureThread with exception");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    //#endregion

    public static void main(String[] args) {
        try {
            IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
            System.out.println("Start to create client with MQTT protocol");
            ModuleClient client = ModuleClient.createFromEnvironment(protocol);
            System.out.println("Client created");
            client.setMessageCallback(App.INPUT_NAME, msgCallback, client);
            client.registerConnectionStatusChangeCallback(new ConnectionStatusChangeCallback(), null);
            client.open();

            //#region add: register the device twin event

            client.startTwin(new DeviceTwinStatusCallBack(), null, new OnProperty(), null);
            Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange = new HashMap<Property, Pair<TwinPropertyCallBack, Object>>() {
                {
                    put(new Property(App.TEMP_THRESHOLD, null), new Pair<TwinPropertyCallBack, Object>(new OnProperty(), null));
                }
            };
            client.subscribeToTwinDesiredProperties(onDesiredPropertyChange);
            client.getTwin();
            
            //#endregion

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
