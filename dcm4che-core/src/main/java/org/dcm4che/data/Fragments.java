package org.dcm4che.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Fragments extends ArrayList<byte[]> {

    private static final long serialVersionUID = -6667210062541083610L;

    private final VR vr;
    private final boolean bigEndian;

    Fragments(VR vr, boolean bigEndian, int initialCapacity) {
        super(initialCapacity);
        this.vr = vr;
        this.bigEndian = bigEndian;
    }

    public final VR vr() {
        return vr;
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    public ByteBuffer getByteBuffer(int index) {
        return ByteBuffer.wrap(get(index))
                .order(bigEndian ? ByteOrder.BIG_ENDIAN
                                  : ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public String toString() {
        return "" + size() + " Fragments";
    }
}
