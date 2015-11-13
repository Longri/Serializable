package de.longri.serializable;

import java.util.ArrayList;
import java.util.BitSet;


/**
 * Created by Longri on 05.11.15.
 */
public class BitStore extends StoreBase {


    private final static int MASK_8bit = 0xFF;
    private final static int MASK_16bit = 0xFFFF;
    private final static long MASK_32bit = 0xFFFFFFFF;
    private final static long MASK_64bit = 0xFFFFFFFFFFFFFFFFL;

    private final static int STATE_BIT_COUNT_BYTE = 3;
    private final static int STATE_BIT_COUNT_SHORT = 4;
    private final static int STATE_BIT_COUNT_INTEGER = 5;
    private final static int STATE_BIT_COUNT_LONG = 6;

    private final static int STATE_BIT_MASK_BYTE = 7;
    private final static int STATE_BIT_MASK_SHORT = 15;
    private final static int STATE_BIT_MASK_INTEGER = 31;

    
    final static Number LONG = new Number(6, 64, Long.MIN_VALUE);
    final static Number INTEGER = new Number(5, 32, Integer.MIN_VALUE);
    final static Number SHORT = new Number(4, 16, Short.MIN_VALUE);
    final static Number BYTE = new Number(3, 8, Byte.MIN_VALUE);

    public enum Bitmask {
        BIT_0((byte) (1 << 0)), BIT_1((byte) (1 << 1)), BIT_2((byte) (1 << 2)), BIT_3((byte) (1 << 3)),
        BIT_4((byte) (1 << 4)), BIT_5((byte) (1 << 5)), BIT_6((byte) (1 << 6)), BIT_7((byte) (1 << 7));
        private int value;

        Bitmask(byte value) {
            this.value = value;
        }
    }

    private class Pointer {
        int _Byte = 0;
        int _Bit = 0;
    }

    private Pointer pointer = new Pointer();

    private void movePointer(int i) {
        if (pointer._Bit + i > 7) {
            long allBit = pointer._Byte * 8 + pointer._Bit + i;
            pointer._Byte = (int) (allBit / 8);
            pointer._Bit = (int) (allBit % 8);
        } else {
            pointer._Bit += i;
        }
        size = pointer._Byte + 1;
    }

    public BitStore(byte[] array) {
        super(array);
    }

    public BitStore() {
        super();
    }

    @Override
    protected void _write(boolean b) throws NotImplementedException {
        if (b) {
            buffer[pointer._Byte] |= getBitmask().value;
        } else {
            buffer[pointer._Byte] &= ~getBitmask().value;
        }
        movePointer(1);
    }


    @Override
    protected void _write(byte b) throws NotImplementedException {

        // write one bit vor negative/positive value
        if (b < 0) {
            write(true);
            b = (byte) -b;
        } else write(false);


        short twoBytes = 0;
        int count = 0;

        if (b < 0) {
            count = 8;
        } else if (b == 0) {
            count = 1;
        } else {
            //get index of first HIGH bit
            BitSet set = BitSet.valueOf(new byte[]{b});
            int index = set.previousSetBit(7);

            //calc bit count
            count = index + 1;
        }


        //write count bits
        twoBytes = (short) (count & STATE_BIT_MASK_BYTE);

        //shift to have place for bit's
        twoBytes = (short) (twoBytes << count);

        //write bit's
        twoBytes = (short) (twoBytes | ((short) (b & MASK_8bit)));

        //get two Bytes from buffer and put they into a Short
        short bufferValue = (short) (((getBufferByte(pointer._Byte) & MASK_8bit) << 8) | (getBufferByte(pointer._Byte + 1) & MASK_8bit));

        //shift the twoBytes to the right pointer
        twoBytes = (short) ((short) (twoBytes << ((16 - (STATE_BIT_COUNT_BYTE + count)) - pointer._Bit)));

        twoBytes = (short) (twoBytes | bufferValue);

        //write back to Buffer
        setBufferByte(pointer._Byte, (byte) (twoBytes >> 8));
        setBufferByte(pointer._Byte + 1, (byte) (twoBytes));

        //move Pointer
        movePointer(count + STATE_BIT_COUNT_BYTE + 1);

    }


    @Override
    protected void _write(short s) throws NotImplementedException {

        // write one bit vor negative/positive value
        if (s < 0) {
            write(true);
            s = (short) -s;
        } else write(false);

        int fourBytes = 0;

        short v = s;
        int count = 1;
        while (true) {
            v = (short) (v >>> 1);
            if (v == 0 | v == -1) break;
            count++;
        }

        //write count bits
        fourBytes = (int) count;

        //shift to have place for bit's
        fourBytes = fourBytes << count;

        //write bit's
        fourBytes = fourBytes | (s & MASK_16bit);

        //get for Bytes from buffer and put they into a Short
        int bufferValue = getBufferByte(pointer._Byte) << 24 | (getBufferByte(pointer._Byte + 1) & 0xff) << 16
                | (getBufferByte(pointer._Byte + 2) & 0xff) << 8 | (getBufferByte(pointer._Byte + 3) & 0xff);

        //shift the twoBytes to the right pointer
        fourBytes = ((fourBytes << ((32 - (STATE_BIT_COUNT_SHORT + count)) - pointer._Bit)));

        fourBytes = (fourBytes | bufferValue);

        //write back to Buffer
        setBufferByte(pointer._Byte, (byte) (fourBytes >> 24));
        setBufferByte(pointer._Byte + 1, (byte) (fourBytes >> 16));
        setBufferByte(pointer._Byte + 2, (byte) (fourBytes >> 8));
        setBufferByte(pointer._Byte + 3, (byte) fourBytes);

        //move Pointer
        movePointer(count + STATE_BIT_COUNT_SHORT + 1);
    }

    @Override
    protected void _write(int i) throws NotImplementedException {

        if (false) {
            boolean negative = false;
            if (i < 0) {
                negative = true;
                if (i == Integer.MIN_VALUE) i = 0;
                else
                    i = -i;
            }
            byte[] bytes = new byte[]{0, (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
            writeValue(negative, bytes, INTEGER);

        } else {
            // write one bit vor negative/positive value
            if (i < 0) {
                write(true);
                if (i == Integer.MIN_VALUE) i = 0;
                else
                    i = -i;
            } else write(false);

            long eightBytes = 0;

            int v = i;
            int count = 1;
            while (true) {
                v = v >>> 1;
                if (v == 0 | v == -1) break;
                count++;
            }

            //write count bits
            eightBytes = (long) count;

            //shift to have place for bit's
            eightBytes = eightBytes << count;

            //write bit's
            eightBytes = eightBytes | (i & MASK_32bit);

            //get eight Bytes from buffer and put they into a Short
            long bufferValue = (getBufferByte(pointer._Byte) & 0xffL) << 56
                    | (getBufferByte(pointer._Byte + 1) & 0xffL) << 48
                    | (getBufferByte(pointer._Byte + 2) & 0xffL) << 40
                    | (getBufferByte(pointer._Byte + 3) & 0xffL) << 32
                    | (getBufferByte(pointer._Byte + 4) & 0xffL) << 24
                    | (getBufferByte(pointer._Byte + 5) & 0xffL) << 16
                    | (getBufferByte(pointer._Byte + 6) & 0xffL) << 8
                    | (getBufferByte(pointer._Byte + 7) & 0xffL);


            //shift the eightBytes to the right pointer
            eightBytes = ((eightBytes << ((64 - (STATE_BIT_COUNT_INTEGER + count)) - pointer._Bit)));

            eightBytes = (eightBytes | bufferValue);

            //write back to Buffer
            setBufferByte(pointer._Byte, (byte) (eightBytes >> 56));
            setBufferByte(pointer._Byte + 1, (byte) (eightBytes >> 48));
            setBufferByte(pointer._Byte + 2, (byte) (eightBytes >> 40));
            setBufferByte(pointer._Byte + 3, (byte) (eightBytes >> 32));
            setBufferByte(pointer._Byte + 4, (byte) (eightBytes >> 24));
            setBufferByte(pointer._Byte + 5, (byte) (eightBytes >> 16));
            setBufferByte(pointer._Byte + 6, (byte) (eightBytes >> 8));
            setBufferByte(pointer._Byte + 7, (byte) eightBytes);

            //move Pointer
            movePointer(count + STATE_BIT_COUNT_INTEGER + 1);
        }
    }


    @Override
    protected void _write(long l) throws NotImplementedException {
        boolean negative = false;
        if (l < 0) {
            negative = true;
            if (l == Long.MIN_VALUE) l = 0;
            else
                l = -l;
        }

        byte[] bytes = new byte[]{0, (byte) (l >> 56), (byte) (l >> 48), (byte) (l >> 40),
                (byte) (l >> 32), (byte) (l >> 24), (byte) (l >> 16), (byte) (l >> 8), (byte) l};

        writeValue(negative, bytes, LONG);
    }

    private void writeValue(boolean negative, byte[] bytes, Number numberType) throws NotImplementedException {
        // write one bit vor negative/positive value
        write(negative);
        ByteArray nineBytes = new ByteArray(bytes);

        byte count = (byte) nineBytes.bitLength();
        if (count == 0) count = 1;
        if (count >= numberType.bitCount) {
            //don't write, only move Pointer
            movePointer(numberType.pointerMove);
        } else {
            //write count bits
            if (numberType.pointerMove > 5) write((count & Bitmask.BIT_5.value) == Bitmask.BIT_5.value);
            if (numberType.pointerMove > 4) write((count & Bitmask.BIT_4.value) == Bitmask.BIT_4.value);
            if (numberType.pointerMove > 3) write((count & Bitmask.BIT_3.value) == Bitmask.BIT_3.value);
            write((count & Bitmask.BIT_2.value) == Bitmask.BIT_2.value);
            write((count & Bitmask.BIT_1.value) == Bitmask.BIT_1.value);
            write((count & Bitmask.BIT_0.value) == Bitmask.BIT_0.value);
        }


        if (nineBytes.longValue() == 0) {
            // we must nothing write, move only Pointer
        } else {
            //shift to the right pointer
            nineBytes.shiftLeft(((numberType.bitCount + 8) - count) - pointer._Bit);

            int byteLengthToWriteBack = (nineBytes.bitLength() / 8) + 1;
            byte[] readBuffer = new byte[byteLengthToWriteBack];
            for (int i = 0; i < byteLengthToWriteBack; i++) {
                readBuffer[i] = getBufferByte(pointer._Byte + i);
            }
            ByteArray bufferValueByteArray = new ByteArray(readBuffer);
            bufferValueByteArray.or(nineBytes);

            byte[] b = bufferValueByteArray.toByteArray();

            //write back to Buffer
            for (int i = 0; i < b.length; i++) {
                setBufferByte(pointer._Byte + i, b[i]);
            }
        }

        //move Pointer
        movePointer(count);
    }


    @Override
    protected void _write(String s) throws NotImplementedException {
        throw new NotImplementedException("Write String not implemented from \"BitStore\"");
    }

    @Override
    public boolean readBool() throws NotImplementedException {
        Bitmask bit = getBitmask();
        boolean ret = (buffer[pointer._Byte] & bit.value) == bit.value;
        movePointer(1);
        return ret;
    }

    @Override
    public byte readByte() throws NotImplementedException {

        // read if Negative value
        boolean isNegative = readBool();

        //copy two bytes from Buffer
        short bufferValue = (short) (((getBufferByte(pointer._Byte) & MASK_8bit) << 8) | (getBufferByte(pointer._Byte + 1) & MASK_8bit));

        //shift left for pointer
        bufferValue = (short) (bufferValue << pointer._Bit);

        //read first three bits for count
        int count = ((byte) (bufferValue >>> 13) & STATE_BIT_MASK_BYTE);

        if (count == 0) count = 8;

        //shift out three count bits
        bufferValue = (short) (bufferValue << STATE_BIT_COUNT_BYTE);

        //create Mask
        int mask = 1;
        for (int i = 1; i < count; i++) mask = (mask << 1) + 1;

        //shift right to the first pos
        bufferValue = (short) (bufferValue >>> 8 + (8 - count) & mask);

        //move pointer
        movePointer(count + STATE_BIT_COUNT_BYTE + 1);

        byte ret = (byte) bufferValue;

        if (isNegative) {
            if (ret == 0) ret = Byte.MIN_VALUE;
            else
                ret = (byte) -ret;
        }

        return ret;
    }

    @Override
    public short readShort() throws NotImplementedException {

        // read if Negative value
        boolean isNegative = readBool();

        //copy four bytes from Buffer
        int bufferValue = getBufferByte(pointer._Byte) << 24
                | (getBufferByte(pointer._Byte + 1) & 0xff) << 16
                | (getBufferByte(pointer._Byte + 2) & 0xff) << 8
                | (getBufferByte(pointer._Byte + 3) & 0xff);

        //shift left for pointer
        bufferValue = (bufferValue << pointer._Bit);

        //read four bits for count
        int count = (byte) ((bufferValue >>> 28) & STATE_BIT_MASK_SHORT);

        if (count == 0) count = 16;

        //shift out three count bits
        bufferValue = (bufferValue << STATE_BIT_COUNT_SHORT);

        //create Mask
        int mask = 1;
        for (int i = 1; i < count; i++) mask = (mask << 1) + 1;

        //shift right to the first pos
        bufferValue = (bufferValue >>> 32 + (32 - count) & mask);


        //move pointer
        movePointer(count + STATE_BIT_COUNT_SHORT + 1);

        short ret = (short) bufferValue;

        if (isNegative) {
            if (ret == 0) ret = Short.MIN_VALUE;
            else
                ret = (short) -ret;
        }

        return ret;
    }

    @Override
    public int readInt() throws NotImplementedException {

        // read if Negative value
        boolean isNegative = readBool();

        //copy eight bytes from Buffer
        long bufferValue = (getBufferByte(pointer._Byte) & 0xffL) << 56
                | (getBufferByte(pointer._Byte + 1) & 0xffL) << 48
                | (getBufferByte(pointer._Byte + 2) & 0xffL) << 40
                | (getBufferByte(pointer._Byte + 3) & 0xffL) << 32
                | (getBufferByte(pointer._Byte + 4) & 0xffL) << 24
                | (getBufferByte(pointer._Byte + 5) & 0xffL) << 16
                | (getBufferByte(pointer._Byte + 6) & 0xffL) << 8
                | (getBufferByte(pointer._Byte + 7) & 0xffL);

        //shift left for pointer
        bufferValue = (bufferValue << pointer._Bit);

        //read four bits for count
        int count = (byte) ((bufferValue >>> 59) & STATE_BIT_MASK_INTEGER);

        if (count == 0) count = 32;

        //shift out three count bits
        bufferValue = (bufferValue << STATE_BIT_COUNT_INTEGER);

        //create Mask
        int mask = 1;
        for (int i = 1; i < count; i++) mask = (mask << 1) + 1;

        //shift right to the first pos
        bufferValue = (bufferValue >> 64 + (64 - count) & mask);

        //move pointer
        movePointer(count + STATE_BIT_COUNT_INTEGER + 1);

        int ret = (int) bufferValue;

        if (isNegative) {
            if (ret == 0) ret = Integer.MIN_VALUE;
            else
                ret = (int) -ret;
        }
        return ret;
    }

    @Override
    public long readLong() throws NotImplementedException {
        return readValue(LONG);
    }

    private long readValue(Number numberType) throws NotImplementedException {
        // read if Negative value
        boolean isNegative = readBool();

        //read count
        byte count = 0;

        int byteBegin = pointer._Byte;
        int bitBegin = pointer._Bit;

        if (numberType.pointerMove > 5) {
            if (readBool()) count |= Bitmask.BIT_5.value;
            else count &= ~Bitmask.BIT_5.value;
        }

        if (numberType.pointerMove > 4) {
            if (readBool()) count |= Bitmask.BIT_4.value;
            else count &= ~Bitmask.BIT_4.value;
        }

        if (numberType.pointerMove > 3) {
            if (readBool()) count |= Bitmask.BIT_3.value;
            else count &= ~Bitmask.BIT_3.value;
        }

        if (readBool()) count |= Bitmask.BIT_2.value;
        else count &= ~Bitmask.BIT_2.value;

        if (readBool()) count |= Bitmask.BIT_1.value;
        else count &= ~Bitmask.BIT_1.value;

        if (readBool()) count |= Bitmask.BIT_0.value;
        else count &= ~Bitmask.BIT_0.value;


        if (count == 0) count = (byte) numberType.bitCount;


        int readByteArrayLength = ((count + numberType.pointerMove) / 8) + 2;
        byte[] ba = new byte[readByteArrayLength];

        for (int i = 0; i < readByteArrayLength; i++) {
            ba[i] = getBufferByte(byteBegin + i);
        }

        ByteArray bufferValueByteArray = new ByteArray(ba);

//shift left for pointer
        bufferValueByteArray.shiftLeft(bitBegin + numberType.pointerMove);

//shift right to the first pos
        bufferValueByteArray.shiftRight(readByteArrayLength * 8 - (count));

        long retValue = bufferValueByteArray.longValue();
        if (isNegative) {
            if (retValue == 0) retValue = numberType.minValue;
            else
                retValue = (long) -retValue;
        }
        //move Pointer
        movePointer(count);
        return retValue;
    }

    @Override
    public String readString() throws NotImplementedException {
        throw new NotImplementedException("Read String not implemented from \"BitStore\"");
    }

    @Override
    public <T extends Serializable> ArrayList<T> readList(Class<T> tClass) throws NotImplementedException {
        throw new NotImplementedException("Read List not implemented from \"BitStore\"");
    }


    private Bitmask getBitmask() {
        Bitmask bit = Bitmask.BIT_7;
        switch (pointer._Bit) {
            case 0:
                bit = Bitmask.BIT_7;
                break;
            case 1:
                bit = Bitmask.BIT_6;
                break;
            case 2:
                bit = Bitmask.BIT_5;
                break;
            case 3:
                bit = Bitmask.BIT_4;
                break;
            case 4:
                bit = Bitmask.BIT_3;
                break;
            case 5:
                bit = Bitmask.BIT_2;
                break;
            case 6:
                bit = Bitmask.BIT_1;
                break;
            case 7:
                bit = Bitmask.BIT_0;
                break;
        }
        return bit;
    }

    private byte getBufferByte(int byteIndex) {
        if (byteIndex >= buffer.length) return 0;
        return buffer[byteIndex];
    }

    private void setBufferByte(int byteIndex, byte _byte) {
        if (byteIndex >= buffer.length) return;
        buffer[byteIndex] = _byte;
    }

    private static class Number {
        final int pointerMove;
        final int bitCount;
        final long minValue;

        Number(int pointerMove, int bitCount, long minValue) {
            this.pointerMove = pointerMove;
            this.bitCount = bitCount;
            this.minValue = minValue;
        }
    }

}
