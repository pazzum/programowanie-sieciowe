package sieciowe.programowanie;

import java.util.TimerTask;

public class UpdateTask extends TimerTask {
    POP3Client client;

    public UpdateTask() {
        try {
            this.client = new POP3Client(
                    ConfigReader.getValue("host"),
                    ConfigReader.getValue("port"),
                    ConfigReader.getValue("username"),
                    ConfigReader.getValue("password")
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            client.connect();
            client.authorize();
            client.getMessages();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public POP3Client getClient() {
        return client;
    }
}
