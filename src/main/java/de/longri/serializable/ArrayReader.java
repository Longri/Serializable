package de.longri.serializable;

import java.util.ArrayList;

/**
 * Created by Longri on 03.11.15.
 */
public class ArrayReader {

    private final byte[] mArray;
    private int readOffset = 0;

    public ArrayReader(byte[] array) {
        this.mArray = array;
    }

    public byte readByte() {
        return mArray[readOffset++];
    }


    public int readInt() {
        return mArray[readOffset++] << 24 | (mArray[readOffset++] & 0xff) << 16 | (mArray[readOffset++] & 0xff) << 8
                | (mArray[readOffset++] & 0xff);
    }

    public int readVariableUnsignedInt() {
        int variableByteDecode = 0;
        byte variableByteShift = 0;

        // check if the continuation bit is set
        while ((this.mArray[this.readOffset] & 0x80) != 0) {
            variableByteDecode |= (this.mArray[this.readOffset++] & 0x7f) << variableByteShift;
            variableByteShift += 7;
        }

        // read the seven data bits from the last byte
        return variableByteDecode | (this.mArray[this.readOffset++] << variableByteShift);
    }

    public int readVariableSignedInt() {
        int variableByteDecode = 0;
        byte variableByteShift = 0;

        // check if the continuation bit is set
        while ((this.mArray[this.readOffset] & 0x80) != 0) {
            variableByteDecode |= (this.mArray[this.readOffset++] & 0x7f) << variableByteShift;
            variableByteShift += 7;
        }

        // read the six data bits from the last byte
        if ((this.mArray[this.readOffset] & 0x40) != 0) {
            // negative
            return -(variableByteDecode | ((this.mArray[this.readOffset++] & 0x3f) << variableByteShift));
        }
        // positive
        return variableByteDecode | ((this.mArray[this.readOffset++] & 0x3f) << variableByteShift);
    }

    public <T extends Serializable> ArrayList<T> readList(Class<T> tClass) {
        ArrayList<T> list = new ArrayList<T>();

        int size = readVariableUnsignedInt();

        for (int i = 0; i < size; i++) {

            try {
                T t = tClass.newInstance();
                t.deserialize(this);
                list.add(t);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return list;
    }

    public long readLong() {
        return (mArray[readOffset++] & 0xffL) << 56 | (mArray[readOffset++] & 0xffL) << 48 | (mArray[readOffset++] & 0xffL) << 40
                | (mArray[readOffset++] & 0xffL) << 32 | (mArray[readOffset++] & 0xffL) << 24
                | (mArray[readOffset++] & 0xffL) << 16 | (mArray[readOffset++] & 0xffL) << 8 | (mArray[readOffset++] & 0xffL);
    }
}
