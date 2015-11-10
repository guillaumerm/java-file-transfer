/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Gus
 */
public class FxTimer extends Observable {

    private Timer timer = new Timer();
    private TimerTask timerTask;
    private long periode;

    public FxTimer(long temps){
        periode = temps;
        
        timerTask = new TimerTask() {

            @Override
            public void run() {
                setChanged();
                notifyObservers();
            }
        };
    }
    
    public void start(){
        timer.schedule(timerTask, 0, periode);
    }
    
    public void cancel(){
        timerTask.cancel();
    }
}
