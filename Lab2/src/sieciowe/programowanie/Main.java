package sieciowe.programowanie;

import java.io.IOException;
import java.util.Timer;

public class Main {

    public static void main(String[] args) {
        Timer updateTimer = new Timer();
        var updateTask = new UpdateTask();
        try {
            updateTimer.schedule(updateTask, 0, Integer.parseInt(ConfigReader.getValue("frequency")) * 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timer quitingTimer = new Timer();
        quitingTimer.schedule(new QuitTask(updateTask.getClient(), updateTimer), 0, 1);
    }
}
