package org.example;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.ericsson.otp.erlang.*;


public class Sistema {
    private SocketChannel ss;
    private String username;

    private long SC_portNumber=12345;

    private long clientPort=12347;

    private Boolean login=false;

    private Editing editing;

    public Boolean isAutenticated() {
        return this.login;
    }


    private String autentication_handler(String atom, String username, String password) {
        try {
            this.ss = SocketChannel.open(new InetSocketAddress((int)SC_portNumber));
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
                int bytesRead;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((bytesRead = ss.read(bb)) != -1) {
                    System.out.println("Lidos " + bytesRead + " bytes do socket.");
                    bb.flip();
                    byte[] receivedBytes = new byte[bb.remaining()];
                    bb.get(receivedBytes);
                    baos.write(receivedBytes);
                    bb.clear();
                }

                // Processa a resposta do servidor
                byte[] receivedBytes = baos.toByteArray();
                OtpErlangTuple response = bytesToTuple(receivedBytes);
                OtpErlangObject[] fields = response.elements();

                OtpErlangObject firstField = fields[0];
                if (firstField instanceof OtpErlangAtom) {
                    // Converter o átomo em uma string
                    return  ((OtpErlangAtom) firstField).atomValue();
                }
                throw new Exception("Não foi possível obter uma resposta");

            }catch(Exception e){
                System.out.println("Não consegui escrever no socket" + e.toString());
            }
        }
        catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
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
            System.out.println("Album criado com sucesso!");
        }
        else if (response.equals("album_exists")){
            System.out.println("O album já existe!");
        }
    }

    public Boolean getAlbum(String nome) {
        String response=this.get_album_handler("get_Album",nome);
        Boolean flag=false;
        if (response.equals("no_exists")){
            System.out.println("O album não existe!");
        } else if (response.equals("no_autorization")){
            System.out.println("O utilizador não pode aceder ao album!");
        }
        else {
            flag=true;
        }
        System.out.println(response);
        return flag;
    }

    private String get_album_handler(String atom, String nome) {
        try {
            // Conecta ao servidor
            try (SocketChannel ss1 = SocketChannel.open(new InetSocketAddress((int) SC_portNumber))) {
                try {
                    // Monta a mensagem a ser enviada para o servidor
                    OtpErlangObject[] tuple = new OtpErlangObject[]{
                            new OtpErlangAtom(atom),
                            new OtpErlangTuple(new OtpErlangObject[]{
                                    new OtpErlangString(nome),
                                    new OtpErlangString(this.username)
                            })
                    };
                    OtpErlangTuple message = new OtpErlangTuple(tuple);
                    ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));

                    // Envia a mensagem para o servidor
                    ss1.write(bb);
                    bb.clear();

                    int bytesRead;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while ((bytesRead = ss1.read(bb)) != -1) {
                        System.out.println("Lidos " + bytesRead + " bytes do socket.");
                        bb.flip();
                        byte[] receivedBytes = new byte[bb.remaining()];
                        bb.get(receivedBytes);
                        baos.write(receivedBytes);
                        bb.clear();
                    }

                    // Processa a resposta do servidor
                    byte[] receivedBytes = baos.toByteArray();
                    OtpErlangTuple response_sc = bytesToTuple(receivedBytes);
                    System.out.println(response_sc);
                    OtpErlangObject[] fields = response_sc.elements();
                    OtpErlangObject firstField = fields[0];
                    String response = "ok";
                    if (firstField instanceof OtpErlangAtom) {
                        response = ((OtpErlangAtom) firstField).atomValue();
                        if (response.equals("no_autorization"))
                            System.out.println("Você não é membro desse álbum!");
                        else if (response.equals("no_exists"))
                            System.out.println("O álbum indicado não existe!");
                    } else {
                        // Processa os dados do álbum
                        Map<String, Object> albumData = new HashMap<>();
                        OtpErlangTuple album = (OtpErlangTuple) firstField;
                        OtpErlangList membersList = (OtpErlangList) album.elementAt(1);
                        Map<String, Map<String, Integer>> membros = new HashMap<>();
                        HashMap<String, Integer> crdt_ids = new HashMap<>();
                        crdt_ids.put(username, 0);
                        for (OtpErlangObject membro : membersList.elements()) {
                            membros.put(((OtpErlangString) membro).stringValue(), crdt_ids);
                        }
                        albumData.put("membros", membros);
                        OtpErlangObject thirdField = album.elementAt(2);
                        Map<String, Object> ficheiros = new HashMap<>();
                        OtpErlangMap erlangMap = (OtpErlangMap) thirdField;
                        for (OtpErlangObject key : erlangMap.keys()) {
                            OtpErlangObject value = erlangMap.get(key);
                            File_CRDT file = new File_CRDT(crdt_ids, value.toString());
                            ficheiros.put(key.toString(), file);
                        }
                        albumData.put("ficheiros", ficheiros);
                        ArrayList<Editor> editors = BeginEdition(nome);
                        System.out.println("editors: " + editors);
                        this.editing = new Editing(this.username, nome, this.clientPort, editors, albumData);

                        // Mostrar os dados do álbum
                        System.out.println("Dados do álbum: " + albumData);
                    }
                    return response;
                } catch (Exception e) {
                    // Trata exceções durante o processamento da mensagem
                    System.out.println("Erro durante o processamento da mensagem: " + e.getMessage());
                }
            } catch (IOException e) {
                // Trata exceções de conexão com o servidor
                System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            }
        } catch (Exception e) {
            // Trata exceções gerais
            System.err.println("Erro geral: " + e.getMessage());
        }
        return "erro";
    }


    private ArrayList<String> list_handler(String atom) {
        try {
            this.ss = SocketChannel.open(new InetSocketAddress((int)SC_portNumber));
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

                int bytesRead;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((bytesRead = ss.read(bb)) != -1) {
                    System.out.println("Lidos " + bytesRead + " bytes do socket.");
                    bb.flip();
                    byte[] receivedBytes = new byte[bb.remaining()];
                    bb.get(receivedBytes);
                    baos.write(receivedBytes);
                    bb.clear();
                }

                // Processa a resposta do servidor
                byte[] receivedBytes = baos.toByteArray();
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
                //throw new Exception("Não foi possível obter uma reposta");

            }catch(Exception e){
                System.out.println("Não consegui escrever no socket" + e.toString());
            }
        }
        catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public void listaAlbuns() {
        System.out.println(list_handler("list_Album"));
    }


    public ArrayList<Editor> BeginEdition(String albumName) {
        ArrayList<Editor> editors = new ArrayList<>();
        try {
            SocketChannel ss1 = SocketChannel.open(new InetSocketAddress((int) SC_portNumber));
            try {
                OtpErlangObject[] tuple = new OtpErlangObject[]{
                        new OtpErlangAtom("add_editor"),
                        new OtpErlangTuple(new OtpErlangObject[]{
                                new OtpErlangString(albumName),
                                new OtpErlangString(this.username),
                                new OtpErlangString("localhost"),
                                new OtpErlangLong(this.clientPort)
                        })
                };
                OtpErlangTuple message = new OtpErlangTuple(tuple);
                ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));
                ss1.write(bb);

                bb.clear();
                int bytesRead;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((bytesRead = ss1.read(bb)) != -1) {
                    System.out.println("Lidos " + bytesRead + " bytes do socket.");
                    bb.flip();
                    byte[] receivedBytes = new byte[bb.remaining()];
                    bb.get(receivedBytes);
                    baos.write(receivedBytes);
                    bb.clear();
                }

                byte[] receivedBytes = baos.toByteArray();

                OtpErlangTuple response = bytesToTuple(receivedBytes);
                OtpErlangObject[] fields = response.elements();
                System.out.println(response);
                OtpErlangObject firstField = fields[0];
                if (firstField instanceof OtpErlangAtom) {
                    // Converter o átomo em uma string
                    String error = ((OtpErlangAtom) firstField).atomValue();
                    if (error.equals("no_autorization")) {
                        System.out.println("Você não é membro desse álbum!");
                    } else if (error.equals("no_exists")) {
                        System.out.println("O álbum indicado não existe!");
                    }
                    return null;
                } else {
                    OtpErlangList editors_erl = (OtpErlangList) firstField;
                    for (OtpErlangObject zone : editors_erl.elements()) {
                        OtpErlangTuple zoneTuple = (OtpErlangTuple) zone;
                        String ip = ((OtpErlangString) zoneTuple.elementAt(0)).stringValue();
                        int port = ((OtpErlangLong) zoneTuple.elementAt(1)).intValue();
                        String user = ((OtpErlangString) zoneTuple.elementAt(2)).stringValue();
                        editors.add(new Editor(ip, port, user));
                    }
                    return editors;
                }
                //throw new Exception("Não foi possível obter uma reposta");

            } catch (Exception e) {
                System.out.println("Erro" + e.getMessage());
            }
            return new ArrayList<>();
        }
        catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public void TerminateEdition() {
        Map<String,Object> album=this.editing.TerminateEdition();
        //System.out.println("Album terminado com sucesso!");
        ArrayList<String> membros= (ArrayList<String>) album.get("membros");
        Map<String,String> ficheiros= (Map<String,String>) album.get("ficheiros");
        OtpErlangMap ficheiros_erl = new OtpErlangMap();
        for (String key : ficheiros.keySet()) {
            ficheiros_erl.put(new OtpErlangString(key), new OtpErlangString(ficheiros.get(key)));
        }

        //System.out.println("Membros do album: "+membros);
        OtpErlangObject[] participantes = new OtpErlangObject[membros.size()];
        for (int i = 0; i < membros.size(); i++) {
            participantes[i] = new OtpErlangString(membros.get(i));
        }
        try {
            SocketChannel ss1 = SocketChannel.open(new InetSocketAddress((int) SC_portNumber));
            try {
                OtpErlangObject[] tuple = new OtpErlangObject[]{
                        new OtpErlangAtom("terminate_edit_Album"),
                        new OtpErlangTuple(new OtpErlangObject[]{
                                new OtpErlangString(this.editing.albumName),
                                new OtpErlangString(this.username),
                                ficheiros_erl,
                                new OtpErlangList(participantes)
                        }),
                };
                OtpErlangTuple message = new OtpErlangTuple(tuple);
                System.out.println(message);
                ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));
                ss1.write(bb);
                bb.clear();
                int bytesRead;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((bytesRead = ss1.read(bb)) != -1) {
                    System.out.println("Lidos " + bytesRead + " bytes do socket.");
                    bb.flip();
                    byte[] receivedBytes = new byte[bb.remaining()];
                    bb.get(receivedBytes);
                    baos.write(receivedBytes);
                    bb.clear();
                }

                byte[] receivedBytes = baos.toByteArray();

                OtpErlangTuple response = bytesToTuple(receivedBytes);
                OtpErlangObject[] fields = response.elements();
                System.out.println(response);
                OtpErlangObject firstField = fields[0];
                if (firstField instanceof OtpErlangAtom) {
                    // Converter o átomo em uma string
                    String error = ((OtpErlangAtom) firstField).atomValue();
                    if (error.equals("no_autorization")) {
                        System.out.println("Você não é membro desse álbum!");
                    } else if (error.equals("no_exists")) {
                        System.out.println("O álbum indicado não existe!");
                    }
                }

            } catch (Exception e) {
                System.out.println("Erro" + e.getMessage());
            }
        }
        catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
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
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
        OtpErlangObject object = new OtpInputStream(bytes).read_any();
        //System.out.println(object);
        if (!(object instanceof OtpErlangTuple)) {
            throw new IllegalArgumentException("Objeto não é um OtpErlangTuple: " + object);
        }

        return (OtpErlangTuple) object;
    }

    public void addFile(String nomeFile, String path) {
        this.editing.addFile(nomeFile,path);
    }

    public void removeFile(String nomeFile) {
        this.editing.removeFile(nomeFile);
    }

    public void addUser(String nome) {
        this.editing.addUser(nome);
    }

    public void removeUser(String nome) {
        this.editing.removeUser(nome);
    }

    public void chat(){
        this.editing.chat();
    }

}