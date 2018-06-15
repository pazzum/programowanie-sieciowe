package sieciowe.programowanie;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DOMDocument {
    private String content;
    private DOMDocument parent;
    private DOMDocument[] children;
    private ArrayList<String> anchors;
    private ArrayList<String> images;
    private ArrayList<String> mailAnchors;
    private ArrayList<String> mails;
    private String uri;
    private String fullUri;
    private String absoluteUrl;

    public DOMDocument(String content, DOMDocument parent) {
        this.content = content;
        this.parent = parent;

        this.anchors = findSiteAnchors();
        this.images = findImages();
        this.mailAnchors = findMailAnchors();
        this.mails = findMails();
    }

    public DOMDocument(String content) {
        this.content = content;

        this.anchors = findSiteAnchors();
        this.images = findImages();
        this.mailAnchors = findMailAnchors();
        this.mails = findMails();
    }

    public ArrayList<String> getAnchors() {
        return this.anchors;
    }

    public int getChildrenCount() {
        if(this.anchors == null) return 0;
        return this.anchors.size();
    }

    public void setChildren(DOMDocument[] children) {
        this.children = children;
    }

    public DOMDocument[] getChildren() {
        return this.children;
    }

    public void setAbsoluteUrl(String url) {
        this.absoluteUrl = url;
    }

    private ArrayList<String> findSiteAnchors() {
        Pattern pattern = Pattern.compile("<a[^>]*href=\"?([^\"]*?(?:html|htm))\"?.*?>(.*?)</a>");
        Matcher matcher = pattern.matcher(this.content);

        var siteAnchors = new ArrayList<String>();

        while(matcher.find()) {
            siteAnchors.add(matcher.group(1).trim());
        }

        return siteAnchors;
    }

    private ArrayList<String> findImages() {
        Pattern pattern = Pattern.compile("<img.*?src\\s*=\\s*\"(.+?)\"");
        Matcher matcher = pattern.matcher(this.content);

        var images = new ArrayList<String>();

        while(matcher.find()) {
            images.add(matcher.group(1).trim());
        }

        return images;
    }

    private ArrayList<String> findMailAnchors() {
        Pattern pattern = Pattern.compile("<a[^>]*href=\"mailto:([^?]*)\".*?>(?:.*?)</a>");
        Matcher matcher = pattern.matcher(this.content);

        var mailAnchors = new ArrayList<String>();

        while(matcher.find()) {
            mailAnchors.add(matcher.group(1).trim());
        }

        return mailAnchors;
    }

    private ArrayList<String> findMails() {
        Pattern pattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])");
        Matcher matcher = pattern.matcher(this.content);

        var mails = new ArrayList<String>();

        while(matcher.find()) {
            mails.add(matcher.group().trim());
        }

        return mails;
    }

    public int getDepth() {
        var depth = 1;
        var parent = this.parent;
        if(parent != null) {
            depth++;
            while(parent.getParent() != null) {
                depth++;
                parent = parent.getParent();
            }
        }
        return depth;
    }

    public DOMDocument getParent() {
        return this.parent;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public ArrayList<String> getMailAnchors() {
        return mailAnchors;
    }

    public ArrayList<String> getMails() {
        return mails;
    }

    public String getFullUri() {
        return fullUri;
    }

    public void setFullUri(String fullUri) {
        this.fullUri = fullUri;
    }
}
