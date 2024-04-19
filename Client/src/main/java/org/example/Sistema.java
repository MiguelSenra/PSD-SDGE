package org.example;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;

import com.ericsson.otp.erlang.*;



public class Sistema {
    private SocketChannel ss;
    private String username;

    private Boolean login=false;

    public Boolean isAutenticated() {
        return this.login;
    }

    public Sistema(int portNumber) {
        try {
            this.ss = SocketChannel.open(new InetSocketAddress(portNumber));
        }
        catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    private String autentication_handler(String atom, String username, String password) {
        try {
            OtpErlangObject[] tuple = new OtpErlangObject[]{
                    new OtpErlangAtom(atom),
                    new OtpErlangTuple(new OtpErlangObject[]{
                            new OtpErlangString(username),
                            new OtpErlangString(password),
                    })
            };
            OtpErlangTuple message = new OtpErlangTuple(tuple);
            ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));
            ss.write(bb);

            bb.clear();
            while (ss.read(bb) > 0) {
                bb.flip();
                byte[] receivedBytes = new byte[bb.remaining()];
                bb.get(receivedBytes);
                OtpErlangTuple response = bytesToTuple(receivedBytes);
                OtpErlangObject[] fields = response.elements();

                OtpErlangObject firstField = fields[0];
                if (firstField instanceof OtpErlangAtom) {
                    // Converter o átomo em uma string
                    return  ((OtpErlangAtom) firstField).atomValue();
                }
            }
            throw new Exception("Não foi possível obter uma reposta");

        }catch(Exception e){
            System.out.println("Não consegui escrever no socket" + e.toString());
        }
        return "";
    }

    public void registar(String username, String password) {
        String response=this.autentication_handler("register",username,password);
        if (response.equals("ok")) {
            System.out.println("Utilizador registado com sucesso!");
        }
        else if (response.equals("user_exists")){
            System.out.println("O utilizador já está registado!");
        }
    }


    public void autenticar (String username, String password) {
        String response=this.autentication_handler("login",username,password);
        if (response.equals("ok")) {
            System.out.println("Utilizador autenticado com sucesso!");
            this.username=username;
            this.login=true;
        }
        else if (response.equals("invalid")){
            System.out.println("As credenciais são inválidas!");
        }

    }
    public void criaAlbum(String nome) {
        String response=this.autentication_handler("create_Album",nome,this.username);
        if (response.equals("album_created")) {
            System.out.println("A criado com sucesso!");
        }
        else if (response.equals("album_exists")){
            System.out.println("O album já existe!");
        }
    }

    private ArrayList<String> list_handler(String atom) {
        try {
            OtpErlangObject[] tuple = new OtpErlangObject[]{
                    new OtpErlangAtom(atom),
                    new OtpErlangTuple(new OtpErlangObject[]{
                            new OtpErlangString(this.username),
                    })
            };
            OtpErlangTuple message = new OtpErlangTuple(tuple);
            ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));
            ss.write(bb);

            bb.clear();
            while (ss.read(bb) > 0) {
                bb.flip();
                byte[] receivedBytes = new byte[bb.remaining()];
                bb.get(receivedBytes);
                OtpErlangTuple response = bytesToTuple(receivedBytes);
                OtpErlangObject[] fields = response.elements();

                OtpErlangObject firstField = fields[0];
                if (firstField instanceof OtpErlangList) {
                    // Converter a lista em um ArrayList
                    OtpErlangList lista = ((OtpErlangList) firstField);
                    ArrayList<String> arrayList = new ArrayList<>();

                    // Iterar sobre os elementos da lista Erlang
                    for (OtpErlangObject element : lista.elements()) {
                        // Converter cada elemento para uma string Java e adicionar ao ArrayList
                        String javaElement = ((OtpErlangString) element).stringValue();
                        arrayList.add(javaElement);
                    }
                    return arrayList;
                }
            }
            throw new Exception("Não foi possível obter uma reposta");

        }catch(Exception e){
            System.out.println("Não consegui escrever no socket" + e.toString());
        }
        return new ArrayList<>();
    }

    public void listaAlbuns() {
        System.out.println(list_handler("list_Album"));
    }



    public static byte[] tupleToBytes(OtpErlangTuple tuple) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OtpOutputStream otpOutputStream = new OtpOutputStream(outputStream.size());
        // Adicionar a tag 131 manualmente
        otpOutputStream.write1(OtpExternal.versionTag);
        tuple.encode(otpOutputStream);
        return otpOutputStream.toByteArray();
    }

    public static OtpErlangTuple bytesToTuple(byte[] bytes) throws IOException, OtpErlangDecodeException {
        // Descodificar os bytes em um objeto OtpErlangTuple
        OtpErlangObject object = new OtpInputStream(bytes).read_any();
        if (!(object instanceof OtpErlangTuple)) {
            throw new IllegalArgumentException("Objeto não é um OtpErlangTuple: " + object);
        }

        return (OtpErlangTuple) object;
    }


}
