package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Editing {

    //private Scanner scanner;

    private ArrayList<Zone> members;

    String username;

    ZContext context;

    ZMQ.Socket socket;

    public Editing(String username, long portNUmber, ArrayList<Zone> members) {
            this.context = new ZContext();
            this.socket = context.createSocket(SocketType.DEALER);
            this.username=username;

            Thread replyThread = new Thread(() -> {
                try (ZContext context1 = new ZContext()) {
                    ZMQ.Socket socket1 = context1.createSocket(SocketType.ROUTER);
                    socket1.setIdentity(username.getBytes(ZMQ.CHARSET));
                    socket1.bind("tcp://localhost:" + portNUmber);
                    CausalBroadcast causal = new CausalBroadcast();
                    //while (!Thread.currentThread().isInterrupted()) {
                    while (true) {
                        byte[] id = socket1.recv();
                        byte[] req = socket1.recv();
                        if (req == null) {
                            continue; // No message, go back to recv
                        }
                        //aqui
                        //byte[] id = socket1.recv();
                        //byte[] res = socket1.recv();
                        //System.out.println(new String(res, ZMQ.CHARSET));
                        System.out.println("[" + new String(id, ZMQ.CHARSET) + "]: " + new String(req, ZMQ.CHARSET));
                        //Body body = new Body(req, ZMQ.CHARSET);
                        //Message msg = new Message(Integer(id), body);
                        //causal.fwdMsg(msg);

                        // Envio da resposta
                        //socket1.sendMore(req);
                        //socket1.send("whatup".getBytes(ZMQ.CHARSET), 0);
                    }
                }
            });
            replyThread.start();
            socket.setIdentity(username.getBytes(ZMQ.CHARSET));
            System.out.println("aquiiiiiiiiiiiiiiiiiiiiii"+members);
            this.socket.connect("tcp://localhost:" + 12346);
            /*
            for (int i = 0; i < members.size(); i++) {
                System.out.println("ola"+members.get(i).getHash());
                this.socket.connect("tcp://localhost:" + members.get(i).getPort());
            }*/
    }

    public void chat() {
        Scanner scanner = new Scanner(System.in);
            try {
                String message;
                while(scanner.hasNextLine() && !(message = scanner.nextLine()).equals("")) {// Aguarda pressionar Enter
                    //socket.sendMore("arg[]".getBytes(ZMQ.CHARSET));
                    //socket.sendMore(this.username.getBytes(ZMQ.CHARSET));
                    socket.send(message.getBytes(ZMQ.CHARSET), 0);
                    System.out.println("enviou");
                    //socket.sendMore("server1".getBytes(ZMQ.CHARSET));
                    //socket.send(message.getBytes(ZMQ.CHARSET), 0);
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                scanner.next();
                scanner.close();
            }
        System.out.println("Erro1:");
            //scanner.reset();
            //scanner.close();
        }


}
