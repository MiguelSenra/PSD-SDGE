package org.example;

import com.google.protobuf.ByteString;
import inc.FileUploadRequest;
import inc.Rx3FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.*;

public class Client_Upload {
    private static final int BATCH_SIZE = 1024;
    private final static byte[] buffer = new byte[BATCH_SIZE];

    public static void main(String[] args) throws Exception {
        var channel = ManagedChannelBuilder.forAddress("localhost", 12345)
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);


        try (FileInputStream fileInputStream = new FileInputStream("currículo.pdf")) {
            stub.upload(Flowable.generate(emitter -> {
                        int numBytesRead = fileInputStream.read(buffer);
                        if (numBytesRead == -1) {
                            emitter.onComplete();
                            //fileInputStream.close();
                        } else {
                            FileUploadRequest request = FileUploadRequest.newBuilder()
                                    .setFileName("curriculo1.pdf")
                                    .setChunk(ByteString.copyFrom(buffer))
                                    .build();
                            emitter.onNext(request);
                        }
                    })).map(n -> "Result: " + n)
                    .blockingSubscribe(System.out::println);

        }

    }
}
