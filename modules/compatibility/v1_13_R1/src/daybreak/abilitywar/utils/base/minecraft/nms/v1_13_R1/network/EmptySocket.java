package daybreak.abilitywar.utils.base.minecraft.nms.v1_13_R1.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class EmptySocket extends Socket {
    private static final byte[] EMPTY = new byte[50];

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(EMPTY);
    }

    @Override
    public OutputStream getOutputStream() {
        return new ByteArrayOutputStream(10);
    }
}