package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.util.Arrays;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {

    private byte[] bytes = new byte[1<<10];
    private int len = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte) {

        if (nextByte != 0 || len == 0){
            pushByte(nextByte);
            return null;
        }

        // meaning that we finish reading the message
        return createOutput();
        
    }

    @Override
    public byte[] encode(byte[] message) {
        byte[] output = new byte[message.length + 1];
        output[output.length - 1] = 0;
        for (int i = 0; i < output.length - 1; i++)
            output[i] = message[i];
        return output;
    }

    // Added
    private void pushByte(byte nextByte){
        if (len >= bytes.length)
            bytes = Arrays.copyOf(bytes, len*2);
        bytes[len] = nextByte;
        len++;
    }

    private byte[] createOutput(){
        byte[] output = new byte[len];
        for (int i = 0; i < len; i++){
            output[i] = bytes[i];
        }
        len = 0;
        return output;
    }
}