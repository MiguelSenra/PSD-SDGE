package org.example;

public class FileHash {
    private String hash;
    private String fileName;

    public FileHash(String hash, String fileName) {
        this.hash = hash;
        this.fileName = fileName;
    }

    public String getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }
}
