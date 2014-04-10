package icom5047.aerobal.mockers;

import com.aerobal.data.objects.Session;

import java.util.ArrayList;
import java.util.List;

import icom5047.aerobal.containers.RawContainer;

/**
 * Created by enrique on 4/9/14.
 */
public class Mocker {

    public static RawContainer[] generateRawContainer(int n){

        RawContainer[] result = new RawContainer[n];
        for(int i=0; i<n; i++){
            result[i] = new RawContainer(i, 0);
        }

        return result;
    }


    public static List<Session> generateFakeSessions(int n) {

        ArrayList<Session> ret = new ArrayList<Session>();

        for(int i=0; i<n; i++){

            ret.add(new Session(null, "Test"+i, "Description", true));


        }

        return ret;

    }


    public static double[] generateLinearDoubleArray(int n){
        double[] ret = new double[n];

        for(int i=0; i<n; i++){
            ret[i] = i+1;
        }
        return ret;


    }

}
