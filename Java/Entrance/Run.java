package Entrance;

import Personal_Packages.*;

public class Run{
    public static void main(String[] args) {
        // use default constructor and then set set name and age in run
        Human tupi = new Human();
        tupi.name = "Tupi";
        tupi.age = 32;
        System.out.println(tupi.name + "'s age: " + tupi.age + ", current total is: " + Human.count);
        
        // use constructor with parameters to set name and age
        Human eric = new Human("Eric", 32);
        System.out.println(eric.name + "'s age: " + eric.age + ", current total is: " + Human.count);
        
        // use given method to set height and weight then print by get method
        eric.setHeightAndWeight(168, 55);
        System.out.println(eric.getHeightAndWeight());
    }
}