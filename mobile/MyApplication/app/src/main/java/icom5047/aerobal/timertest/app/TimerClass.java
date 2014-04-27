package icom5047.aerobal.timertest.app;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by enrique on 4/26/14.
 */
public class TimerClass {

    private Timer timer;

    public TimerClass(){
        timer = new Timer(true);
    }

    public void startTime(TimerTask task){
        timer.scheduleAtFixedRate(task, (long)3000, (long)3000);
    }

    public void cancel(){
        timer.cancel();
    }



}
