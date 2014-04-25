package icom5047.aerobal.fragments;

import android.app.Fragment;

import icom5047.aerobal.controllers.UserController;

/**
 * Created by enrique on 4/25/14.
 */
public class OpenOnlineFragment extends Fragment {

    private volatile UserController userController;


    public static OpenOnlineFragment getInstance(UserController userController){
        OpenOnlineFragment openOnlineFragment = new OpenOnlineFragment();
        openOnlineFragment.setUserController(userController);
        return openOnlineFragment;

    }

    private void setUserController(UserController userController){
        this.userController = userController;
    }



}
