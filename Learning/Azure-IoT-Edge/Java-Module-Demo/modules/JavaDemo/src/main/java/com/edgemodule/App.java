package com.edgemodule;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

//#region Demo: import JSON and Device Twin packages

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

//#region IoTC: test for connecting with IoTC app via lucadruda Azure IoTCentral Device Client

import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.callbacks.IoTCCallback;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;

//#endregion

public class App {
    private static MessageCallbackMqtt msgCallback = new MessageCallbackMqtt();
    private static EventCallback eventCallback = new EventCallback();
    private static final String INPUT_NAME = "input1";
    private static final String OUTPUT_NAME = "output1";
    
    //#region Demo: set initial threshold

    private static final String TEMP_THRESHOLD = "TemperatureThreshold";
    private static AtomicLong tempThreshold = new AtomicLong(25);

    //#endregion

    //#region IoTC: set IoTC device connection information
    
    private static final IOTC_CONNECT IoTC_AuthType = IOTC_CONNECT.DEVICE_KEY;
    private static String IoTC_ScopeId = "0ne00132CE0";
    private static String IoTC_DeviceId = "09072020001";
    private static String IoTC_SASToken = "X+MN5vdE5OiLFIBDY5+wgKwACN5rIRO4JlRtHdRyLI4=";
    private static IoTCClient IoTCDeviceClient = new IoTCClient(IoTC_DeviceId, IoTC_ScopeId, IoTC_AuthType, IoTC_SASToken);

    // #endregion

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

    // the main class to react the inputing message
    protected static class MessageCallbackMqtt implements MessageCallback {
        private int counter = 0;

        @Override
        public IotHubMessageResult execute(Message msg, Object context) {
            this.counter += 1;

            String msgString = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
            System.out.println(String.format("Received message %d: %s", this.counter, msgString));

            if (context instanceof ModuleClient) {
                
                try (JsonReader jsonReader = Json.createReader(new StringReader(msgString))) {
                    final JsonObject msgObject = jsonReader.readObject();
                    double temperature = msgObject.getJsonObject("machine").getJsonNumber("temperature").doubleValue();
                    long threshold = App.tempThreshold.get();
                    
                    if (temperature >= threshold) {
                        //#region Demo: send event message via sendEventAsync to IoT Hub

                        ModuleClient client = (ModuleClient) context;
                        System.out.println(String.format("Machine temperature exceed threshold %d. Sending message to IoT Hub", threshold));

                        // client.sendEventAsync(msg, eventCallback, msg, App.OUTPUT_NAME);
                        Message newMsg = new Message(msgString);
                        newMsg.setContentEncoding("UTF-8");
                        newMsg.setContentTypeFinal("application/json");
                        newMsg.setProperty("routeTag", "Temperature-Message");
                        newMsg.setProperty("moduleVer", "1.3.2");
                        newMsg.setProperty("devEnv", "Windows10");
                        // newMsg.setProperty("messageTitle", String.format("Temperature Alert: exceed %d degree", threshold)); // need to encode invalid json character
                        newMsg.setProperty("messageNo", Integer.toString(this.counter));
                        newMsg.setProperty("machineTemperature", Double.toString(temperature));
                        client.sendEventAsync(newMsg, eventCallback, msg, App.OUTPUT_NAME);
                        
                        //#endregion
                        
                        // Can do something else e.g. sending message to ThingWorx via rest api
                        //#region IoTC: send telemetry to IoTC device

                        System.out.println(String.format("Send exceeded telemetry to IoTC device: %s", IoTC_DeviceId));
                        IoTCCallback callback = new IoTCCallback() {
                            @Override
                            public void Exec(Object result) {
                                System.out.println("Send telemetry with status:" + result);
                            }
                        };
                        IoTCDeviceClient.SendTelemetry(msgString, callback);
                        
                        //#endregion
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class ConnectionStatusChangeCallback implements IotHubConnectionStatusChangeCallback {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason,
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

    // #region Demo: define callback when a device twin property is changed

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

    // #endregion

    public static void main(String[] args) {
        try {
            IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
            System.out.println("Start to create client with MQTT protocol");
            ModuleClient client = ModuleClient.createFromEnvironment(protocol);
            System.out.println("Client created");
            client.setMessageCallback(App.INPUT_NAME, msgCallback, client);
            client.registerConnectionStatusChangeCallback(new ConnectionStatusChangeCallback(), null);
            client.open();

            // #region Demo: register the device twin event

            client.startTwin(new DeviceTwinStatusCallBack(), null, new OnProperty(), null);
            Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange = new HashMap<Property, Pair<TwinPropertyCallBack, Object>>() {
                {
                    put(new Property(App.TEMP_THRESHOLD, null),
                            new Pair<TwinPropertyCallBack, Object>(new OnProperty(), null));
                }
            };
            client.subscribeToTwinDesiredProperties(onDesiredPropertyChange);
            client.getTwin();

            // #endregion

            // #region IoTC: connect with IoTC device client

            IoTCDeviceClient.Connect();

            //#endregion

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
