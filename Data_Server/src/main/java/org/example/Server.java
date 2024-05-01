package org.example;

import io.grpc.ServerBuilder;

import java.util.Map;

public class Server {
    public static void main(String[] args) throws Exception {
        ServerBuilder.forPort(12345)
                .addService(new Service())
                .build()
                .start()
                .awaitTermination();
    }
}