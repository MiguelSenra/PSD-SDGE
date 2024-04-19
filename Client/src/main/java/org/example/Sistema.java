package org.example;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import com.ericsson.otp.erlang.*;



public class Sistema {
    private SocketChannel ss;

    public Sistema(int portNumber) {
        try {
            this.ss = SocketChannel.open(new InetSocketAddress(portNumber));
        }
        catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    public void registar(String username, String password) {
        //new ErlangMessage("register", "username", "password")
        try {
            OtpErlangObject[] tuple = new OtpErlangObject[] {
                    new OtpErlangAtom("register"),
                    new OtpErlangTuple(new OtpErlangObject[] {
                            new OtpErlangString(username),
                            new OtpErlangString(password),
                    })
            };
        OtpErlangTuple message = new OtpErlangTuple(tuple);
            System.err.println("AQUIIIIIIIIIIII " );
            ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));
            ss.write(bb);
        } catch (Exception e) {
            System.out.println("Não consegui escrever no socket");
        }
    }

    public static byte[] tupleToBytes(OtpErlangTuple tuple) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OtpOutputStream otpOutputStream = new OtpOutputStream(outputStream.size());
        // Adicionar a tag 131 manualmente
        otpOutputStream.write1(OtpExternal.versionTag);
        tuple.encode(otpOutputStream);
        return otpOutputStream.toByteArray();
    }
}
