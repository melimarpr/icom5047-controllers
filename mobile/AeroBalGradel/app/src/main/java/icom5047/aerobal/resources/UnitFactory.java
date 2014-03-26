package icom5047.aerobal.resources;

import icom5047.aerobal.exceptions.InvalidUnitException;

import java.util.HashMap;
import java.util.Map;

public class UnitFactory {

    //Type Identifier
    public static class Type {
        public static final int PRESSURE = 0;
        public static final int HUMIDITY = 1;
        public static final int TEMPERATURE = 2;
        public static final int DIRECTION = 3;
        public static final int SPEED = 4;
        public static final int FORCE = 5;

    }

    public static class Pressure {
        public static final int UNIT_PASCAL = 6;
        public static final int UNIT_PSI = 7;

        public static String getUnitString(int unit) throws InvalidUnitException {
            switch (unit) {
                case UnitFactory.Pressure.UNIT_PSI:
                    return "psi";

                case UNIT_PASCAL:
                    return "Pa";
                default:
                    throw new InvalidUnitException("Enter Unit is Invalid");
            }
        }

        public static Map<Integer, String> getAllUnits() {
            Map<Integer, String> map = new HashMap<Integer, String>();

            try {
                map.put(UNIT_PASCAL, getUnitString(UNIT_PASCAL));
                map.put(UNIT_PSI, getUnitString(UNIT_PSI));
            } catch (InvalidUnitException e) {
                //Always Valid
                e.printStackTrace();
            }
            return map;
        }

        public static double convert(double value, int from, int to) {
            return value;
        }

    }

    public static class Humidity {
        public static final int UNIT_PERCENTAGE = 8;

        public static String getUnitString(int unit) throws InvalidUnitException {
            switch (unit) {
                case UnitFactory.Humidity.UNIT_PERCENTAGE:
                    return "%";
                default:
                    throw new InvalidUnitException("Enter Unit is Invalid");
            }
        }

        public static Map<Integer, String> getAllUnits() {
            Map<Integer, String> map = new HashMap<Integer, String>();

            try {
                map.put(UNIT_PERCENTAGE, getUnitString(UNIT_PERCENTAGE));
            } catch (InvalidUnitException e) {
                //Always Valid
                e.printStackTrace();
            }
            return map;
        }

    }

    public static class Temperature {
        public static final int UNIT_CELSIUS = 9;
        public static final int UNIT_KELVIN = 10;
        public static final int UNIT_FAHRENHEIT = 11;

        public static String getUnitString(int unit) throws InvalidUnitException {
            switch (unit) {
                case UnitFactory.Temperature.UNIT_CELSIUS:
                    return "°C";
                case UNIT_FAHRENHEIT:
                    return "°F";
                case UNIT_KELVIN:
                    return "K";
                default:
                    throw new InvalidUnitException("Enter Unit is Invalid");
            }
        }

        public static Map<Integer, String> getAllUnits() {
            Map<Integer, String> map = new HashMap<Integer, String>();
            try {
                map.put(UNIT_CELSIUS, getUnitString(UNIT_CELSIUS));
                map.put(UNIT_FAHRENHEIT, getUnitString(UNIT_FAHRENHEIT));
                map.put(UNIT_KELVIN, getUnitString(UNIT_KELVIN));
            } catch (InvalidUnitException e) {
                //Always Valid
                e.printStackTrace();
            }
            return map;
        }

        public static double convert(double value, int from, int to) {

            switch (from) {
                case UNIT_CELSIUS:
                    switch (to) {
                        case UNIT_FAHRENHEIT:
                            return value * 1.8 + 32.0;
                        case UNIT_KELVIN:
                            return value + 273.15;
                    }
                    break;
                case UNIT_FAHRENHEIT:
                    switch (to) {
                        case UNIT_CELSIUS:
                            return (value - 32.0) / 1.8;
                        case UNIT_KELVIN:
                            return (value + 459.67) / 1.8;

                    }
                    break;

                case UNIT_KELVIN:
                    switch (to) {
                        case UNIT_FAHRENHEIT:
                            return value * 1.8 - 459.67;
                        case UNIT_CELSIUS:
                            return value - 273.15;

                    }
                    break;

            }


            return value;
        }
    }

    public static class Speed {
        public static final int UNIT_KMPH = 12;
        public static final int UNIT_MPH = 13;
        public static final int UNIT_MS = 14;

        public static String getUnitString(int unit) throws InvalidUnitException {
            switch (unit) {
                case UNIT_KMPH:
                    return "km/h";

                case UNIT_MPH:
                    return "mph";
                case UNIT_MS:
                    return "m/s";
                default:
                    throw new InvalidUnitException("Enter Unit is Invalid");
            }
        }

        public static Map<Integer, String> getAllUnits() {
            Map<Integer, String> map = new HashMap<Integer, String>();

            try {
                map.put(UNIT_KMPH, getUnitString(UNIT_KMPH));
                map.put(UNIT_MPH, getUnitString(UNIT_MPH));
                map.put(UNIT_MS, getUnitString(UNIT_MS));
            } catch (InvalidUnitException e) {
                //Always Valid
                e.printStackTrace();
            }
            return map;
        }

        public static double convert(double value, int from, int to) {
            return value;
        }
    }

    public static class Direction {
        public static final int UNIT_DEGREES = 15;

        public static String getUnitString(int unit) throws InvalidUnitException {
            switch (unit) {
                case UNIT_DEGREES:
                    return "θ";
                default:
                    throw new InvalidUnitException("Enter Unit is Invalid");
            }
        }

        public static Map<Integer, String> getAllUnits() {
            Map<Integer, String> map = new HashMap<Integer, String>();

            try {
                map.put(UNIT_DEGREES, getUnitString(UNIT_DEGREES));
            } catch (InvalidUnitException e) {
                //Always Valid
                e.printStackTrace();
            }
            return map;
        }

    }

    public static class Force {
        public static final int UNIT_POUNDS = 16;
        public static final int UNIT_NEWTON = 17;

        public static String getUnitString(int unit) throws InvalidUnitException {
            switch (unit) {
                case UNIT_POUNDS:
                    return "Lbs";

                case UNIT_NEWTON:
                    return "N";
                default:
                    throw new InvalidUnitException("Enter Unit is Invalid");
            }
        }

        public static Map<Integer, String> getAllUnits() {
            Map<Integer, String> map = new HashMap<Integer, String>();

            try {
                map.put(UNIT_POUNDS, getUnitString(UNIT_POUNDS));
                map.put(UNIT_NEWTON, getUnitString(UNIT_NEWTON));
            } catch (InvalidUnitException e) {
                //Always Valid
                e.printStackTrace();
            }
            return map;
        }

        public static double convert(double value, int from, int to) {
            return value;
        }

    }

    public static Map<Integer, String> getAllUnits() {
        Map<Integer, String> map = new HashMap<Integer, String>();

        try {
            map.put(Force.UNIT_POUNDS, Force.getUnitString(Force.UNIT_POUNDS));
            map.put(Force.UNIT_NEWTON, Force.getUnitString(Force.UNIT_NEWTON));
            map.put(Direction.UNIT_DEGREES, Direction.getUnitString(Direction.UNIT_DEGREES));
            map.put(Speed.UNIT_KMPH, Speed.getUnitString(Speed.UNIT_KMPH));
            map.put(Speed.UNIT_MPH, Speed.getUnitString(Speed.UNIT_MPH));
            map.put(Speed.UNIT_MS, Speed.getUnitString(Speed.UNIT_MS));
            map.put(Temperature.UNIT_CELSIUS, Temperature.getUnitString(Temperature.UNIT_CELSIUS));
            map.put(Temperature.UNIT_FAHRENHEIT, Temperature.getUnitString(Temperature.UNIT_FAHRENHEIT));
            map.put(Temperature.UNIT_KELVIN, Temperature.getUnitString(Temperature.UNIT_KELVIN));
            map.put(Humidity.UNIT_PERCENTAGE, Humidity.getUnitString(Humidity.UNIT_PERCENTAGE));
            map.put(Pressure.UNIT_PASCAL, Pressure.getUnitString(Pressure.UNIT_PASCAL));
            map.put(Pressure.UNIT_PSI, Pressure.getUnitString(Pressure.UNIT_PSI));


        } catch (InvalidUnitException e) {
            //Always Valid
            e.printStackTrace();
        }
        return map;
    }

}
