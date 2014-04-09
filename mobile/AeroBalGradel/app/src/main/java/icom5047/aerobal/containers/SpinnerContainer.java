package icom5047.aerobal.containers;

import java.io.Serializable;

/**
 * Created by enrique on 3/26/14.
 */
public class SpinnerContainer implements Serializable {

    public int spinnerIndex;
    public int index;
    public String name;

    public SpinnerContainer(int spinnerIndex, int index, String name){

        this.spinnerIndex = spinnerIndex;
        this.index = index;
        this.name = name;

    }

    @Override
    public String toString() {
        return name;
    }
}

