package sieciowe.programowanie;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Base64Coder implements CoderInterface {
    protected String filePath;
    protected String input;
    protected String output;
    protected final static String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    protected String openFile() throws Exception {
        this.validateExtensions();
        byte[] encoded = Files.readAllBytes(Paths.get(this.filePath));
        return new String(encoded);
    }

    private void validateExtensions() {

    }
}
