package sieciowe.programowanie;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class ResultBuilder {
    private DOMDocument rootDocument;

    public ResultBuilder(DOMDocument rootDocument) {
        this.rootDocument = rootDocument;
    }

    public String generateXML() throws ParserConfigurationException, TransformerException, IOException {
        Document document = this.createDocument();

        Element site = document.createElement("site");
        site.setAttribute("url", ConfigReader.getValue("host"));
        site.setAttribute("depth", ConfigReader.getValue("depth"));

        this.createNodes(this.rootDocument, document, site);

        document.appendChild(site);

        return xmlToString(document);
    }

    private void createNodes(DOMDocument domDocument, Document document, Element parent) {
        this.createImageNodes(domDocument, document, parent);
        this.createEmailNodes(domDocument, document, parent);
        this.createChildNodes(domDocument, document, parent);
    }

    private void createImageNodes(DOMDocument domDocument, Document document, Element parent) {
        for(String imagePath : domDocument.getImages()) {
            Element image = document.createElement("image");
            image.setTextContent(imagePath);
            parent.appendChild(image);
        }
    }

    private void createEmailNodes(DOMDocument domDocument, Document document, Element parent) {
        for(String emailValue : domDocument.getMails()) {
            Element mail = document.createElement("mail");
            mail.setTextContent(emailValue);
            parent.appendChild(mail);
        }
    }

    private void createChildNodes(DOMDocument domDocument, Document document, Element parent) {
        if(domDocument.getChildren() == null) return;
        for(DOMDocument child : domDocument.getChildren()) {
            Element file = document.createElement("file");
            file.setAttribute("href", child.getFullUri());
            parent.appendChild(file);

            this.createNodes(child, document, file);
        }
    }

    private Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    private String xmlToString(Document document) throws TransformerException {
        StringWriter stringWriter = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    public void saveXml(String xml) throws Exception {
        File file = new File("result.xml");

        if(!file.exists()) {
            boolean newFileCreated = file.createNewFile();
            if(!newFileCreated) throw new Exception("Cannot write to file");
        }

        var writer = new BufferedWriter(new FileWriter(file));
        writer.write(xml);
        writer.flush();
    }
}
