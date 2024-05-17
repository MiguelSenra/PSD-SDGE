package org.example;

import com.ericsson.otp.erlang.*;
import com.google.common.hash.Hashing;
import inc.Rx3FileServiceGrpc;
import inc.TransferDataNewServerRequest;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Strings.repeat;
import static org.example.Client_Upload.bytesToTuple;
import static org.example.Client_Upload.tupleToBytes;

public class DataServer1 {
    private SocketChannel ss;

    private static final String ip = "localhost";
    private static final int portNumber = 12347;

    private final int Nr_keys=3;

    private ArrayList<ZoneLimits> limits;
    private List<ZoneLimits> zoneLimits;
    public static void main(String[] args) throws Exception {
        DataServer1 server = new DataServer1();
        ArrayList<Zone> servers=server.OpenSession();
        ArrayList<Zone> vizinhos = server.getVizinhos(servers);
        //System.out.println(vizinhos.toString());
        server.limits=server.getLimits(servers);
        //System.out.println(server.limits.toString());
        for (Zone z : vizinhos) {
            server.GetData(z);
        }
        //System.out.println("ativo");
        server.GRPCServer();
    }

    public ArrayList<Zone> getVizinhos(ArrayList<Zone> servers) {
        ArrayList<Zone> vizinhos = new ArrayList<>();
        Boolean add =false;
        for (Zone z : servers) {
            if (add && (!z.getIp().equals(this.ip) ||  z.getPort()!=this.portNumber)){
                //System.out.println(z);
                vizinhos.add(z);
                add=false;
            }
            else if (z.getIp().equals(this.ip) && z.getPort()==this.portNumber) {
                //System.out.println(z);
                add=true;
            }
        }
        //fecho do ciclo
        if (add  && (!servers.get(0).getIp().equals(this.ip) || servers.get(0).getPort()!=this.portNumber)) {
            vizinhos.add(servers.get(0));
        }
        return vizinhos;
    }

    public static String calculateMinimumSHA256() {
        // The minimum SHA-256 hash is a byte array filled with zeros
        return repeat("0", 64);
    }

    public static String calculateMaximumSHA256() {
        return repeat("f", 64);
    }

    public ArrayList<ZoneLimits> getLimits(ArrayList<Zone> servers) {
        ArrayList<ZoneLimits> limits = new ArrayList<>();
        String hash_before = calculateMinimumSHA256();
        for (Zone z : servers) {
            if (z.getIp().equals(this.ip) && z.getPort()==this.portNumber) {
                ZoneLimits zl = new ZoneLimits(hash_before, z.getHash());
                limits.add(zl);
            }
            hash_before = z.getHash();
        }
        //fecho do ciclo
        if (servers.getFirst().getIp().equals(this.ip) && servers.getFirst().getPort()==this.portNumber) {
            limits.add(new ZoneLimits(hash_before, calculateMaximumSHA256()));
        }
        return limits;
    }

    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomString.append(characters.charAt(index));
        }

        return randomString.toString();
    }
    public ArrayList<Zone> OpenSession() throws Exception {
        ArrayList<OtpErlangTuple> keys = new ArrayList<>();
        for (int i=0;i<Nr_keys;i++) {
            String key = generateRandomString(10);
            String hash = Hashing.sha256()
                    .hashString(key, StandardCharsets.UTF_8)
                    .toString();
            keys.add(new OtpErlangTuple(new OtpErlangObject[]{
                    new OtpErlangString(ip),
                    new OtpErlangInt(portNumber),
                    new OtpErlangString(hash)
            }));
        }

        this.ss = SocketChannel.open(new InetSocketAddress(12345));
        try {
            // Construindo uma lista de objetos Erlang a partir da lista de chaves
            OtpErlangObject[] tuple = new OtpErlangObject[]{
                    new OtpErlangAtom("new_data_server"),
                    new OtpErlangList(keys.toArray(new OtpErlangObject[0]))
            };
            OtpErlangTuple message = new OtpErlangTuple(tuple);
            ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));
            ss.write(bb);
            bb.clear();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Loop para ler os dados do socket
            int bytesRead;
            while ((bytesRead = ss.read(bb)) != -1) {
                //System.out.println("Lidos " + bytesRead + " bytes do socket.");
                bb.flip();
                byte[] receivedBytes = new byte[bb.remaining()];
                bb.get(receivedBytes);
                baos.write(receivedBytes);
                bb.clear();
            }
            byte[] receivedBytes = baos.toByteArray();
            // Concatenar os bytes recebidos em um único array
            OtpErlangTuple response = bytesToTuple(receivedBytes);
            OtpErlangObject[] fields = response.elements();

            OtpErlangObject firstField = fields[0];
            if (firstField instanceof OtpErlangList) {
                // Converter a lista em um ArrayList
                OtpErlangList lista = ((OtpErlangList) firstField);

                ArrayList<Zone> servers = new ArrayList<>();
                // Iterar sobre os elementos da lista Erlang
                for (OtpErlangObject element : lista.elements()) {
                    if (element instanceof OtpErlangTuple) {
                        OtpErlangTuple new_tuple = (OtpErlangTuple) element;
                        OtpErlangObject[] fields1 = new_tuple.elements();
                        String ip = ((OtpErlangString) fields1[0]).stringValue();
                        long portLong = ((OtpErlangLong) fields1[1]).longValue();
                        int port=(int) portLong;
                        String hash = ((OtpErlangString) fields1[2]).stringValue();
                        servers.add(new Zone(ip, port, hash));
                    }
                }
                //return arrayList;
                return servers;
            }
            throw new Exception("Não foi possível obter uma resposta");

        }catch(Exception e){
            System.out.println("Não consegui escrever no socket" + e.getMessage());
        }
        this.ss.close();
        return new ArrayList<>();
    }

    public void GetData(Zone z) {
        var channel = ManagedChannelBuilder.forAddress(z.getIp(), z.getPort())
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);
        TransferDataNewServerRequest request = TransferDataNewServerRequest.newBuilder()
                .setSsaKey(z.getHash())
                .build();

        final FileOutputStream[] fileOutputStream = {null};
        // Submeter a solicitação de download e processar as respostas
        stub.transferDataNewServer(Flowable.just(request))
                .map(response -> {
                    try {
                        if (response.getSeqNum()==1) {
                            fileOutputStream[0] = new FileOutputStream(response.getFileName());
                        }
                        // Escrever os bytes recebidos no arquivo
                        fileOutputStream[0].write(response.getChunk().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Flowable.empty();
                })
                .blockingSubscribe(System.out::println);
    }
    public void GRPCServer() throws Exception {
        io.grpc.Server server = io.grpc.ServerBuilder.forPort(this.portNumber)
                .addService(new Service(limits))
                .build();
        server.start();
        server.awaitTermination();
    }
}
