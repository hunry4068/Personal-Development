package Personal_Packages;

//external class
public class Human{
    private int height = -1;
    private int weight = -1;

    public static int count = 0;
    public String name;
    public int age = -1;

    //#region constructor
    
    public Human(){
        count += 1;

        age = -1;
        name = "undefined";
    }
    public Human(String str){
        this(); // call itselve constructor
        name = str;
    }

    public Human(String str, int num){
        this(str);
        this.age = num;
    }

    //#endregion

    public void setHeightAndWeight(int h, int w){
        height = h;
        weight = w;
    }

    public String getHeightAndWeight(){
        // return name + "'s height: " + height + ", weight: " + weight;
        return String.format("%1$s's height: %2$s, weight: %3$s", name, height, weight);
    }
}
