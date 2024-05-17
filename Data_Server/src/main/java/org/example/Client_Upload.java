package org.example;

import com.ericsson.otp.erlang.*;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import inc.FileUploadRequest;
import inc.Rx3FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.*;
import java.security.MessageDigest;

public class Client_Upload {
    private static final int BATCH_SIZE = 1024;
    private final static byte[] buffer = new byte[BATCH_SIZE];

    public static void main(String[] args) throws Exception {
        var channel = ManagedChannelBuilder.forAddress("localhost", 12346)
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);

        try (FileInputStream fileInputStream = new FileInputStream("senra_3.jpg")) {
            stub.upload(Flowable.generate(emitter -> {
                        int numBytesRead = fileInputStream.read(buffer);
                        if (numBytesRead == -1) {
                            emitter.onComplete();
                        } else {
                            FileUploadRequest request = FileUploadRequest.newBuilder()
                                    .setChunk(ByteString.copyFrom(buffer))
                                    .setSsaKey(Hashing.sha256().hashString("ola", java.nio.charset.StandardCharsets.UTF_8).toString())
                                    .build();
                            emitter.onNext(request);
                        }
                    })).map(n -> "Result: " + n)
                    .blockingSubscribe(
                            // Lambda para processar resultados
                            System.out::println,
                            // Lambda para tratar erros
                            error -> System.err.println("Erro ao fazer upload do arquivo: " + error.getMessage())
                    );

        } catch (IOException e) {
            System.err.println("Erro ao abrir arquivo: " + e.getMessage());
        }

    }


}
