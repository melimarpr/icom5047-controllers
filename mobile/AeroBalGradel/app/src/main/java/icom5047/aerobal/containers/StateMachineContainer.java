package icom5047.aerobal.containers;

import icom5047.aerobal.callbacks.StateMachineCallback;

/**
 * Created by enrique on 5/3/14.
 */
public class StateMachineContainer {
    public String command;
    public StateMachineCallback callback;

    public StateMachineContainer( String command, StateMachineCallback callback) {
        this.command = command;
        this.callback = callback;
    }
}
