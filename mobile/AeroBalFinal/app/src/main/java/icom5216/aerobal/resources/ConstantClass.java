package icom5216.aerobal.resources;

public class ConstantClass {
	
	
	//Connection Dialog
	public static String connectDialogTag = "connectDialogTag";
	
	//Constants
	public static String PRESSURE_KEY = "pressure";
	public static String DIRECTION_KEY = "direction";
	public static String WINDSPEED_KEY = "windspeed";
	public static String TEMP_KEY = "temperature";
	public static String HUMIDITY_KEY = "humidity";
	
	public static String DRAG_KEY = "drag";
	public static String LIFT_KEY = "lift";
	public static String TITL_KEY = "tilt";
	
	public static String INIT_DRAG_KEY = "initdrag";
	public static String INIT_LIFT_KEY = "initlift";
	public static String INIT_TILT_KEY = "inittilt";
	
	//Btns Constants
	public static final String MESSUREMENT_STRING_KEY = "messurementStringKey";
	public static final String INIT_STRING_KEY = "initStringKey";

	
	public static class BTCommands{
		public static String ON_CMD = "bt:power=on\r\n";
		public static String OFF_CMD = "bt:power=off\r\n";
		public static String MEASURE_CMD = "bt:measure\r\n";
	}
	
	
}
