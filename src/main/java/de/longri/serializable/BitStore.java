package de.longri.serializable;

import java.util.ArrayList;
import java.util.BitSet;


/**
 * Created by Longri on 05.11.15.
 */
public class BitStore extends StoreBase {


    private final static int MASK_8bit = 0xFF;
    private final static int MASK_16bit = 0xFFFF;

    private final static int STATE_BIT_COUNT_BYTE = 3;
    private final static int STATE_BIT_COUNT_SHORT = 4;

    private final static int STATE_BIT_MASK_BYTE = 7;
    private final static int STATE_BIT_MASK_SHORT = 15;

    private short SHORT_MASK;
    private int INT_MASK;

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
        short buffervalue = (short) (((getBufferByte(pointer._Byte) & MASK_8bit) << 8) | (getBufferByte(pointer._Byte + 1) & MASK_8bit));


        //SHORT_MASK for 3bits state & count
        SHORT_MASK = 1;
        for (int i = 1; i < count + STATE_BIT_COUNT_BYTE; i++) SHORT_MASK = (short) ((SHORT_MASK << 1) + 1);

        //shift the twoBytes to the right pointer
        twoBytes = (short) ((short) (twoBytes << ((16 - (STATE_BIT_COUNT_BYTE + count)) - pointer._Bit)));

        //add twoBytes with Buffer
        SHORT_MASK = 1;
        for (int i = 1; i < 16 - pointer._Bit; i++) SHORT_MASK = (short) ((SHORT_MASK << 1) + 1);


        twoBytes = (short) (twoBytes & SHORT_MASK | buffervalue);

        //write back to Buffer
        setBufferByte(pointer._Byte, (byte) (twoBytes >> 8));
        setBufferByte(pointer._Byte + 1, (byte) (twoBytes));


        //move Pointer
        movePointer(count + STATE_BIT_COUNT_BYTE);

    }


    @Override
    protected void _write(short s) throws NotImplementedException {
        int fourBytes = 0;

        //get index of first HIGH bit
        BitSet set = BitSet.valueOf(new byte[]{(byte) (s), (byte) (s >> 8)});
        int index = set.previousSetBit(15);

        //calc bit count
        int count = index + 1;

        //write count bits
        fourBytes = (int) count;

        //shift to have place for bit's
        fourBytes = fourBytes << count;

        //write bit's
        fourBytes = fourBytes | s & MASK_16bit;

        //get for Bytes from buffer and put thier into a Short
        int buffervalue = getBufferByte(pointer._Byte) << 24 | (getBufferByte(pointer._Byte + 1) & 0xff) << 16
                | (getBufferByte(pointer._Byte + 2) & 0xff) << 8 | (getBufferByte(pointer._Byte + 3) & 0xff);


        //SHORT_MASK for 4bits state & count
        INT_MASK = 1;
        for (int i = 1; i < count + STATE_BIT_COUNT_SHORT; i++) INT_MASK = ((INT_MASK << 1) + 1);

        //shift the twoBytes to the right pointer
        fourBytes = ((fourBytes << ((32 - (STATE_BIT_COUNT_SHORT + count)) - pointer._Bit)));

        //add twoBytes with Buffer
        INT_MASK = 1;
        for (int i = 1; i < 32 - pointer._Bit; i++) INT_MASK = ((INT_MASK << 1) + 1);


        fourBytes = ((fourBytes & INT_MASK) | buffervalue);

        //write back to Buffer
        setBufferByte(pointer._Byte, (byte) (fourBytes >> 24));
        setBufferByte(pointer._Byte + 1, (byte) (fourBytes >> 16));
        setBufferByte(pointer._Byte + 2, (byte) (fourBytes >> 8));
        setBufferByte(pointer._Byte + 3, (byte) fourBytes);

        //move Pointer
        movePointer(count + STATE_BIT_COUNT_SHORT);
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
        short buffervalue = (short) (((getBufferByte(pointer._Byte) & MASK_8bit) << 8) | (getBufferByte(pointer._Byte + 1) & MASK_8bit));

        //shift left for pointer
        buffervalue = (short) (buffervalue << pointer._Bit);

        //read first three bits for count
        int count = ((byte) (buffervalue >> 13) & STATE_BIT_MASK_BYTE);

        if (count == 0) count = 8;

        //shift out three count bits
        buffervalue = (short) (buffervalue << STATE_BIT_COUNT_BYTE);

        //create Mask
        int mask = 1;
        for (int i = 1; i < count; i++) mask = (mask << 1) + 1;

        //shift right to the first pos
        buffervalue = (short) (buffervalue >> 8 + (8 - count) & mask);

        byte ret = (byte) buffervalue;

        //move pointer
        movePointer(count + STATE_BIT_COUNT_BYTE);

        return ret;

    }

    @Override
    public short readShort() throws NotImplementedException {
        //copy four bytes from Buffer
        int buffervalue = getBufferByte(pointer._Byte) << 24 | (getBufferByte(pointer._Byte + 1) & 0xff) << 16
                | (getBufferByte(pointer._Byte + 2) & 0xff) << 8 | (getBufferByte(pointer._Byte + 3) & 0xff);


        //shift left for pointer
        buffervalue = (buffervalue << pointer._Bit);


        //read four bits for count
        int count = ((byte) (buffervalue >> 28) & STATE_BIT_MASK_SHORT);

        if (count == 0) count = 16;

        //shift out three count bits
        buffervalue = (buffervalue << STATE_BIT_COUNT_SHORT);

        //create Mask
        int mask = 1;
        for (int i = 1; i < count; i++) mask = (mask << 1) + 1;

        //shift right to the first pos
        buffervalue = (buffervalue >> 32 + (32 - count) & mask);

        short ret = (short) buffervalue;

        //move pointer
        movePointer(count + STATE_BIT_COUNT_SHORT);

        return ret;

    }

    private byte getBufferByte(int byteIndex) {
        if (byteIndex >= buffer.length) return 0;
        return buffer[byteIndex];
    }

    private void setBufferByte(int byteIndex, byte _byte) {
        if (byteIndex >= buffer.length) return;
        buffer[byteIndex] = _byte;
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
