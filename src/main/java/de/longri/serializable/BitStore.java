package de.longri.serializable;

import java.util.ArrayList;


/**
 * Created by Longri on 05.11.15.
 */
public class BitStore extends StoreBase {

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
        boolean negative = false;
        if (b < 0) {
            negative = true;
            if (b == Short.MIN_VALUE) b = 0;
            else
                b = (byte) -b;
        }
        byte[] bytes = new byte[]{0, b};
        writeValue(negative, bytes, BYTE);
    }


    @Override
    protected void _write(short s) throws NotImplementedException {
        boolean negative = false;
        if (s < 0) {
            negative = true;
            if (s == Short.MIN_VALUE) s = 0;
            else
                s = (short) -s;
        }
        byte[] bytes = new byte[]{0, (byte) (s >> 8), (byte) s};
        writeValue(negative, bytes, SHORT);
    }

    @Override
    protected void _write(int i) throws NotImplementedException {
        boolean negative = false;
        if (i < 0) {
            negative = true;
            if (i == Integer.MIN_VALUE) i = 0;
            else
                i = -i;
        }
        byte[] bytes = new byte[]{0, (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
        writeValue(negative, bytes, INTEGER);
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
        return (byte) readValue(BYTE);
    }

    @Override
    public short readShort() throws NotImplementedException {
        return (short) readValue(SHORT);
    }

    @Override
    public int readInt() throws NotImplementedException {
        return (int) readValue(INTEGER);
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
