package sieciowe.programowanie;

public class Main {

    public static void main(String[] args) {
        Base64Encoder encoder = new Base64Encoder("test.txt");
        try {
            encoder.encode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Base64Decoder decoder = new Base64Decoder("test.b64");
        try {
            decoder.decode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
