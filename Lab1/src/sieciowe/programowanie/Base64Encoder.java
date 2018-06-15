package sieciowe.programowanie;

import java.io.*;

public class Base64Encoder extends Base64Coder implements CoderInterface {
    private String encoded;
    private String padding;
    private Integer padCount;

    Base64Encoder(String filePath) {
        this.filePath = filePath;
        this.padding = "";
        this.encoded = "";
    }

    public void encode() throws Exception {
        this.input = openFile();

        this.padCount = this.input.length() % 3;
        if(padCount > 0) {
            this.addPadding();
        }

        for(Integer i = 0; i < input.length(); i += 3) {
            if (i > 0 && (i / 3 * 4) % 76 == 0)
                encoded += "\r\n";

            // these three 8-bit (ASCII) characters become one 24-bit number
            int n = (input.charAt(i) << 16) + (input.charAt(i + 1) << 8)
                    + (input.charAt(i + 2));

            // this 24-bit number gets separated into four 6-bit numbers
            int n1 = (n >> 18) & 63;
            int n2 = (n >> 12) & 63;
            int n3 = (n >> 6) & 63;
            int n4 = n & 63;

            // those four 6-bit numbers are used as indices into the base64
            // character list
            encoded = encoded.concat("" + base64chars.charAt(n1) + base64chars.charAt(n2)
                    + base64chars.charAt(n3) + base64chars.charAt(n4));
        }

        this.output = encoded.substring(0, encoded.length() - padding.length()) + padding;

        this.output = this.getHeader().concat(this.output);

        this.saveOutputToFile();
    }

    private void saveOutputToFile() throws Exception {
        String extension = ".b64";
        String fileName = this.extractFileNameFromPath() + extension;
        File file = new File(fileName);

        if(!file.exists()) {
            boolean newFileCreated = file.createNewFile();
            if(!newFileCreated) throw new Exception("Cannot write to file");
        }

        var writer = new BufferedWriter(new FileWriter(file));
        writer.write(this.output);
        writer.flush();

        System.out.println("Output has been saved to " + fileName + " file.");
    }

    private String extractFileNameFromPath() {
        return this.filePath.substring(0, this.filePath.indexOf('.'));
    }
    private String extractExtensionFromPath() {
        return this.filePath.substring(this.filePath.indexOf('.'), this.filePath.length());
    }


    private void validateExtensions() throws Exception {
        String[] extensions = { ".txt", ".bmp", ".zip" };

        boolean correctExtension = false;
        for (String extension : extensions
             ) {
            if(this.filePath.endsWith(extension)) {
                correctExtension = true;
            }
        }

        if(!correctExtension) {
            throw new Exception("Incorrect extension.");
        }
    }

    private void addPadding() {
        while(this.padCount < 3) {
            this.input += "\0";
            this.padding += "=";

            this.padCount++;
        }
    }

    private String getHeader() {
        String type = "";
        switch (this.extractExtensionFromPath()) {
            case ".txt":
                type = "text/txt";
                break;
            case ".bmp":
                type = "image/bmp";
                break;
            case ".zip":
                type = "archive/zip";
                break;
        }

        return "data:" + type + ";base64,";
    }


}
