package sieciowe.programowanie;

public class Main {

    public static void main(String[] args) {
        try {
            var smtpClient = new SMTPClient(
                ConfigReader.getValue("host"),
                ConfigReader.getValue("port"),
                ConfigReader.getValue("client"),
                ConfigReader.getValue("sender"),
                ConfigReader.getValue("recipient"),
                ConfigReader.getValue("username"),
                ConfigReader.getValue("password"));
            smtpClient.connect();
            smtpClient.authorize();
            smtpClient.sendMessage(ConfigReader.getValue("subject"),
                                    ConfigReader.getValue("content"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
