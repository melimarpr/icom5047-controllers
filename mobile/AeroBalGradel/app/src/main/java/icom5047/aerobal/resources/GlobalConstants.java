package icom5047.aerobal.resources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import icom5047.aerobal.containers.SpinnerContainer;

public class GlobalConstants {

    public static final int CharacterLimit = 100;

    public static final double WindSpeedMinimumKMPH = 25.0;
    public static final double WindSpeedMaximumKMPH = 75.0;
    public static final int TimeIntervalMinimum = 100;
    public static final int TimeIntervalMaximum = 10000;
    public static final int SamplesMinimum = 1;
    public static final int SampleMaximum = 10000;
    public static final int DecimalPrecision = 3;



    public static final String[] allMeasurementType = new String[]{ "Average", "Max", "Min", "Standard Deviation", "Median" };

    public static class File{
        public static String folderName = "Aerobal";

    }

    public static class Measurements{


        public static int WindSpeedKey = 0;
        public static int TemperatureKey = 1;
        public static int HumidityKey = 2;
        public static int WindDirectionKey = 3;
        public static int PressureKey = 4;
        public static int TiltKey = 5;
        public static int DragKey = 6;
        public static int LiftKey = 7;




        public static String WindSpeedString = "Wind Speed";
        public static String HumidityString = "Humidity";
        public static String TemperatureString = "Temperature";
        public static String WindDirectionString = "Wind Direction";
        public static String PressureString = "Pressure";
        public static String TiltString = "Tilt";
        public static String DragString = "Drag";
        public static String LiftString = "Lift";



        public static  Map<Integer, Integer> measurementToUnitMapping(){

            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            map.put(WindSpeedKey, UnitFactory.Type.SPEED);
            map.put(TemperatureKey, UnitFactory.Type.TEMPERATURE);
            map.put(HumidityKey, UnitFactory.Type.HUMIDITY);
            map.put(WindDirectionKey, UnitFactory.Type.DIRECTION);
            map.put(PressureKey, UnitFactory.Type.PRESSURE);
            map.put(TiltKey, UnitFactory.Type.FORCE);
            map.put(DragKey, UnitFactory.Type.FORCE);
            map.put(LiftKey, UnitFactory.Type.FORCE);

            return  map;
        }

        public static  Map<Integer, String> measurementToStringMapping(){

            Map<Integer, String> map = new HashMap<Integer, String>();
            map.put(WindSpeedKey, WindSpeedString);
            map.put(TemperatureKey, TemperatureString);
            map.put(HumidityKey, HumidityString);
            map.put(WindDirectionKey, WindDirectionString);
            map.put(PressureKey, PressureString);
            map.put(TiltKey, TiltString);
            map.put(DragKey, DragString);
            map.put(LiftKey, LiftString);

            return  map;
        }


        public static List<SpinnerContainer> getMeasurementListSpinner(){
            LinkedList<SpinnerContainer> list = new LinkedList<SpinnerContainer>();

            list.add(0, new SpinnerContainer(0,PressureKey, PressureString));
            list.add(1, new SpinnerContainer(1, WindSpeedKey, WindSpeedString));
            list.add(2, new SpinnerContainer(2, WindDirectionKey, WindDirectionString));
            list.add(3, new SpinnerContainer(3, HumidityKey, HumidityString));
            list.add(4, new SpinnerContainer(4, WindDirectionKey, WindDirectionString));
            list.add(5, new SpinnerContainer(5, TemperatureKey, TemperatureString));
            list.add(6, new SpinnerContainer(6, TiltKey, TiltString));
            list.add(7, new SpinnerContainer(7, DragKey, DragString));
            list.add(8, new SpinnerContainer(8, LiftKey, LiftString));

            return list;
        }




    }



}
