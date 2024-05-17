package org.example;

import com.ericsson.otp.erlang.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ErlangSerialization {
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
