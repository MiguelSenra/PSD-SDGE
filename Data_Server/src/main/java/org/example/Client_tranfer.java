package org.example;

import com.google.protobuf.ByteString;
import inc.FileDownloadRequest;
import inc.FileUploadRequest;
import inc.Rx3FileServiceGrpc;
import inc.TransferDataNewServerRequest;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.*;

public class Client_tranfer {
    private static final int BATCH_SIZE = 1024;
    private static byte[] buffer = new byte[BATCH_SIZE];

    public static void main(String[] args) {
        var channel = ManagedChannelBuilder.forAddress("localhost", 12345)
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);

        TransferDataNewServerRequest request = TransferDataNewServerRequest.newBuilder()
                .setSsaKey("a")
                .build();

        final FileOutputStream[] fileOutputStream = {null};
            // Submeter a solicitação de download e processar as respostas
            stub.transferDataNewServer(Flowable.just(request))
                    .map(response -> {
                        try {
                            if (response.getSeqNum()==1) {
                                fileOutputStream[0] = new FileOutputStream("ola"+response.getFileName());
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
}