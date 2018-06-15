package sieciowe.programowanie;

import java.io.IOException;
import java.util.Scanner;

public class Menu {
    public void display() {
        System.out.println("1. Root directory || 2. Enter path manually || 3. Display tree");
    }

    public void handleChoice(FTPClient client) throws Exception {
        Scanner scanner = new Scanner(System.in);
        var choice = scanner.next();
        switch (choice) {
            case "1":
                client.displayRoot();
                break;
            case "2":
                System.out.print("Enter path: ");
                String scanned = scanner.next();
                client.displayPath(scanned);
                break;
            case "3":
                client.displayTree();
            default:
                System.out.println("Brak opcji.");
                break;
        }
    }
}
