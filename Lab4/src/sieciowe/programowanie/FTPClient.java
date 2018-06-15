package sieciowe.programowanie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;

public class FTPClient {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private Socket socket;
    private Socket passiveSocket;
    private PrintWriter output;
    private PrintWriter passiveOutput;
    private BufferedReader input;
    private BufferedReader passiveInput;
    private int passivePort;
    private LinkedList<String> recursiveTree;

    public FTPClient(String host, String port, String username, String password) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.username = username;
        this.password = password;
    }

    public void connect() throws Exception {
        this.socket = new Socket(this.host, this.port);

        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        if(!socket.isConnected()) throw new Exception("Connection failed.");

        var response = readBuffer(220);

//        System.out.println("Connected to " + this.host + ":" + this.port);
    }

    public void authenticate() throws Exception {
        this.output.write("USER " + this.username + "\n");
        this.output.flush();

        var response = this.readBuffer(331);

        this.output.write("PASS " + this.password + "\n");
        this.output.flush();

        response = this.readBuffer(230);

        this.enterPassiveMode();
    }

    public void displayRoot() throws Exception {

        this.output.write("NLST /\n");
        this.output.flush();

        var response = this.readPassiveBuffer(0);
        var controlResponse = this.readBuffer(0);
        System.out.println("B - back");
        int i = 1;
        for(String line : response) {
            System.out.println(i + ". " + line);
            i++;
        }

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.next();
        String newDirectory;
        if(choice.equals("B")) {
            this.output.write("CDUP\n");
            this.output.flush();
            response = this.readBuffer(0);
            newDirectory = "";
        }
        else {
            newDirectory = response.get(Integer.parseInt(choice) - 1);
        }

        this.output.write("PWD\n");
        this.output.flush();

        response = this.readBuffer(0);
        String currentPath = response.get(0).split(" ")[1];
        Character toReplace = '"';
        currentPath = currentPath.replace(toReplace.toString(), "");
        System.out.println("Current location: " + currentPath + newDirectory);

        this.output.write("CWD " + currentPath + newDirectory + "\n");
        this.output.flush();
        response = this.readBuffer(0);

        this.openPassiveConnection(this.passivePort);
        this.displayPath(currentPath + newDirectory);

        this.output.write("QUIT\n");
        this.output.flush();

        response = this.readBuffer(0);
    }

    private ArrayList<String> readBuffer(int successCode) throws Exception {
        ArrayList<String> readText = new ArrayList<>();
        Thread.sleep(1000);
        while(this.input.ready()) {
            var response = this.input.readLine();
            readText.add(response);
            if(successCode != 0 && !response.startsWith(String.valueOf(successCode))) throw new Exception("Error during reading from buffer " + response);
        }

        return readText;
    }

    private ArrayList<String> readPassiveBuffer(int successCode) throws Exception {
        ArrayList<String> readText = new ArrayList<>();
        Thread.sleep(1000);
        while(this.passiveInput.ready()) {
            var response = this.passiveInput.readLine();
            readText.add(response);
            if(successCode != 0 && !response.startsWith(String.valueOf(successCode))) throw new Exception("Error during reading from buffer " + response);
        }

        return readText;
    }

    private void enterPassiveMode() throws Exception {
        this.output.write("PASV\n");
        this.output.flush();

        var response = this.readBuffer(0);
        this.passivePort = extractPortFromPasvResponse(response.toString());

        this.openPassiveConnection(this.passivePort);
    }

    private void openPassiveConnection(int port) throws Exception {
        this.passiveSocket = new Socket(this.host, port);

        this.passiveOutput = new PrintWriter(passiveSocket.getOutputStream(), true);
        this.passiveInput = new BufferedReader(new InputStreamReader(passiveSocket.getInputStream()));

        if(!passiveSocket.isConnected()) throw new Exception("Connection failed.");
    }

    private int extractPortFromPasvResponse(String pasvResponse) {
        String[] octets = pasvResponse.split(",");
        String portFirst = Integer.toHexString(
                Integer.parseInt(octets[octets.length-2])
        );
        String portSecond = Integer.toHexString(
                Integer.parseInt(octets[octets.length-1].replaceAll("[^\\d.]",
                        ""))
        );
        String port = portFirst + portSecond;
        return Integer.parseInt(port, 16);
    }

    public void displayPath(String path) throws Exception {
        this.output.write("NLST " + path + "\n");
        this.output.flush();

        var response = this.readPassiveBuffer(0);
        var controlResponse = this.readBuffer(0);
        System.out.println("B - back");
        int i = 1;
        for(String line : response) {
            System.out.println(i + ". " + line);
            i++;
        }

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.next();
        String newDirectory;
        if(choice.equals("B")) {
            this.output.write("CDUP\n");
            this.output.flush();
            response = this.readBuffer(0);
            newDirectory = "";
        }
        else {
            newDirectory = response.get(Integer.parseInt(choice) - 1);
        }

        this.output.write("PWD\n");
        this.output.flush();

        response = this.readBuffer(0);
        String currentPath = response.get(0).split(" ")[1];
        Character toReplace = '"';
        currentPath = currentPath.replace(toReplace.toString(), "");
        System.out.println("Current location: " + currentPath + newDirectory);

        this.openPassiveConnection(this.passivePort);
        this.displayPath(currentPath + newDirectory);

        this.output.write("QUIT\n");
        this.output.flush();

        response = this.readBuffer(0);
    }

    public void displayTree() throws Exception {
        this.recursiveTree = new LinkedList<>();
        getDirectoryContentRecursively("/");
        for (String line:
             this.recursiveTree ) {
            System.out.println(line);
        }
    }

    public ArrayList<String> getDirectoryContentRecursively(String directory) throws Exception {
        this.output.write("PWD\n");
        this.output.flush();

        var response = this.readBuffer(0);
        String currentPath = response.get(0).split(" ")[1];
        Character toReplace = '"';
        currentPath = currentPath.replace(toReplace.toString(), "");

        String path = currentPath + directory;
        this.output.write("LIST " + path + "\n");
        this.output.flush();

        response = this.readPassiveBuffer(0);
        var controlResponse = this.readBuffer(0);

        if(directory == "/") {
            for (String item : response) {
                this.recursiveTree.add(getNameFromListResponse(item));
            }
        }

        for (String subPath:
             response) {
            if (this.getIsDirectory(subPath) && !this.getIsLink(subPath)) {
                this.openPassiveConnection(this.passivePort);
                var subContent = getDirectoryContentRecursively(getNameFromListResponse(subPath));
                for (String subItem:
                     subContent) {
                    String prefix = "--";
                    String subItemName = this.getNameFromListResponse(subItem);
                    var index = subContent.indexOf(subItem);
                    subItem = prefix.concat(subItemName);
                    subContent.set(index, subItem);
                }
                this.recursiveTree.addAll(subContent);
                //response.addAll(response.indexOf(subPath), subContent);
            }
        }

        return response;
    }

    private boolean getIsDirectory(String listResponseItem) {
        return listResponseItem.charAt(0) == 'd';
    }

    private boolean getIsLink(String listResponseItem) {
        var itemName = getNameFromListResponse(listResponseItem);
        return itemName.equals(".") || itemName.equals("..");
    }

    private String getNameFromListResponse(String listResponseItem) {
        var splitted = listResponseItem.split(" ");
        return splitted[splitted.length-1];
    }

}
