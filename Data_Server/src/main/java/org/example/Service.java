package org.example;

import com.google.protobuf.ByteString;
import inc.FileDownloadResponse;
import inc.FileUploadRequest;
import inc.FileUploadResponse;
import inc.RemoveFileResponse;
import inc.Rx3FileServiceGrpc;
import io.reactivex.rxjava3.core.Flowable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.System.out;

public class Service extends Rx3FileServiceGrpc.FileServiceImplBase {

    private Map<ZoneLimits,ArrayList<String>> filesByLimits;

    public Service(ArrayList<ZoneLimits> limits) {
        super();
        this.filesByLimits = new HashMap<>();
        for (ZoneLimits limit : limits) {
            filesByLimits.put(limit, new ArrayList<String>());
        }
    }

    private ZoneLimits keyInDomain(String key) {
        ZoneLimits zoneLimits = null;
        for (ZoneLimits z : filesByLimits.keySet()) {
            if (z.keyInInterval(key))
                zoneLimits= z;
        }
        return zoneLimits;
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
        ZoneLimits zoneLimits = keyInDomain(hash);

        // Se encontrou os limites correspondentes
        if (zoneLimits != null) {
            // Obtém o mapa de arquivos para esses limites
            ArrayList<String> files = filesByLimits.get(zoneLimits);

            // Se o mapa de arquivos não for nulo
            if (files != null) {
                // Adiciona os nomes dos arquivos à lista de arquivos para transferência
                for (String fileHash : files) {
                    int val= fileHash.compareTo(hash);
                    if (val<0)
                        filesToTransfer.add(fileHash);
                }
            }
        }
        System.out.println("Os ficheiros a transferir são: " + filesToTransfer.toString());
        return filesToTransfer;
    }

    public Flowable<FileUploadResponse> upload(Flowable<FileUploadRequest> request) {
        final boolean[] error = {false};
        final boolean[] checkFile= {false};
        final boolean[] fileExists= {false};
        return request.flatMap(requestMessage -> {
            byte[] campo2 = requestMessage.getChunk().toByteArray();
            String ssh_key = requestMessage.getSsaKey();
            ZoneLimits zone= keyInDomain(ssh_key);

            if (zone==null) {
                if (error[0])
                    return Flowable.empty();
                else {
                    error[0] = true;
                    return Flowable.just(FileUploadResponse.newBuilder().setSize(-1).setMessage("No autorization").build());
                }
            }
            else {
                //boolean OK = true;
                if (!checkFile[0]) {
                    if (!filesByLimits.get(zone).contains(ssh_key)) {
                        filesByLimits.get(zone).add(ssh_key);
                        checkFile[0] = true;
                    }
                    else {
                        fileExists[0] = true;
                        checkFile[0] = true;
                        return Flowable.just(FileUploadResponse.newBuilder().setSize(-1).setMessage("File exists").build());
                    }
                }

                if (fileExists[0])
                    return Flowable.empty();
                else {
                    int val=guardar_file(ssh_key, campo2);
                    return Flowable.just(FileUploadResponse.newBuilder().setSize(val).setMessage("Packet uploaded").build());
                }
            }
        });
    }

    public Flowable<FileDownloadResponse> transferDataNewServer(Flowable<inc.TransferDataNewServerRequest> request) {
        return request.flatMap(res -> {
            ArrayList<String> ar = ListFilesToTransferNewServer(res.getSsaKey());
            newLimits(res.getSsaKey());
            return auxNewServer(ar);
        });
    }



    public Flowable<FileDownloadResponse> aux_download(String hash) {
        //System.out.println("resposta ");
        FileInputStream fis;
        byte[] buffer=new byte[1024];
        try {
            fis = new FileInputStream(hash);
        }
        catch (Exception e) {
            out.println(e.getMessage());
            return Flowable.empty();
        }
        ZoneLimits zone= keyInDomain(hash);
        if (zone==null) {
            return Flowable.just(FileDownloadResponse.newBuilder().setMessage("No autorization").build());
        }
        else {
            return Flowable.generate(emiter -> {
                int bytesRead;
                try {
                    bytesRead = fis.read(buffer);
                    //System.out.println(buffer);
                    if (bytesRead != -1) {
                        FileDownloadResponse msg = FileDownloadResponse.newBuilder().setChunk(ByteString.copyFrom(buffer, 0, bytesRead)).build();
                        emiter.onNext(msg);
                        //emiter.onNext(msg);
                    } else {
                        emiter.onComplete();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }
    }

    public Flowable<FileDownloadResponse> auxNewServer(List<String> filenames) {
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
                            FileDownloadResponse msg = FileDownloadResponse.newBuilder().setSsaKey(filename).setChunk(ByteString.copyFrom(buffer, 0, bytesRead)).setSeqNum(idx[0]).build();
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
        return request.flatMap(res-> aux_download(res.getSsaKey()));
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
            } catch (IOException e) {
                System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar o arquivo: " + e.getMessage());
        }
        return (int) file.length();
    }

    public Flowable<inc.RemoveFileResponse> removeFile(Flowable<inc.RemoveFileRequest> request) {
        return request.map(req->{
            try {
                ZoneLimits zone= keyInDomain(req.getHash());
                if (zone==null) {
                    return RemoveFileResponse.newBuilder().setMessage("No autorization").build();
                }
                else {
                    filesByLimits.get(zone).remove(req.getHash());
                    Path path = Paths.get( req.getHash());
                    Files.delete(path);
                    return RemoveFileResponse.newBuilder().setMessage("File removed").build();
                }
            } catch (IOException e) {
                return RemoveFileResponse.newBuilder().setMessage("Não foi possível apagar o ficheiro").build();
            }
        });
    }
}
