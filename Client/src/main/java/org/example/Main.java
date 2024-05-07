package org.example;

import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);//Para strings

    public static void main(String[] args) {

        Controller controller = new Controller();

        Menu menuInicial = new Menu(new String[]{
                "Registar",
                "Autenticar",
                "Listar Albuns",
                "Criar Album",
                "Obter replica",
        });

        Menu MenuEditing = new Menu(new String[]{
                "Inserir um novo ficheiro",
                "Remover Ficheiro",
                "Adicionar Utilizador",
                "Remover Utilizador",
                "Classificar ficheiro",
                "Chat"
        });

        MenuEditing.setHandler(1, () -> {
            System.out.println("Nome do Ficheiro:");
            String nomeFicheiro = scanner.nextLine();
            System.out.println("Path do Ficheiro:");
            String path = scanner.nextLine();
            controller.addFile(nomeFicheiro, path);
        });

        MenuEditing.setHandler(3,() -> {
            System.out.println("Nome do Utilizador:");
            String nome = scanner.nextLine();
            controller.addUser(nome);
        });

        MenuEditing.setHandler(6, () -> {
            controller.chat();
        });


        //Fazer o registo
        menuInicial.setHandler(1, () -> {
                System.out.println("Nome de Utilizador:");
                String nome = scanner.nextLine();
                System.out.println("Password:");
                String password = scanner.nextLine();
                controller.registar(nome,password);
        });

        //Autenticar
        menuInicial.setHandler(2, () -> {
            System.out.println("Nome de Utilizador:");
            String nome = scanner.nextLine();
            System.out.println("Password:");
            String password = scanner.nextLine();
            controller.autenticar(nome,password);
        });
        menuInicial.setHandler(3, () -> {
            controller.listaAlbuns();
        });

        menuInicial.setHandler(4, () -> {
            System.out.println("Nome do Album:");
            String nome = scanner.nextLine();
            controller.criaAlbum(nome);
        });

        menuInicial.setHandler(5, () -> {
            System.out.println("Nome do Album:");
            String nome = scanner.nextLine();
            Boolean edition =controller.getAlbum(nome);
            if (edition) {
                MenuEditing.run();
            }


        });

        menuInicial.setPreCondition(1, () -> !controller.Precondition1());
        menuInicial.setPreCondition(2, () -> !controller.Precondition1());
        menuInicial.setPreCondition(3, controller::Precondition1);
        menuInicial.setPreCondition(4, controller::Precondition1);
        menuInicial.setPreCondition(5, controller::Precondition1);






        /*
        menuInicial.setHandler(2, () -> {

            NewMenu menuAddDivisoes = new NewMenu(new String[]{
                    "Adicionar uma nova divisão"
            });

        System.out.println("Nome de Utilizador:");
        String nome = scanner.nextLine();
        System.out.println("Password");
        String password = scanner.nextLine();
        controller.autenticar(nome,password);

        //controller.InsereCasa(nome, nif, nomeEmpresa);

            try {
                menuAddDivisoes.setHandler(1, () -> {
                    try {
                        System.out.println("Insira o nome da Divisão da casa que pretende adicionar:");
                        String nomeDivisao = scanner.nextLine();
                        controller.addRoom(nif, nomeDivisao);
                    } catch (DivisaoJaExisteException a) {
                        System.out.println(a.getMessage());
                    }
                });

                menuAddDivisoes.run();
            } catch (NumberFormatException e) {
                System.out.println("Formato do NIF errado! Não foi possível adicionar a Casa Inteligente!");
            }

    });
         */

        menuInicial.run();
    }
}