package org.example;

import com.google.protobuf.ByteString;
import inc.FileUploadResponse;
import inc.FileUploadRequest;
import inc.Rx3FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.FileInputStream;

public class Client {
    private static final int BATCH_SIZE = 1024;
    public static void main(String[] args) throws Exception {
        var channel = ManagedChannelBuilder.forAddress("localhost", 12345)
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);

        try (FileInputStream fileInputStream = new FileInputStream("senra.jpg")) {
            byte[] buffer = new byte[BATCH_SIZE];
            int batchNumber = 1;
            while (true) {
                int numBytesRead = fileInputStream.read(buffer);
                if (numBytesRead == -1) {
                    break; // Fim do arquivo
                }
                byte[] chunk = new byte[numBytesRead];
                System.arraycopy(buffer, 0, chunk, 0, numBytesRead);

                FileUploadRequest request = FileUploadRequest.newBuilder().setFileName("senra_otimizado.jpg").setChunk(ByteString.copyFrom(chunk))
                .build();
                // Enviar o chunk para o servidor aqui
                stub.upload(Flowable.just(request))
                                //.map(n ->  FileUploadRequest.newBuilder().build()))
                        .map(n -> "Result: "+n)
                        .blockingSubscribe(System.out::println);

                System.out.printf("Sent - batch #%d - size - %d\n", batchNumber, numBytesRead);
                batchNumber++;
            }
        }
    }
}