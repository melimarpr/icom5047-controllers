package icom5047.aerobal.http;

/**
 * Created by enrique on 4/24/14.
 */
public class Server {

    public static String url = "http://162.243.4.162:9000/";


    public static class User{
        public static final String POST_LOGIN = url+"auth";
        public static final String POST_VALIDATE_USER = url+"user";
        public static final String GET_FORGOT_PASSWORD = url+"forgot";
    }



}
