package org.example;

import com.google.protobuf.ByteString;
import inc.*;
import inc.FileDownloadRequest;
import inc.FileDownloadResponse;
import inc.FileUploadRequest;
import inc.FileUploadResponse;
import inc.Message;
import inc.Rx3FileServiceGrpc;
import io.reactivex.rxjava3.core.Flowable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.lang.System.out;


public class Service extends Rx3FileServiceGrpc.FileServiceImplBase {

    private Map<String, Integer> files = new HashMap<>();
    public Flowable<FileUploadResponse> upload(Flowable<FileUploadRequest> request) {
        return request.map(requestMessage -> {
                    String campo1 = requestMessage.getFileName();
                    byte[] campo2 = requestMessage.getChunk().toByteArray();
                    return guardar_file(campo1, campo2);
                })
                .map(n -> FileUploadResponse.newBuilder().setSize(2).build());
    }

    public Flowable<FileDownloadResponse> transferDataNewServer(Flowable<inc.TransferDataNewServerRequest> request) {
        ArrayList ar= new ArrayList();
        ar.add("senra_2.jpg");
        ar.add("senra_3.jpg");
        ar.add("senra_4.jpg");
        return request.flatMap(res-> aux1(ar));
    }


    public Flowable<FileDownloadResponse> aux(String filename) {
        //System.out.println("resposta ");
        FileInputStream fis;
        byte[] buffer=new byte[1024];
        try {
            fis = new FileInputStream(filename);
        }
        catch (Exception e) {
            return Flowable.empty();
        }
        return Flowable.generate(emiter->{
            int bytesRead;
            try {
                bytesRead = fis.read(buffer);
                System.out.println(buffer);
            if ( bytesRead!=-1) {
                FileDownloadResponse msg= FileDownloadResponse.newBuilder().setChunk(ByteString.copyFrom(buffer,0,bytesRead)).build();
                emiter.onNext(msg);
            }
            else {
                emiter.onComplete();
            }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public Flowable<FileDownloadResponse> aux1(List<String> filenames) {
        return Flowable.fromIterable(filenames)
                .flatMap(filename -> {
                    final int[] idx = {1};
                    try {
                        FileInputStream fis = new FileInputStream(filename);
                        return Flowable.generate(emitter -> {
                            byte[] buffer = new byte[1024];
                            int bytesRead = fis.read(buffer);
                            if (bytesRead != -1) {
                                //out.println(idx[0]);
                                FileDownloadResponse msg = FileDownloadResponse.newBuilder().setFileName(filename).setChunk(ByteString.copyFrom(buffer, 0, bytesRead)).setSeqNum(idx[0]).build();
                                idx[0]++;
                                emitter.onNext(msg);
                            } else {
                                out.println("completei");
                                emitter.onComplete();
                                fis.close();
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("Error reading file: " + filename + ": " + e.getMessage());
                        return Flowable.empty();
                    }
                });
    }

    public Flowable<FileDownloadResponse> download(Flowable<inc.FileDownloadRequest> request) {
        return request.flatMap(res-> aux(res.getFileName()));
    }

    public int guardar_file( String filename, byte[] data ) {
        //out.println("entrei");
        if (!(this.files.containsKey(filename))) {
            this.files.put(filename, 1);
        }
        File file = new File(filename);
        if (file.exists()) {
            //out.println("ola");
        }
        else {
            out.println("Algo de errado aconteceu!");
        }
        try {
            // Verifica se o arquivo existe
            if (!(file.exists())) {
                if (file.createNewFile()) {
                    out.println("File created: " + file.getName());
                }
            }
            // Escreve bytes no arquivo
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                fos.write(data);
                //out.println("Dados adicionados com sucesso ao arquivo.");
            } catch (IOException e) {
                System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar o arquivo: " + e.getMessage());
        }
        return 1;
    }
}
