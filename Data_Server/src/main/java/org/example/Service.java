package org.example;

import com.google.protobuf.ByteString;
import inc.FileUploadResponse;
import inc.FileUploadRequest;
import inc.Rx3FileServiceGrpc;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Service extends Rx3FileServiceGrpc.FileServiceImplBase {
    public Flowable<FileUploadResponse> upload(Flowable<FileUploadRequest> request) {
        return request.map(requestMessage -> {
                    String campo1 = requestMessage.getFileName();
                    byte[] campo2 = requestMessage.getChunk().toByteArray();
                    return this.ficheiro(campo1, campo2);
                })
                .map(n -> FileUploadResponse.newBuilder().setSize(2).build());
    }

    public int ficheiro( String filename, byte[] data ) {
        System.out.println("entrei");
        File file = new File(filename);
        if (file.exists()) {
            System.out.println("ola");
        }
        else {
            System.out.println("foda-se");
        }
        try {
            // Verifica se o arquivo existe
            if (!(file.exists())) {
                if (file.createNewFile()) {
                    System.out.println("File created: " + file.getName());
                }
            }
            // Escreve bytes no arquivo
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                fos.write(data);
                System.out.println("Dados adicionados com sucesso ao arquivo.");
            } catch (IOException e) {
                System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar o arquivo: " + e.getMessage());
        }
        return 1;
    }
}
