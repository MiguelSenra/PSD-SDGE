package org.example;

public class Controller {

    private Sistema sistema;

    public Controller() {
        this.sistema = new Sistema();
    }

    public boolean Precondition1() {
        return this.sistema.isAutenticated();
    }

    public void registar(String username, String password) {
        this.sistema.registar(username, password);
    }

    public void autenticar(String username, String password) {
        this.sistema.autenticar(username, password);
    }
    public void listaAlbuns() {
        this.sistema.listaAlbuns();
    }

    public void criaAlbum(String nome) {
        this.sistema.criaAlbum(nome);
    }

    public Boolean getAlbum(String nome) {
        return this.sistema.getAlbum(nome);
    }

    /*public void BeginEdition(String nomeAlbum, String username) {
        this.sistema.BeginEdition(nomeAlbum);
    }
    */
    public void TerminateEdition() {
        this.sistema.TerminateEdition();
    }



    public void addFile(String nomeFile, String path) {
        this.sistema.addFile(nomeFile, path);
    }
    public void removeFile(String nomeFile) {
        this.sistema.removeFile(nomeFile);
    }

    public void addUser(String nome) {
        this.sistema.addUser(nome);
    }
    public void removeUser(String nome) {
        this.sistema.removeUser(nome);
    }

    public void chat() {
        this.sistema.chat();
    }

    public void downloadFile (String nomeFicheiro, String path) {
        this.sistema.downloadFile(nomeFicheiro, path);
    }

    public void infoAlbum() {
        this.sistema.infoAlbum();
    }



}
