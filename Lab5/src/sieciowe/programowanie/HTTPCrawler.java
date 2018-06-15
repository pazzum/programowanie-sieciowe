package sieciowe.programowanie;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class HTTPCrawler  {
    private String host;
    private int port;
    private PrintWriter output;
    private BufferedReader input;
    private DOMDocument rootDocument;
    private int depth;
    private LinkedList<DOMDocument> visited;

    public HTTPCrawler(String host, String port) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.visited = new LinkedList<>();
    }

    public void connect() throws Exception {
        Socket socket = new Socket(this.host, this.port);

        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.depth = Integer.parseInt(ConfigReader.getValue("depth"));

        if (!socket.isConnected()) throw new Exception("Connection failed.");
    }

    public void dump() throws Exception {
        var url = generateUrl("", "");
        var dumpedData = doGet(url);
        this.rootDocument = new DOMDocument(implodeString(dumpedData));
        this.rootDocument.setUri("");
        this.rootDocument.setFullUri("");
        this.rootDocument.setAbsoluteUrl(url);
        this.visited.add(this.rootDocument);

        this.findChildren(this.rootDocument);
    }

    public DOMDocument[] findChildren(DOMDocument document) throws Exception {
        DOMDocument[] children = null;
        if(document.getDepth() < this.depth && document.getChildrenCount() > 0) {
            var anchors = document.getAnchors();
            children = new DOMDocument[document.getChildrenCount()];
            var i = 0;
            for (String anchor:
                 anchors) {
                this.connect();
                var url = this.generateUrl("/" + anchor, document.getUri());
                var dumpedData = doGet(url);
                children[i] = new DOMDocument(implodeString(dumpedData), document);
                children[i].setUri("/" + cutFileName(anchor));
                children[i].setFullUri(anchor);
                children[i].setAbsoluteUrl(url);
                this.visited.add(children[i]);
                i++;
            }
        }
        document.setChildren(children);

        if(children != null) {
            for(DOMDocument child : children) {
                this.findChildren(child);
            }
        }
        return children;
    }

    private String cutFileName(String path) {
        var splitted = path.split("/");
        var toCut = "/"  + splitted[splitted.length-1];
        if(!path.contains(toCut)) return path;
        return path.substring(0, path.indexOf(toCut));
    }

    private ArrayList<String> doGet(String url) throws IOException, InterruptedException {
        if(this.lookForVisited(url)) return null;
        Thread.sleep(1000);
        this.sendCommand("GET " + url);

        return getResponse();
    }

    private boolean lookForVisited(String lookupUrl) {
        for(DOMDocument document : this.visited) {
            if(lookupUrl.equals(document.getAbsoluteUrl())) {
                return true;
            }
        }
        return false;
    }

    private String generateUrl(String path, String parent) {
        return "http://" + this.host + parent + path;
    }

    private ArrayList<String> getResponse() throws IOException, InterruptedException {
        Thread.sleep(500);
        ArrayList<String> response = new ArrayList<>();
        while(this.input.ready()) {
            var line = this.input.readLine();
            response.add(line);
        }
        return response;
    }

    private void sendCommand(String command) {
        this.output.write(command + "\r\n");
        this.output.flush();
    }

    private String implodeString(ArrayList<String> arrayList) {
        var sb = new StringBuilder();

        for(String element : arrayList) {
            sb.append(element);
        }

        return sb.toString();
    }

    public DOMDocument getRootDocument() {
        return this.rootDocument;
    }
}
