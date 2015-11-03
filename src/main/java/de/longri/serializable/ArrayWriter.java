package de.longri.serializable;

import de.longri.serializable.mapsforge.Serializer;

import java.util.ArrayList;

/**
 * Created by Longri on 03.11.15.
 */
public class ArrayWriter {

    private ArrayList<Byte> mByteArray = new ArrayList<Byte>();

    public ArrayWriter() {
    }

    public void writeByte(byte value){
        mByteArray.add(value);
    }

    public void writeByte(BooleanStore booleanStore) {
        mByteArray.add(booleanStore.getByte());
    }

    public void writeInt(int value) {
        add(Serializer.getBytes(value));
    }


    public void writeVariableUnsignedInt(int value) {
        add(Serializer.getVariableByteUnsigned(value));
    }

    public void writeVariableSignedInt(int value) {
        add(Serializer.getVariableByteSigned(value));
    }

    private void add(byte[] bytes) {
        for (byte b : bytes) {
            mByteArray.add(b);
        }
    }


    public byte[] getArray() {

        byte[] array = new byte[mByteArray.size()];

        int index = 0;
        for (Byte b : mByteArray) {
            array[index++] = b;
        }

        return array;
    }



}
