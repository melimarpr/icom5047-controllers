package icom5047.aerobal.containers;

/**
 * Created by enrique on 5/14/14.
 */
public class ValueContainer {

    public String title;
    public double value;
    public String unit;
    public boolean hasUnit;
    public boolean isString;
    public String strValue;


    public ValueContainer(String title, double value, boolean isInteger, boolean hasUnit, String unit){
        this.title = title;
        this.value = value;
        this.isString = isInteger;
        this.hasUnit = hasUnit;
        this.unit = unit;
    }

    public ValueContainer(String title, String value, boolean isInteger, boolean hasUnit, String unit){
        this.title = title;
        this.strValue = value;
        this.isString = isInteger;
        this.hasUnit = hasUnit;
        this.unit = unit;
    }

}