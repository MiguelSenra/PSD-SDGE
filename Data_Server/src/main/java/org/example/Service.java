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

    private Map<ZoneLimits, Map<String, String>> filesByLimits;

    public Service(ArrayList<ZoneLimits> limits) {
        super();
        this.filesByLimits = new HashMap<>();
        for (ZoneLimits limit : limits) {
            filesByLimits.put(limit, new HashMap<>());
        }
    }

    private ZoneLimits keyInDomain(String key) {
        for (ZoneLimits z : filesByLimits.keySet()) {
            if (z.keyInInterval(key))
                return z;
        }
        return null;
    }

    private void newLimits(String key) {
        for (ZoneLimits z : filesByLimits.keySet()) {
            if (z.keyInInterval(key)) {
                z.setStartHash(key);
            }
        }
    }

    private ArrayList<String> ListFilesToTransferNewServer(String hash) {
        ArrayList<String> filesToTransfer = new ArrayList<>();
        // Procura pelos limites que contêm o hash fornecido
        //out.println();
        ZoneLimits zoneLimits = keyInDomain(hash);

        // Se encontrou os limites correspondentes
        if (zoneLimits != null) {
            // Obtém o mapa de arquivos para esses limites
            Map<String, String> filesMap = filesByLimits.get(zoneLimits);

            // Se o mapa de arquivos não for nulo
            if (filesMap != null) {
                // Converte o mapa de arquivos para uma lista de FileHash
                ArrayList<FileHash> filesInLimits = new ArrayList<>();
                for (Map.Entry<String, String> entry : filesMap.entrySet()) {
                    filesInLimits.add(new FileHash(entry.getKey(), entry.getValue()));
                }

                // Ordena os arquivos com base no comprimento do nome do arquivo
                filesInLimits.sort(Comparator.comparingInt(fh -> fh.getFileName().length()));

                // Adiciona os nomes dos arquivos à lista de arquivos para transferência
                for (FileHash fileHash : filesInLimits) {
                    filesToTransfer.add(fileHash.getFileName());
                }
            }
        }
        System.out.println("Os ficheiros a transferir são: " + filesToTransfer.toString());
        return filesToTransfer;
    }

    public Flowable<FileUploadResponse> upload(Flowable<FileUploadRequest> request) {
        return request.map(requestMessage -> {
                    String campo1 = requestMessage.getFileName();
                    byte[] campo2 = requestMessage.getChunk().toByteArray();
                    String ssh_key = requestMessage.getSsaKey();
                    ZoneLimits zone= keyInDomain(ssh_key);
                    //out.println("ola");
                    //out.println(zone);
                    if (!(filesByLimits.get(zone).containsKey(ssh_key))) {
                        filesByLimits.get(zone).put(ssh_key, campo1);
                    }
                    return guardar_file(campo1, campo2);
                })
                .map(n -> FileUploadResponse.newBuilder().setSize(2).build());
    }

    public Flowable<FileDownloadResponse> transferDataNewServer(Flowable<inc.TransferDataNewServerRequest> request) {
        out.println("TransferDataNewServerRequest");
        return request.flatMap(res-> {
            ArrayList<String> ar= ListFilesToTransferNewServer(res.getSsaKey());
            newLimits(res.getSsaKey());
            out.println(ar.toString());
            return aux1(ar);
            });
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
                    out.println(filename);
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
        File file = new File(filename);
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
