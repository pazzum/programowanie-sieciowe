package sieciowe.programowanie;

public class Mail {
    private String uid;
    private String id;
    private String size;
    private String from;
    private String date;
    private String subject;

    public Mail(String uid, String id, String size, String from, String date, String subject) {
        this.uid = uid;
        this.id = id;
        this.size = size;
        this.from = from;
        this.date = date;
        this.subject = subject;
    }

    public String toString() {
        return  "UID: " + this.uid + "\n" +
                "ID: " + this.id + "\n" +
                "Size: " + this.size + "\n" +
                "From: " + this.from + "\n" +
                "Date: " + this.date + "\n" +
                "Subject: " + this.subject + "\n";
    }

    public String getUid() {
        return uid;
    }
}
