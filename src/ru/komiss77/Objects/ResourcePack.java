package ru.komiss77.Objects;





public class ResourcePack {
    
    public String url;
    public String hash;
    public String filename;

    public ResourcePack(String link, String sha_1, String fileName) {
//System.out.println("vvvvvvvvvvvvvv url="+url+ "hash="+hash+" filename="+filename);
        this.url=link;
        this.hash=sha_1;
        this.filename=fileName;
    }
    
    
}
