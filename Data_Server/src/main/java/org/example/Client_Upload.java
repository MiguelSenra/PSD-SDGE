package org.example;

import com.ericsson.otp.erlang.*;
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


        try (FileInputStream fileInputStream = new FileInputStream("senra_2.jpg")) {
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
