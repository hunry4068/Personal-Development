using System;
// using System.IO;

namespace C_
{
    class Program
    {
        static void Main(string[] args)
        {
            // Console.WriteLine("Hello, World!");
            
            System.Console.WriteLine("\nTest Polymorphism:");
            Animal animal = new Cat();
            animal.who_am_i();
            animal.attact();

            animal = new Kitten();
            animal.who_am_i();
            animal.run();

            // Console.WriteLine("What is your name?");
            // var name = Console.ReadLine();
            // var currentDate = DateTime.Now;
            // Console.WriteLine($"{Environment.NewLine}Hello, {name}, on {currentDate:d} at {currentDate:t}!");
            // Console.Write($"{Environment.NewLine}Press any key to exit...");
            // Console.ReadKey(true);

            System.Console.WriteLine("\nTest Dependency Injection:\n");
            System.Console.WriteLine("使用吹風機插頭");
            IElectricalPlug hairDryerPlug = new HairDryerPlug();
            var socket = new Socket(hairDryerPlug);
            socket.SendPower();

            System.Console.WriteLine("使用手機插頭");
            IElectricalPlug mobilePhonePlug = new MobilePhonePlug();
            socket = new Socket(mobilePhonePlug);
            socket.SendPower();
        }
    }
}

# region Test Polymorphism 

// abstract class = thing template (can inherit class and interfaces)
// interface = thing shape (can only inherit interface)

interface IWildLife 
{
    void attact();
    void run();
}

// abstract class cannot be directly implemented, can only be inherited and implemented by the child class (Cat or Kitten)
public abstract class Animal : IWildLife
{
    public abstract void attact(); // implement inteface method
    public abstract void run(); // implement inteface method

    public abstract void who_am_i(); // abstract member
}

// must to implement the inherited abstract member
public class Cat : Animal  
{
    // clas constructor
    public Cat() 
    {
        Console.WriteLine("");
    }

    public override void attact() 
    {
        Console.WriteLine("Cat straches you!");
    }
    public override void run() 
    {
        Console.WriteLine("Cat runs away!");
    }

    public override void who_am_i() 
    {
        Console.WriteLine("I'm a Cat");
    }
}

// if doesn't overrde the who_am_i() then will apply the definition of Cat
public class Kitten : Cat
{
    // clas constructor
    public Kitten() 
    {
        Console.WriteLine("");
        Console.WriteLine("");
    }
    
    public override void who_am_i() 
    {
        Console.WriteLine("I'm a Kitten");
    }
}

# endregion

# region Test Dependency Injection

public interface IElectricalPlug
{
    void Connect();
}

public class Socket
{
    private readonly IElectricalPlug _plug;

    // 建構子注入
    public Socket(IElectricalPlug plug)
    {
        if (plug == null)
            throw new ArgumentNullException("plug 為空值");

        this._plug = plug;
    }

    public void SendPower()
    {
        this._plug.Connect();
    }
}

public class HairDryerPlug : IElectricalPlug
{
    public void Connect()
    {
        Console.WriteLine("HairDryerPlug connected!\n");
    }
}

public class MobilePhonePlug : IElectricalPlug
{
    public void Connect()
    {
        Console.WriteLine("Checking power level...");
        Console.WriteLine("MobilePhonePlug connected!\n");
    }
}

#endregion

