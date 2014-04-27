package icom5047.aerobal.comm;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by enrique on 4/26/14.
 */
public class BluetoothTimer {


    private Timer timer;
    private static final String TIMER_NAME = "btTimer";
    private static final long START_DELAY = 2000;
    public BluetoothTimer() {
        //Create Timer with Daemon
        timer = new Timer(TIMER_NAME, true);
    }

    public void reset(){
        //Cancel Old
        timer.cancel();
        //Create new
        timer = new Timer(TIMER_NAME, true);
    }

    public void runTask(TimerTask task, long period){
        timer.scheduleAtFixedRate(task, START_DELAY, period);
    }

}
