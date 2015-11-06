package de.longri.serializable;

import java.util.ArrayList;
import java.util.BitSet;


/**
 * Created by Longri on 05.11.15.
 */
public class BitStore extends StoreBase {


    private final static int MASK_8bit = 0xFF;

    private short MASK;

    private class Pointer {
        int _Byte = 0;
        int _Bit = 0;
    }


    private Pointer pointer = new Pointer();

    private void movePointer(int i) {

        long allBit = pointer._Byte * 8 + pointer._Bit + i;

        pointer._Byte = (int) (allBit / 8);
        pointer._Bit = (int) (allBit % 8);

        size = pointer._Byte + 1;

    }

    public BitStore(byte[] array) {
        super(array);
    }

    public BitStore() {
        super();
    }

    @Override
    protected void _write(byte b) throws NotImplementedException {

        short twoBytes = 0;

        //get index of first HIGH bit
        BitSet set = BitSet.valueOf(new byte[]{b});
        int index = set.previousSetBit(7);

        //calc bit count
        int count = index + 1;

        //write count bits
        twoBytes = (short) count;

        //shift to have place for bit's
        twoBytes = (short) (twoBytes << count);

        //write bit's
        twoBytes = (short) (twoBytes | (short) b & MASK_8bit);


        //get two Bytes from buffer and put thier into a Short
        short buffervalue = (short) (((buffer[pointer._Byte] & MASK_8bit) << 8) | (buffer[pointer._Byte + 1] & MASK_8bit));


        //MASK for 3bits state & count
        MASK = 1;
        for (int i = 1; i < count + 3; i++) MASK = (short) ((MASK << 1) + 1);

        //shift the twoBytes to the right pointer
        twoBytes = (short) ((short) (twoBytes << ((16 - (3 + count)) - pointer._Bit)));

        //add twoBytes with Buffer
        MASK = 1;
        for (int i = 1; i < 16 - pointer._Bit; i++) MASK = (short) ((MASK << 1) + 1);


        twoBytes = (short) (twoBytes & MASK | buffervalue);

        //write back to Buffer
        buffer[pointer._Byte] = (byte) (twoBytes >> 8);
        buffer[pointer._Byte + 1] = (byte) (twoBytes);


        //move Pointer
        movePointer(count + 3);

    }


    @Override
    protected void _write(short s) throws NotImplementedException {
        throw new NotImplementedException("Write Short not implemented from \"BitStore\"");
    }

    @Override
    protected void _write(int i) throws NotImplementedException {
        throw new NotImplementedException("Write Integer not implemented from \"BitStore\"");
    }

    @Override
    protected void _write(long l) throws NotImplementedException {
        throw new NotImplementedException("Write Long not implemented from \"BitStore\"");
    }

    @Override
    protected void _write(String s) throws NotImplementedException {
        throw new NotImplementedException("Write String not implemented from \"BitStore\"");
    }

    @Override
    public byte readByte() throws NotImplementedException {

        //copy two bytes from Buffer
        short buffervalue = (short) (((buffer[pointer._Byte] & MASK_8bit) << 8) | (buffer[pointer._Byte + 1] & MASK_8bit));

        //shift left for pointer
        buffervalue = (short) (buffervalue << pointer._Bit);

        //read first three bits for count
        int count = ((byte) (buffervalue >> 13) & 7);

        if (count == 0) count = 8;

        //shift out three count bits
        buffervalue = (short) (buffervalue << 3);

        //create Mask
        int mask = 1;
        for (int i = 1; i < count; i++) mask = (mask << 1) + 1;

        //shift right to the first pos
        buffervalue = (short) (buffervalue >> 8 + (8 - count) & mask);

        byte ret = (byte) buffervalue;

        //move pointer
        movePointer(count + 3);

        return ret;

    }

    @Override
    public short readShort() throws NotImplementedException {
        throw new NotImplementedException("Read Short not implemented from \"BitStore\"");
    }

    @Override
    public int readInt() throws NotImplementedException {
        throw new NotImplementedException("Read Integer not implemented from \"BitStore\"");
    }

    @Override
    public long readLong() throws NotImplementedException {
        throw new NotImplementedException("Read Long not implemented from \"BitStore\"");
    }

    @Override
    public String readString() throws NotImplementedException {
        throw new NotImplementedException("Read String not implemented from \"BitStore\"");
    }

    @Override
    public <T extends Serializable> ArrayList<T> readList(Class<T> tClass) throws NotImplementedException {
        throw new NotImplementedException("Read List not implemented from \"BitStore\"");
    }
}
