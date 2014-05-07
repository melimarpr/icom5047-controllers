package icom5047.aerobal.containers;

/**
 * Created by enrique on 4/9/14.
 */
public class RawContainer {

    public long nanoseconds;
    public double value;

    public RawContainer(long seconds, double value){

        this.nanoseconds = seconds;
        this.value = value;

    }

    @Override
    public String toString() {
        return value+"";
    }
}
