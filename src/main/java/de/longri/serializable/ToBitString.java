package de.longri.serializable;


import java.util.ArrayList;

/**
 * Created by Longri on 06.11.15.
 */
public class ToBitString {

    byte[] value = new byte[4];

    private short mValue;

    ToBitString(int[] val) {

        ArrayList<Byte> intByteArray = new ArrayList<Byte>();

        for (int i : val) {
            intByteArray.add((byte) (i >> 24));
            intByteArray.add((byte) (i >> 16));
            intByteArray.add((byte) (i >> 8));
            intByteArray.add((byte) (i));
        }

        byte[] bytes = new byte[intByteArray.size()];

        int index = 0;
        for (byte b : intByteArray) {
            bytes[index++] = b;
        }

        value = bytes;
    }


    ToBitString(byte[] b) {
        value = b;
    }

    ToBitString(byte b) {
        value[3] = b;
    }

    ToBitString(short s) {
        value = new byte[]{(byte) (s >> 8), (byte) s};

    }

    ToBitString(int i) {
        value = new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
    }

    ToBitString(long l) {
        value = new byte[]{(byte) (l >> 56), (byte) (l >> 48), (byte) (l >> 40), (byte) (l >> 32),
                (byte) (l >> 24), (byte) (l >> 16), (byte) (l >> 8), (byte) l};
    }

    ToBitString(ByteArray big) {
        value = big.toByteArray();
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
