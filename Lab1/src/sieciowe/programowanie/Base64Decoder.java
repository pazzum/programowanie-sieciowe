package sieciowe.programowanie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Base64Decoder extends Base64Coder implements CoderInterface {
    private final static String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private String extension;

    Base64Decoder(String filePath) {
        this.filePath = filePath;
    }

    public void decode() throws Exception {
        this.input = this.openFile();

        this.extension = this.extractExtension();
        this.input = this.extractContent();

        // remove/ignore any characters not in the base64 characters list
        // or the pad character -- particularly newlines
        this.input = this.input.replaceAll("[^" + base64chars + "=]", "");

        // replace any incoming padding with a zero pad (the 'A' character is
        // zero)
        var p = (this.input.charAt(this.input.length() - 1) == '=' ?
                (this.input.charAt(this.input.length() - 2) == '=' ? "AA" : "A") : "");
        var r = "";
        this.input = this.input.substring(0, this.input.length() - p.length()) + p;


        // increment over the length of this encoded string, four characters
        // at a time
        for (int c = 0; c < this.input.length(); c += 4) {

            // each of these four characters represents a 6-bit index in the
            // base64 characters list which, when concatenated, will give the
            // 24-bit number for the original 3 characters
            int n = (base64chars.indexOf(this.input.charAt(c)) << 18)
                    + (base64chars.indexOf(this.input.charAt(c + 1)) << 12)
                    + (base64chars.indexOf(this.input.charAt(c + 2)) << 6)
                    + base64chars.indexOf(this.input.charAt(c + 3));

            // split the 24-bit number into the original three 8-bit (ASCII)
            // characters
            r += "" + (char) ((n >>> 16) & 0xFF) + (char) ((n >>> 8) & 0xFF)
                    + (char) (n & 0xFF);
        }

        // remove any zero pad that was added to make this a multiple of 24 bits
        var output = r.substring(0, r.length() - p.length());

        this.saveOutputToFile(output);
    }

    private void saveOutputToFile(String output) throws Exception { ;
        String fileName = this.extractFileNameFromPath() + "." + extension;
        File file = new File(fileName);

        if(!file.exists()) {
            boolean newFileCreated = file.createNewFile();
            if(!newFileCreated) throw new Exception("Cannot write to file");
        }

        var writer = new BufferedWriter(new FileWriter(file));
        writer.write(output);
        writer.flush();

        System.out.println("Output has been saved to " + fileName + " file.");
    }

    private String extractFileNameFromPath() {
        return this.filePath.substring(0, this.filePath.indexOf('.'));
    }

    private String extractExtension() {
        String[] array = this.input.split("base64,");
        return array[0].substring(array[0].indexOf("/") + 1, array[0].indexOf(";"));
    }

    private String extractContent() {
        String[] array = this.input.split("base64,");
        return array[1];
    }

    private void validateExtensions() throws Exception {
        if(!this.filePath.endsWith(".b64")) throw new Exception("Incorrect extension.");
    }
}
