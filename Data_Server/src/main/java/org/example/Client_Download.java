package org.example;

import com.google.protobuf.ByteString;
import inc.FileDownloadRequest;
import inc.FileUploadRequest;
import inc.Rx3FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.*;

public class Client_Download {
    private static final int BATCH_SIZE = 1024;
    private static byte[] buffer = new byte[BATCH_SIZE];

    public static void main(String[] args) {
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
}

