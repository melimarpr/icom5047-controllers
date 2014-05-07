package icom5047.aerobal.containers;

/**
 * Created by enrique on 4/9/14.
 */
public class SummaryContainer {

    public String type;
    public double value;

    public SummaryContainer(String type, double value){

        this.type = type;
        this.value = value;

    }

    @Override
    public String toString() {
        return type;
    }
}
