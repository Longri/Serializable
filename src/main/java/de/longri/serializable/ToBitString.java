package de.longri.serializable;

/**
 * Created by Longri on 06.11.15.
 */
public class ToBitString {

    byte[] value = new byte[4];

    private short mValue;

    ToBitString(byte[] b) {
        value = b;
    }

    ToBitString(byte b) {
        value[3] = b;
    }

    ToBitString(short s) {
        byte[] b = new byte[]{(byte) (s >> 8), (byte) s};
        value[2] = b[0];
        value[3] = b[1];
    }

    ToBitString(int i) {
        byte[] b = new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
        value[0] = b[0];
        value[1] = b[1];
        value[2] = b[2];
        value[3] = b[3];
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        byte[] masks = {-128, 64, 32, 16, 8, 4, 2, 1};


        int writtenByte = 0;
        for (byte write : value) {
            int index = 0;
            for (short m : masks) {
                if (index++ == 4) builder.append(' ');
                if ((write & m) == m) {
                    builder.append('1');
                } else {
                    builder.append('0');
                }
            }
            if (writtenByte++ < value.length - 1) builder.append(" | ");
        }
        return builder.toString();
    }
}
