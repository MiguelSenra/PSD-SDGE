package org.example;

import java.util.Scanner;

public class Main {
    private static Scanner is = new Scanner(System.in);//Para Números
    private static Scanner scanner = new Scanner(System.in);//Para strings
    private static final double imposto=0.23;
    private static final double valorbase=0.149;

    public static void main(String[] args) {

        Controller controller = new Controller();

        Menu menuInicial = new Menu(new String[]{
                "Registar",
                "Autenticar"

        });
        /*
        NewMenu menuDevices = new NewMenu(new String[]{
                "Inserir um SmartBulb",
                "Inserir um SmartSpeaker",
                "Inserir uma SmartCamera"
        });
         */
        /*
        menuDevices.setHandler(1,()-> {
            try {
                System.out.println("Insira o NIF do proprietário da Casa:");
                int nif = is.nextInt();
                System.out.println("Insira o nome da Divisão da casa a que pretende adicionar o SmartBulb:");
                String nomeDivisao = scanner.nextLine();
                System.out.println("Insira o nome do dispositivo");
                String nome = scanner.nextLine();
                System.out.println("Insira o tamanho da Lâmpada (em cm) ");
                double height = is.nextDouble();
                int tom = 0;
                for (int i = 0; tom < 1 || tom > 3; i++) {
                    if (i > 0)
                        System.out.println("A opção escolhida não se encontra nas opções disponíveis!\n");
                    System.out.println("Insira o tom da Lâmpada:");
                    System.out.println("1-> Tom Frio");
                    System.out.println("2-> Tom Médio");
                    System.out.println("3-> Tom Quente");
                    tom = is.nextInt();
                }
                SmartBulb smartbulb = new SmartBulb(nome, tom, height);
                controller.addDevice(nif, nomeDivisao, smartbulb);
            } catch (NumberFormatException e) {
                System.out.println("O último dado introduzido não está na forma correta. Não foi possível adicionar o dispositivo!");
            } catch (DispositivoExisteNaDivisaoException e) {
                System.out.println(e.getMessage() + "Não foi possível adicionar o dispositivo!");
            } catch (NullPointerException e) {
                System.out.println("O proprietário do NIF não possui nenhuma casa! Não foi possível adicionar o dispositivo!");
            }

        });
        menuDevices.setHandler(2,()-> {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Insira o NIF do proprietário da Casa:");
                int nif = is.nextInt();
                System.out.println("Insira o nome da Divisão da casa a que pretende adicionar o SmartSpeaker:");
                String nomeDivisao = scanner.nextLine();
                System.out.println("Insira o nome do dispositivo");
                String nome = scanner.nextLine();
                System.out.println("Insira a marca do dispositivo");
                String brand = scanner.nextLine();
                System.out.println("Insira o Canal de rádio atual do dispositivo");
                String channel = scanner.nextLine();
                int volume = -1;
                for (int i = 0; volume < 0 || volume > 20; i++) {
                    if (i > 0)
                        System.out.println("O volume selecionado não é um volume válido!\n");
                    System.out.println("Insira o volume do dispositivo (volume Mínimo: 0 , volume Máximo: 20)");
                    volume = is.nextInt();
                }
                SmartSpeaker smartspeaker = new SmartSpeaker(nome, volume, channel, brand);
                controller.addDevice(nif, nomeDivisao, smartspeaker);
            } catch (NumberFormatException e) {
                System.out.println("O último dado introduzido não está na forma correta. Não foi possível adicionar o dispositivo!");
            } catch (DispositivoExisteNaDivisaoException e) {
                System.out.println(e.getMessage() + "Não foi possível adicionar o dispositivo!");
            } catch (NullPointerException e) {
                System.out.println("O proprietário do NIF não possui nenhuma casa! Não foi possível adicionar o dispositivo!");
            }
        });

        menuDevices.setHandler(3,()-> {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Insira o NIF do proprietário da Casa:");
                int nif = is.nextInt();
                System.out.println("Insira o nome da Divisão da casa a que pretende adicionar a SmartCamera:");
                String nomeDivisao = scanner.nextLine();
                System.out.println("Insira o nome do dispositivo");
                String nome = scanner.nextLine();
                System.out.println("Insira o tamanho do ficheiro da camara (em Gigabytes):");
                int size = is.nextInt();
                System.out.println("Insira a resolução da câmara:");
                String resolution = scanner.nextLine();
                SmartCamera smartcamera = new SmartCamera(nome, resolution, size);
                controller.addDevice(nif, nomeDivisao, smartcamera);
            } catch (NumberFormatException e) {
                System.out.println("O último dado introduzido não está na forma correta. Não foi possível adicionar o dispositivo!");
            } catch (DispositivoExisteNaDivisaoException e) {
                System.out.println(e.getMessage() + "Não foi possível adicionar o dispositivo!");
            } catch (NullPointerException e) {
                System.out.println("O proprietário do NIF não possui nenhuma casa! Não foi possível adicionar o dispositivo!");
            }
        });

        NewMenu menuQueries = new NewMenu(new String[]{
                "Qual  ́e a casa que mais gastou num Periodo de Tempo",
                "Qual o comercializador com maior volume de faturação",
                "Listar as facturas emitidas por um comercializador",
                "Dar uma ordena̧ção dos maiores consumidores de energia durante um Periodo a determinar"
        });

        menuQueries.setHandler(1, () -> {
            try {
                System.out.println("Insira a data inical (Formato dd/mm/aaaa) ");
                String inputdatainic = scanner.nextLine();
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d/M/yyyy");
                LocalDate dateinic = LocalDate.parse(inputdatainic, dateFormat);
                System.out.println("Insira a data final(Formato dd/mm/aaaa):");
                String inputdatafim = scanner.nextLine();
                LocalDate datefim = LocalDate.parse(inputdatafim, dateFormat);
                System.out.println("A casa que mais gastou no periodo entre " + dateinic + " e " + datefim + " foi:\n" + controller.executaQuery1(dateinic, datefim));
                if (datefim.compareTo(dateinic) < 0)
                    throw new DataNaoValidaException("A data inserida é anterior à data atual!");
            } catch (DateTimeParseException e) {
                System.out.println("A data não se encontra na formatação correta.\nSimulação não realizada");
            }
            catch (DataNaoValidaException e) {
                System.out.println(e.getMessage());
            }
        });

        menuQueries.setHandler(2, () -> {
            System.out.println("O fornecedor com maior volume de faturação é:\n" + controller.executaQuery2());
        });

        menuQueries.setHandler(3, () -> {
            try {
                System.out.println("Insira o nome do comercializador");
                String nome = scanner.nextLine();
                System.out.println("O fornecedor emitiu as seguintes faturas:\n" + controller.executaQuery3(nome));
            } catch (CasaExceptionFornecedorNaoExiste e) {
                System.out.println(e.getMessage());
            }
        });

        menuQueries.setHandler(4, () -> {
            try {
                System.out.println("Insira a data inical (Formato dd/mm/aaaa) ");
                String inputdatainic = scanner.nextLine();
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d/M/yyyy");
                LocalDate dateinic = LocalDate.parse(inputdatainic, dateFormat);
                System.out.println("Insira a data final(Formato dd/mm/aaaa):");
                String inputdatafim = scanner.nextLine();
                LocalDate datefim = LocalDate.parse(inputdatafim, dateFormat);
                if (datefim.compareTo(dateinic) < 0)
                    throw new DataNaoValidaException("A data inserida é anterior à data atual!");
                System.out.println("Os maiores consumidores de energia no periodo entre " + dateinic + " e " + datefim + " foram:\n" + controller.executaQuery4(dateinic, datefim));
            } catch (DateTimeParseException e) {
                System.out.println("A data não se encontra na formatação correta.\nSimulação não realizada");
            }
            catch (DataNaoValidaException e) {
                System.out.println(e.getMessage());
            }

        });
        */
        //menuInicial.setPreCondition (2, controller::Precondition2);
        //menuInicial.setPreCondition(3, controller::Precondition4);
        //menuInicial.setPreCondition(4, controller::Precondition4);
        //menuInicial.setPreCondition(5, controller::Precondition4);//são iguais
        //menuInicial.setPreCondition(6, controller::Precondition6);
        //menuInicial.setPreCondition(11, controller::Precondition4);

        menuInicial.setHandler(1, () -> {
            try {
                System.out.println("Nome de Utilizador:");
                String nome = scanner.nextLine();
                System.out.println("Password");
                String password = scanner.nextLine();
                //controller.InsereFornecedor(nome,valorbase, imposto, multiplicador);
                //System.out.println(sistema.toString());
            } catch (NumberFormatException e) {
                System.out.println("O último dado introduzido não está na forma correta. Não foi possível registar o utilizador!");
            } //catch (FornecedorComNomeJaExistenteException e) {
            //System.out.println(e.getMessage() + " Não foi possível adicionar a Empresa!");
            // }
        });

        menuInicial.setHandler(2, () -> {
            /*
            NewMenu menuAddDivisoes = new NewMenu(new String[]{
                    "Adicionar uma nova divisão"
            });
            */
            System.out.println("Nome de Utilizador:");
            String nome = scanner.nextLine();
            System.out.println("Password");
            String password = scanner.nextLine();
            controller.registar(nome,password);

            //controller.InsereCasa(nome, nif, nomeEmpresa);
            /*
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
        */
        });

        menuInicial.run();
    }
}