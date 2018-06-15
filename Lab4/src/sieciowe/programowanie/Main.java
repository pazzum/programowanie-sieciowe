package sieciowe.programowanie;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            var ftpClient = new FTPClient(
                ConfigReader.getValue("host"),
                ConfigReader.getValue("port"),
                ConfigReader.getValue("username"),
                ConfigReader.getValue("password")
            );

            var menu = new Menu();
            while(true) {
                ftpClient.connect();
                ftpClient.authenticate();
                menu.display();
                menu.handleChoice(ftpClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
