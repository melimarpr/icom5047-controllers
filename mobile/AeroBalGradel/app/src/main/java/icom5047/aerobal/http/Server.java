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

    public static class Session{
        public static final String GET_USER_SESSION = url+"sessions";
    }

    public static class Experiments{
        public static final String GET_EXPERIMENTS_FOR_SESSION = url+"experiments";

        public static class Parmas{
            public static final String SESSION_ID = "sessionId";
        }
    }
    public static class Headers{
        public static final String TOKEN = "token";

    }


    public static class ErrorStrings{

        public static final String USER_NOT_FOUND = "User not found.";
        public static final String PASSWORD_INVALID = "Invalid password for user.";
    }


}
