package de.longri.serializable;

import java.io.UnsupportedEncodingException;

/**
 * Created by Longri on 05.11.15.
 */
public class NormalStore extends StoreBase {


    public NormalStore(int initialSize) {
        super(initialSize);
    }

    public NormalStore() {
        super();
    }

    public NormalStore(byte[] values) {
        super(values);
    }

    public NormalStore(String base64) {
        super(base64);
    }

    int readIndex = 0;

    @Override
    protected void _write(boolean b) {
        buffer[size++] = (byte) (b ? 1 : 0);
    }

    @Override
    protected void _write(Byte b) {
        buffer[size++] = b;
    }

    @Override
    protected void _write(Short s) {
        add(new byte[]{(byte) (s >> 8), (byte) (s >> 0)});
    }

    @Override
    protected void _write(Integer i) {
        add(new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) (i >> 0)});

    }

    @Override
    protected void _write(Long l) {
        add(new byte[]{(byte) (l >> 56), (byte) (l >> 48), (byte) (l >> 40), (byte) (l >> 32),
                (byte) (l >> 24), (byte) (l >> 16), (byte) (l >> 8), (byte) (l >> 0)});
    }

    @Override
    protected void _write(String s) {
        byte[] bytes = s.getBytes(UTF8_CHARSET);
        _write(bytes.length);
        add(bytes);
    }

    @Override
    public boolean readBool() {
        return buffer[readIndex++] == 1;
    }

    @Override
    public byte readByte() {
        return buffer[readIndex++];
    }

    @Override
    public short readShort() {
        return (short) (buffer[readIndex++] << 8 | (buffer[readIndex++] & 0xff));
    }

    @Override
    public int readInt() {
        return buffer[readIndex++] << 24 | (buffer[readIndex++] & 0xff) << 16 | (buffer[readIndex++] & 0xff) << 8
                | (buffer[readIndex++] & 0xff);
    }

    @Override
    public long readLong() {
        return (buffer[readIndex++] & 0xffL) << 56 | (buffer[readIndex++] & 0xffL) << 48 | (buffer[readIndex++] & 0xffL) << 40
                | (buffer[readIndex++] & 0xffL) << 32 | (buffer[readIndex++] & 0xffL) << 24
                | (buffer[readIndex++] & 0xffL) << 16 | (buffer[readIndex++] & 0xffL) << 8 | (buffer[readIndex++] & 0xffL);
    }

    @Override
    public String readString() {

        int stringLength = readInt();

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

    protected void add(byte[] bytes) {
        for (byte b : bytes) {
            _write(b);
        }
    }


}
