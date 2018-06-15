package sieciowe.programowanie;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

public class SMTPClient {
    private final String username;
    private String host;
    private int port;
    private SSLSocket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String password;
    private final String client;
    private final String sender;
    private final String recipient;
    private String subject;
    private String content;


    public SMTPClient(String host, String port, String client, String sender, String recipient, String username, String password) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.client = client;
        this.sender = sender;
        this.recipient = recipient;
        this.username = username;
        this.password = password;
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
        var initialMessage = this.input.readLine();
        if(!initialMessage.startsWith("220")) throw new Exception("Connection established not correctly.");
    }

    public void authorize() throws Exception {
        this.performEhlo();
        this.performAuthPlain();
        this.performEncodedLoginData();
        System.out.println("Authorized as " + this.username);
    }

    private void performEhlo() throws Exception {
        this.output.write("EHLO " + this.client + "\n");
        this.output.flush();

        var helloMessage = this.input.readLine();
        while(this.input.ready()) {
            var moreInfo = this.input.readLine();
            if(!moreInfo.startsWith("250")) throw new Exception("Command EHLO " + this.client + " failed: " + moreInfo);
        }

        if(!helloMessage.startsWith("250")) throw new Exception("Command EHLO " + this.client + " failed: " + helloMessage);
    }

    private void performAuthPlain() throws Exception {
        this.output.write("AUTH PLAIN\n");
        this.output.flush();

        var code = this.input.readLine();
        if(!code.startsWith("334")) throw new Exception("Command AUTH PLAIN failed: " + code);
    }

    private void performEncodedLoginData() throws Exception {
        var encoder = Base64.getEncoder();
        var mergedLoginPassword = "\0" + this.username + "\0" + this.password;
        var encodedData = encoder.encodeToString(mergedLoginPassword.getBytes());
        this.output.write(encodedData + "\n");
        this.output.flush();

        var response = this.input.readLine();
        if(!response.startsWith("235")) throw new Exception("Login failed: " + response);
    }

    public void sendMessage(String subject, String content) {
        this.subject = subject;
        this.content = content;
        try {
            this.setFrom();
            this.setTo();
            this.setMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Message sent at " + LocalDateTime.now());
        System.out.println("From: " + this.sender);
        System.out.println("To: " + this.recipient);
        System.out.println("Subject: " + this.subject);
        System.out.println("Content: " + this.content);
    }

    private void setFrom() throws Exception {
        this.output.write("MAIL FROM:<" + this.sender + ">\n");
        this.output.flush();

        var response = this.input.readLine();
        if(!response.startsWith("250")) throw new Exception("Command MAIL FROM <" + this.sender + "> failed: " + response);
    }

    private void setTo() throws Exception {
        this.output.write("RCPT TO:<" + this.recipient + ">\n");
        this.output.flush();

        var response = this.input.readLine();
        if(!response.startsWith("250")) throw new Exception("Command RCPT TO <" + this.recipient + "> failed: " + response);
    }

    private void setMessage() throws Exception {
        this.output.write("DATA\n");
        this.output.flush();

        var response = this.input.readLine();
        if(!response.startsWith("354")) throw new Exception("Command DATA failed: " + response);

        String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
        var dateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);

        this.output.write("Date: " + dateFormat.format(new Date()) + "\nFrom: Sieciowe <" + this.sender + ">\nTo: Sieciowe <" + this.recipient
                + ">\nSubject: " + this.subject + "\n\n" + this.content);
        this.output.write("\n.\n");
        this.output.flush();

        response = this.input.readLine();
        if(!response.startsWith("250")) throw new Exception("Sending message failed : " + response);
    }
}
