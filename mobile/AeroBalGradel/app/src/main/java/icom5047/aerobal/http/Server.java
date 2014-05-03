package icom5047.aerobal.http;

/**
 * Created by enrique on 4/24/14.
 */
public class Server {

    public static String url = "http://162.243.4.162:9000/";


    public static class User{
        public static final String POST_LOGIN = url+"auth";
        public static final String POST_VALIDATE_USER = url+"user";
        public static final String GET_FORGOT_PASSWORD = url+"forgot_password";
    }

    public static class Session{
        public static final String GET_USER_SESSION = url+"sessions";
        public static final String POST_NEW_SESSION = url+"new_session";
    }

    public static class Experiments{
        public static final String GET_EXPERIMENTS_FOR_SESSION = url+"experiments";
        public static final String GET_EXPERIMENT_COMPLETE = url+"full_experiment";
        public static final String POST_COMPLETE_EXPERIMENT = url+"submit_experiment";
        public static final String PUT_UPDATE_EXPERIMENT_NAME = url+"update_experiment";

        public static class Parmas{
            public static final String SESSION_ID = "sessionId";
            public static final String EXPERIMENT_ID = "experimentId";

        }
    }

    public static class Params{
        public static final String ID = "id";
    }
    public static class Headers{
        public static final String TOKEN = "token";
        public static final String SESSION_ID = "sessionId";

    }


    public static class ErrorStrings{

        public static final String USER_NOT_FOUND = "User not found.";
        public static final String PASSWORD_INVALID = "Invalid password for user.";
    }


}
