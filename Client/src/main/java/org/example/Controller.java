package org.example;

public class Controller {

    private Sistema sistema;

    public Controller() {
        this.sistema = new Sistema(12345);
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

    public void addFile(String nomeFile, String path) {
        this.sistema.addFile(nomeFile, path);
    }

    public void BeginEdition(String nomeAlbum, String username) {
        this.sistema.BeginEdition(nomeAlbum);
    }

    public void chat() {
        this.sistema.chat();
    }

}
