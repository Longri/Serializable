package de.longri.serializable;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Longri on 05.11.15.
 */
public abstract class StoreBase {
    protected static final String CHARSET_UTF8 = "UTF-8";
    protected static final Charset UTF8_CHARSET = Charset.forName("utf8");
    private static final int INITIAL_SIZE = 20;


    protected byte[] buffer;
    protected int size;

    public StoreBase() {
        buffer = this.createNewItems(INITIAL_SIZE);
    }

    public StoreBase(byte[] values) {
        size = values.length - 1;
        buffer = values;
    }

/*---------- abstract method's --------------*/

    protected abstract void _write(byte b);

    protected abstract void _write(short s);

    protected abstract void _write(int i);

    protected abstract void _write(long l);

    protected abstract void _write(String s);

    public abstract byte readByte();

    public abstract short readShort();

    public abstract int readInt();

    public abstract long readLong();

    public abstract String readString();

    public abstract <T extends Serializable> ArrayList<T> readList(Class<T> tClass);


/*---------- public method's --------------*/

    public final int size() {
        return size;
    }

    public final boolean isEmpty() {
        return size <= 0;
    }

    public final void write(byte b) {
        ensureCapacity(4);
        _write(b);
    }

    public final void write(BooleanStore b) {
        ensureCapacity(4);
        _write(b.getByte());
    }

    public final void write(short s) {
        ensureCapacity(8);
        _write(s);
    }

    public final void write(int i) {
        ensureCapacity(16);
        _write(i);
    }

    public final void write(long l) {
        ensureCapacity(32);
        _write(l);
    }

    public final void write(String s) {
        ensureCapacity(s.length() * 2);
        _write(s);
    }

    public final byte[] getArray() {
        trimToSize();
        return buffer;
    }

/*---------- method's for handle byte array --------------*/

    protected byte[] createNewItems(int size) {
        if (size <= 0) return null;
        return new byte[size];
    }

    /**
     * Increases the size of the backing array to acommodate the specified number of additional buffer. Useful before adding many buffer to
     * avoid multiple backing array resizes.
     *
     * @return {@link #buffer}
     */
    protected byte[] ensureCapacity(int additionalCapacity) {
        int sizeNeeded = size + additionalCapacity;
        if (sizeNeeded > getItemLength()) resize(Math.max(INITIAL_SIZE, sizeNeeded));
        return buffer;
    }

    protected byte[] resize(int newSize) {
        if (newSize < INITIAL_SIZE) newSize = INITIAL_SIZE;
        if (this.buffer == null) {
            this.buffer = createNewItems(newSize);
        } else {
            this.buffer = Arrays.copyOf(this.buffer, newSize);
        }
        return this.buffer;
    }

    /**
     * Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is taken.
     */
    protected void truncate(int newSize) {
        if (size > newSize) {
            size = newSize;
        }
    }

    private int getItemLength() {
        if (this.buffer == null) return 0;
        return buffer.length;
    }


    protected void trimToSize() {
        byte[] array = this.createNewItems(size);
        System.arraycopy(buffer, 0, array, 0, size);
        buffer = array;
    }

}
