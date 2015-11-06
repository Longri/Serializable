package de.longri.serializable;

/**
 * Created by Longri on 06.11.15.
 */
public class ToBitString {

    byte[] value = new byte[4];

    private short mValue;

    ToBitString(byte b) {
        value[3] = b;
    }

    ToBitString(short s) {
        value[3] = (byte) (s >> 8);
        value[3] = (byte) (8);
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
            if (writtenByte++ < 4) builder.append(" | ");
        }
        return builder.toString();
    }
}
