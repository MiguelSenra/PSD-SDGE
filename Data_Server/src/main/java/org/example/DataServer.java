package org.example;
/*

import com.ericsson.otp.erlang.*;
import inc.FileDownloadRequest;
import inc.Rx3FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.example.Client_Upload.bytesToTuple;
import static org.example.Client_Upload.tupleToBytes;

public class DataServer {
    private SocketChannel ss;
    private static final int portNumber = 12346;
    public static void main(String[] args) throws Exception {
    }

    public ArrayList<String> OpenSession() throws Exception {
        this.ss = SocketChannel.open(new InetSocketAddress(portNumber));
        String input = "Hello, World!";

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        try {
            OtpErlangObject[] tuple = new OtpErlangObject[]{
                    new OtpErlangAtom("new_data_server"),
                    new OtpErlangTuple(new OtpErlangObject[]{
                            new OtpErlangString("localhost:"+portNumber),
                            new OtpErlangBinary(hashBytes)
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
            throw new Exception("Não foi possível obter uma resposta");

        }catch(Exception e){
            System.out.println("Não consegui escrever no socket" + e.toString());
        }
        this.ss.close();
        //return "";
    }

    public void GetData() {
        var channel = ManagedChannelBuilder.forAddress("localhost", 12345)
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);

        FileDownloadRequest request = FileDownloadRequest.newBuilder()
                .setFileName("senra_2.jpg")
                .build();

        try (FileOutputStream fileOutputStream = new FileOutputStream("senra_2.jpg")) {
            // Submeter a solicitação de download e processar as respostas
            stub.download(Flowable.just(request))
                    .map(response -> {
                        try {
                            // Escrever os bytes recebidos no arquivo
                            fileOutputStream.write(response.getChunk().toByteArray());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return Flowable.empty();
                    })
                    .blockingSubscribe(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void GRPCServer() throws Exception {
        io.grpc.Server server = io.grpc.ServerBuilder.forPort(12345)
                .addService(new Service())
                .build();
        server.start();
        server.awaitTermination();
    }
}
*/