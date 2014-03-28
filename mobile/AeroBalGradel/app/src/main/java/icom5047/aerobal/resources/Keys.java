package icom5047.aerobal.resources;

public class Keys {

    //Class to Store all the Shared Preferences Key
    public static class SharedPref {

        public static String UserSharedPreferences = "userSharedPref";
        public static String LoginStatus = "loginStatus";


    }

    public static class CallbackMap {
        public static String BluetoothDevice = "btDevice";
        public static String BluetoothDialog = "btDialog";
        public static String BluetoothConnectedStatus = "btConneectedStatus";
        public static String UnitsDialog = "unitsDialog";
        public static String UnitsBtnType = "unitBtnType";
        public static String UnitsDefault = "unitsDefault";
        public static String UnitsSaveNew = "unitsSave";
        public static String UnitsPressure = "unitsPressure";
        public static String UnitsForce = "unitsForce";
        public static String UnitsHumidity = "unitsHumidity";
        public static String UnitsTemperature = "unitsTemperature";
        public static String UnitsSpeed = "unitsSpeed";
        public static String UnitsDirection = "unitsDirection";
        public static String NewExperimentDialog = "newExperimentDialog";
        public static String NewExperimentObject = "newExperimentObject";

    }


    public static class FragmentTag {
        public static String EmptyTag = "emptyFragmentTag";
        public static String ExperimentTag = "experimentFragmentTag";
    }

    public static class BundleKeys{

        public static String ExperimentController = "keyExperimentController";
        public static String UnitController = "keyUnitController";


    }


}