package sieciowe.programowanie;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class QuitTask extends TimerTask {
    POP3Client client;
    Timer timer;

    public QuitTask(POP3Client client, Timer timer) {
        this.client = client;
        this.timer = timer;
    }

    @Override
    public void run() {
        var character = 0;
        try {
            character = System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (character == 'q') {
            timer.purge();
            timer.cancel();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Downloaded from start: " + client.getStats());
            this.cancel();
        }
    }
}
