package icom5047.aerobal.resources;

/**
 * Created by enrique on 5/3/14.
 */
public class BTResponseCodes {

    public static final String fanOff = "fan:off";
    public static final String fanOn = "fan:on";
    public static final String ack = "ack";
    public static final String err = "err";

    //Load Cell
    public static final String loadFront = "lcf";
    public static final String loadBack = "lcb";
    public static final String loadUp = "lcu";
    public static final String loadDown = "lcd";
    public static final String loadLeft = "lcl";
    public static final String loadRight = "lcr";

    //WS


    //Measurements
    public static final String humidity = "hm";
    public static final String temperature = "tm0";
    public static final String windSpeed = "spda";
    public static final String windSpeedSet = "ws";
    public static final String pressure0 = "ps0";
    public static final String pressure1 = "ps1";
    public static final String pressure2 = "ps2";
    public static final String pressure3 = "ps3";
    public static final String pressure4 = "ps4";
    public static final String pressure5 = "ps5";
    public static final String pressure6 = "ps6";
    public static final String pressure7 = "ps7";
    public static final String windDirection = "wd";



    public static boolean validTimerCommand(String prefix){
        return validForce(prefix) ||prefix.equals(humidity) || prefix.equals(temperature) || prefix.equals(windSpeed) || prefix.equals(windDirection) ||

               prefix.equals(pressure1)  || prefix.equals(pressure2) || prefix.equals(pressure3) || prefix.equals(pressure4) || prefix.equals(pressure5) ||

               prefix.equals(pressure6) || prefix.equals(pressure7) || prefix.equals(pressure0);

    }





    public static String[] splitCommand(String command){
        return command.split(":");
    }

    public static  boolean validForce(String prefix){
        return prefix.equals(loadFront) || prefix.equals(loadBack) || prefix.equals(loadUp) || prefix.equals(loadDown) || prefix.equals(loadLeft) || prefix.equals(loadRight);
    }




}
