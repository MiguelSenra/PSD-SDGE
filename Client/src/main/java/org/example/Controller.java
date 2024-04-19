package org.example;

public class Controller {

    private Sistema sistema;

    public Controller() {
        this.sistema = new Sistema(12345);
    }
/*
    public boolean Precondition2() {
        return this.sistema.getNrFornecedores()>0;
    }
*/


    public void registar(String username, String password) {
        this.sistema.registar(username, password);
    }
}
