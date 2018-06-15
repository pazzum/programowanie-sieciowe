package sieciowe.programowanie;

public class Main {

    public static void main(String[] args) {
        try {
            var httpCrawler = new HTTPCrawler(ConfigReader.getValue("host"), ConfigReader.getValue("port"));
            httpCrawler.connect();
            httpCrawler.dump();
            var resultBuilder = new ResultBuilder(httpCrawler.getRootDocument());
            var xml = resultBuilder.generateXML();
            resultBuilder.saveXml(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
