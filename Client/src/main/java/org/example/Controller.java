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
}
