package de.longri.serializable;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

public class VariableByteStore extends NormalStore {

    public VariableByteStore() {
        super();
    }

    public VariableByteStore(byte[] array) {
        super(array);
    }

    public VariableByteStore(String base64) {
        super(base64);
    }

    /**
     * Converts a signed int to a variable length byte array.
     * <p/>
     * The first bit is for continuation info, the other six (last byte) or seven (all other bytes) bits for data. The
     * second bit in the last byte indicates the sign of the number.
     *
     * @param value the int value.
     * @return an array with 1-5 bytes.
     */
    public static byte[] getVariableByteSigned(int value) {
        long absValue = Math.abs((long) value);
        if (absValue < 64) { // 2^6
            // encode the number in a single byte
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x40)};
            }
            return new byte[]{(byte) absValue};
        } else if (absValue < 8192) { // 2^13
            // encode the number in two bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) (absValue >> 7)};
        } else if (absValue < 1048576) { // 2^20
            // encode the number in three bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80),
                        (byte) ((absValue >> 14) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80), (byte) (absValue >> 14)};
        } else if (absValue < 134217728) { // 2^27
            // encode the number in four bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80),
                        (byte) ((absValue >> 14) | 0x80), (byte) ((absValue >> 21) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80),
                    (byte) ((absValue >> 14) | 0x80), (byte) (absValue >> 21)};
        } else {
            // encode the number in five bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80),
                        (byte) ((absValue >> 14) | 0x80), (byte) ((absValue >> 21) | 0x80),
                        (byte) ((absValue >> 28) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80),
                    (byte) ((absValue >> 14) | 0x80), (byte) ((absValue >> 21) | 0x80), (byte) (absValue >> 28)};
        }
    }

    /**
     * Converts an unsigned int to a variable length byte array.
     * <p/>
     * The first bit is for continuation info, the other seven bits for data.
     *
     * @param value the int value, must not be negative.
     * @return an array with 1-5 bytes.
     */
    public static byte[] getVariableByteUnsigned(int value) {
        if (value < 0) {
            throw new InvalidParameterException("negative value not allowed: " + value);
        } else if (value < 128) { // 2^7
            // encode the number in a single byte
            return new byte[]{(byte) value};
        } else if (value < 16384) { // 2^14
            // encode the number in two bytes
            return new byte[]{(byte) (value | 0x80), (byte) (value >> 7)};
        } else if (value < 2097152) { // 2^21
            // encode the number in three bytes
            return new byte[]{(byte) (value | 0x80), (byte) ((value >> 7) | 0x80), (byte) (value >> 14)};
        } else if (value < 268435456) { // 2^28
            // encode the number in four bytes
            return new byte[]{(byte) (value | 0x80), (byte) ((value >> 7) | 0x80), (byte) ((value >> 14) | 0x80),
                    (byte) (value >> 21)};
        } else {
            // encode the number in five bytes
            return new byte[]{(byte) (value | 0x80), (byte) ((value >> 7) | 0x80), (byte) ((value >> 14) | 0x80),
                    (byte) ((value >> 21) | 0x80), (byte) (value >> 28)};
        }
    }

    /**
     * Converts a variable amount of bytes from the read buffer to a signed int.
     * <p/>
     * The first bit is for continuation info, the other six (last byte) or seven (all other bytes) bits are for data.
     * The second bit in the last byte indicates the sign of the number.
     *
     * @return the int value.
     */
    public int readSignedInt() {
        int variableByteDecode = 0;
        byte variableByteShift = 0;

        // check if the continuation bit is set
        while ((this.buffer[this.readIndex] & 0x80) != 0) {
            variableByteDecode |= (this.buffer[this.readIndex++] & 0x7f) << variableByteShift;
            variableByteShift += 7;
        }

        // read the six data bits from the last byte
        if ((this.buffer[this.readIndex] & 0x40) != 0) {
            // negative
            return -(variableByteDecode | ((this.buffer[this.readIndex++] & 0x3f) << variableByteShift));
        }
        // positive
        return variableByteDecode | ((this.buffer[this.readIndex++] & 0x3f) << variableByteShift);
    }

    /**
     * Converts a variable amount of bytes from the read buffer to an unsigned int.
     * <p/>
     * The first bit is for continuation info, the other seven bits are for data.
     *
     * @return the int value.
     */
    public int readUnsignedInt() {
        int variableByteDecode = 0;
        byte variableByteShift = 0;

        // check if the continuation bit is set
        while ((this.buffer[this.readIndex] & 0x80) != 0) {
            variableByteDecode |= (this.buffer[this.readIndex++] & 0x7f) << variableByteShift;
            variableByteShift += 7;
        }

        // read the seven data bits from the last byte
        return variableByteDecode | (this.buffer[this.readIndex++] << variableByteShift);
    }

    @Override
    protected void _write(Integer i) {
        add(getVariableByteSigned(i));
    }

    @Override
    public int readInt() {
        return readSignedInt();
    }

    @Override
    protected void _write(String s) {
        byte[] bytes = s.getBytes(UTF8_CHARSET);
        add(getVariableByteUnsigned(bytes.length));
        add(bytes);
    }

    @Override
    public String readString() {
        int stringLength = readUnsignedInt();
        if (stringLength > 0 && readIndex + stringLength <= buffer.length) {
            readIndex += stringLength;
            try {
                return new String(buffer, readIndex - stringLength, stringLength, CHARSET_UTF8);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
        return "";
    }
}
