package sieciowe.programowanie;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class POP3Client {
    private String host;
    private int port;
    private SSLSocket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String user;
    private String password;
    private LinkedList<Mail> mails;

    private final String okResponse = "+OK";

    public POP3Client(String host, String port, String user, String password) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.user = user;
        this.password = password;

        this.mails = new LinkedList<>();
    }

    public void connect() throws Exception {
        Socket basicSocket = new Socket(this.host, this.port);

        this.socket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(
                basicSocket,
                basicSocket.getInetAddress().getHostAddress(),
                basicSocket.getPort(),
                true);

        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        if(!socket.isConnected()) throw new Exception("Connection failed.");
    }

    public void authorize() throws Exception {

        if(!extractStatus(this.input.readLine()).equals(this.okResponse)) throw new Exception("Connection failed");

        this.output.write("USER " + this.user + "\n");
        this.output.flush();
        if(!extractStatus(this.input.readLine()).equals(this.okResponse)) throw new Exception("Wrong username");
        this.output.write("PASS " + this.password + "\n");
        this.output.flush();
        if(!extractStatus(this.input.readLine()).equals(this.okResponse)) throw new Exception("Wrong password");
    }

    public void getMessages() throws Exception {
        System.out.println("Looking for messages at " + LocalDateTime.now());

        final String endResponse = ".";

        this.output.write("LIST\n");
        this.output.flush();

        var response = this.read();
            for (String chunk : response) {
                if (!chunk.equals(okResponse) && !chunk.equals(endResponse)) {
                    var id = extractId(chunk);
                    var size = extractSize(chunk);
                    var uid = getUid(id);
                    var content = getContent(id, Integer.parseInt(size));
                    var mail = new Mail(uid,
                            id, extractSize(chunk),
                            extractFrom(content),
                            extractDate(content),
                            extractSubject(content));
                    var isNew = this.addNewEmail(mail);
                    if(isNew) System.out.println("New email at " + LocalDateTime.now() + ":\n" + mail.toString());
                }
        }
    }

    public String getUid(String messageId) throws Exception {
        this.output.write("UIDL " + messageId + "\n");
        this.output.flush();

        var response = this.input.readLine();
        var status = this.extractStatus(response);
        if(!status.equals(this.okResponse)) throw new Exception("UIDL " + messageId + " command failed " + response);
        return this.extractUid(response);
    }

    public String[] getContent(String messageId, int size) throws Exception {
        this.output.write("RETR " + messageId + "\n");
        this.output.flush();

        return this.readContent(size);
    }

    public String extractId(String message) {
        var elements = message.split(" ");
        return elements[0];
    }

    public String extractSize(String message) {
        var elements = message.split(" ");
        return elements[1];
    }

    public String extractStatus(String message) {
        var elements = message.split(" ");
        return elements[0];
    }

    public String extractUid(String message) {
        var elements = message.split(" ");
        return elements[2];
    }

    private ArrayList<String> read() throws IOException {
        String line;
        ArrayList<String> response = new ArrayList<>();
        while((line = this.input.readLine()) != null) {
            response.add(line);
            if(line.endsWith(".")) break;
        }
        return response;
    }

    private String[] readContent(int size) throws Exception {
        String line;
        var buffer = new ArrayList<Character>();
        int i = 0;
        while(true) {
            char character = (char) this.input.read();
            buffer.add(character);
            if(i > size && character == '.') break;
            i++;
        }

        this.input.readLine();

        StringBuilder bufferString = new StringBuilder();
        for(Character character : buffer) {
            bufferString.append(Character.toString(character));
        }

        var response = bufferString.toString().split("\r\n");

        if(!extractStatus(response[0]).equals(this.okResponse)) throw new Exception("Reading message failed " + Arrays.toString(response));

        return response;
    }

    private String extractByPattern(String[] content, String pattern, char separator) {
        String result = "";

        for(String line : content) {
            if(line.startsWith(pattern)) {
                result = line.substring(line.indexOf(separator) + 1, line.length());
                break;
            }
        }

        return result;
    }

    private String extractSubject(String[] content) {
        String pattern = "Subject: ";
        return extractByPattern(content, pattern, ' ');
    }

    private String extractFrom(String[] content) {
        String pattern = "From: ";
        return extractByPattern(content, pattern, ' ');
    }

    private String extractDate(String[] content) {
        String pattern = "Date: ";
        return extractByPattern(content, pattern, ' ');
    }

    private Boolean addNewEmail(Mail mail) {
        if(!mailExists(mail)) {
            mails.add(mail);
            return true;
        }
        return false;
    }

    private Boolean mailExists(Mail mail) {
        if(this.mails.isEmpty()) return false;

        for(Mail message : this.mails) {
            if(message.getUid().equals(mail.getUid())) return true;
        }

        return false;
    }

    public Integer getStats() {
        return mails.size();
    }

    public void close() throws IOException {
        this.output.close();
        this.input.close();
        this.socket.close();
    }
}