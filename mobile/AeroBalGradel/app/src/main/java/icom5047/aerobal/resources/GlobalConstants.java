package icom5047.aerobal.resources;

import com.aerobal.data.objects.measurementTypes.Drag;
import com.aerobal.data.objects.measurementTypes.Humidity;
import com.aerobal.data.objects.measurementTypes.Lift;
import com.aerobal.data.objects.measurementTypes.MeasurementType;
import com.aerobal.data.objects.measurementTypes.MeasurementTypes;
import com.aerobal.data.objects.measurementTypes.Pressure;
import com.aerobal.data.objects.measurementTypes.Temperature;
import com.aerobal.data.objects.measurementTypes.Tilt;
import com.aerobal.data.objects.measurementTypes.WindDirection;
import com.aerobal.data.objects.measurementTypes.WindSpeed;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import icom5047.aerobal.containers.SpinnerContainer;

public class GlobalConstants {

    public static final int CharacterLimit = 100;

    //Wind Speed: Implied from Default
    public static final double WindSpeedMinimum = 1;
    public static final double WindSpeedMaximum = 50;
    public static final int TimeIntervalMinimum = 100;
    public static final int TimeIntervalMaximum = 10000;
    public static final int SamplesMinimum = 1;
    public static final int SampleMaximum = 10000;
    public static final int DecimalPrecision = 3;



    public static final String[] allMeasurementType = new String[]{ "Average", "Max", "Min", "Standard Deviation", "Median" };

    public static class File{
        public static String folderName = "AeroBal";
        public static String csvExt = ".csv";
        public static String aeroExt = ".aero";
        public static String pathName = "AeroBal";

    }

    public static class Measurements{


        public static final int WindSpeedKey = 0;
        public static final int TemperatureKey = 1;
        public static final int HumidityKey = 2;
        public static final int WindDirectionKey = 3;
        public static final int PressureKey = 4;
        public static final int SideKey = 5;
        public static final int DragKey = 6;
        public static final int LiftKey = 7;
        public static final int TimeKey = 8;




        public static String WindSpeedString = "Wind Speed";
        public static String HumidityString = "Humidity";
        public static String TemperatureString = "Temperature";
        public static String WindDirectionString = "Wind Direction";
        public static String PressureString = "Pressure";
        public static String SideString = "Side";
        public static String DragString = "Drag";
        public static String LiftString = "Lift";
        public static String TimeString = "Time";



        public static  Map<Integer, Integer> measurementToUnitMapping(){

            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            map.put(WindSpeedKey, UnitFactory.Type.SPEED);
            map.put(TemperatureKey, UnitFactory.Type.TEMPERATURE);
            map.put(HumidityKey, UnitFactory.Type.HUMIDITY);
            map.put(WindDirectionKey, UnitFactory.Type.DIRECTION);
            map.put(PressureKey, UnitFactory.Type.PRESSURE);
            map.put(SideKey, UnitFactory.Type.FORCE);
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
            map.put(SideKey, SideString);
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
            list.add(4, new SpinnerContainer(4, TemperatureKey, TemperatureString));
            list.add(5, new SpinnerContainer(5, SideKey, SideString));
            list.add(6, new SpinnerContainer(6, DragKey, DragString));
            list.add(7, new SpinnerContainer(7, LiftKey, LiftString));

            return list;
        }

        public static List<SpinnerContainer> getGraphListSpinner(){
            List<SpinnerContainer> ret = getMeasurementListSpinner();
            ret.add(8, new SpinnerContainer(8, TimeKey, TimeString));
            return ret;
        }


        public static MeasurementType getMessurmentTypeForSpinner(SpinnerContainer container){


            switch (container.index){
                case PressureKey:
                    return MeasurementTypes.getType(Pressure.toString());
                case WindSpeedKey:
                    return MeasurementTypes.getType(WindSpeed.toString());
                case WindDirectionKey:
                    return MeasurementTypes.getType(WindDirection.toString());
                case HumidityKey:
                    return MeasurementTypes.getType(Humidity.toString());
                case TemperatureKey:
                    return  MeasurementTypes.getType(Temperature.toString());
                case SideKey:
                    return MeasurementTypes.getType(Tilt.toString());
                case DragKey:
                    return MeasurementTypes.getType(Drag.toString());
                case LiftKey:
                    return MeasurementTypes.getType(Lift.toString());

            }


            return null;

        }




    }



}
