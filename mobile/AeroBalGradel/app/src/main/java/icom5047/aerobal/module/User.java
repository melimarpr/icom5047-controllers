package icom5047.aerobal.module;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by enrique on 4/24/14.
 */
public class User {

    private String email;
    private String name;
    private String token;



    public User(String email, String name, String token) {
        this.email = email;
        this.name = name;
        this.token = token;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put("email", email);
            jsonObject.put("name", name);
            jsonObject.put("token", token);
        }catch (JSONException e){}

        return jsonObject.toString();
    }
}
