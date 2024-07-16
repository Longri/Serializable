package de.longri.serializable;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by Longri on 05.11.15.
 */
public abstract class StoreBase {
    protected static final String CHARSET_UTF8 = "UTF-8";
    protected static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private static final int INITIAL_SIZE = 30000; //20;


    protected byte[] buffer;
    protected int size;

    public StoreBase() {
        buffer = this.createNewItems(INITIAL_SIZE);
    }

    public StoreBase(byte[] values) {
        size = values.length - 1;
        buffer = values;
    }
    /*---------- String constructor and getter based on Base64 --------------*/

    public StoreBase(String base64) {
        this(Base64.getDecoder().decode(base64));
    }

    public String getBase64String() {
        try {
            return Base64.getEncoder().encodeToString(getArray());
        } catch (NotImplementedException e) {
            throw new RuntimeException(e);
        }
    }

    /*---------- abstract method's --------------*/

    protected abstract void _write(boolean b) throws NotImplementedException;

    protected abstract void _write(Byte b) throws NotImplementedException;

    protected abstract void _write(Short s) throws NotImplementedException;

    protected abstract void _write(Integer i) throws NotImplementedException;

    protected abstract void _write(Long l) throws NotImplementedException;

    protected abstract void _write(String s) throws NotImplementedException;

    public abstract boolean readBool() throws NotImplementedException;

    public abstract byte readByte() throws NotImplementedException;

    public abstract short readShort() throws NotImplementedException;

    public abstract int readInt() throws NotImplementedException;

    public abstract long readLong() throws NotImplementedException;

    public abstract String readString() throws NotImplementedException;




    /*---------- public method's --------------*/

    public final int size() {
        return size;
    }

    public final boolean isEmpty() {
        return size <= 0;
    }

    public final void write(boolean b) throws NotImplementedException {
        ensureCapacity(2);
        _write(b);
    }

    public final void write(byte b) throws NotImplementedException {
        ensureCapacity(4);
        _write(b);
    }

    public final void write(BooleanStore b) throws NotImplementedException {
        ensureCapacity(4);
        _write(b.getByte());
    }

    public final void write(short s) throws NotImplementedException {
        ensureCapacity(8);
        _write(s);
    }

    public final void write(int i) throws NotImplementedException {
        ensureCapacity(16);
        _write(i);
    }

    public final void write(long l) throws NotImplementedException {
        ensureCapacity(32);
        _write(l);
    }

    public final void write(String s) throws NotImplementedException {
        if (s == null) {
            ensureCapacity(2);
            _write(s);
        } else {
            ensureCapacity(s.length() * 2);
            _write(s);
        }


    }

    public ZoneId berlinZone = ZoneId.of("Europe/Berlin");

    public final void write(LocalDate localDate) throws NotImplementedException {
        if (localDate == null) {
            long l = -1L;
            write(l);
        } else {
            long unixTimestamp = localDate.atStartOfDay(berlinZone).toEpochSecond() * 1000;
            write(unixTimestamp);
        }
    }

    public LocalDate readLocalDate() throws NotImplementedException {
        long unixTimestamp = readLong();
        if (unixTimestamp == -1L) return null;
        Instant instant = Instant.ofEpochMilli(unixTimestamp);
        return instant.atZone(berlinZone).toLocalDate();
    }


    public final void write(LocalDateTime localDateTime) throws NotImplementedException {
        long unixTimestamp;
        if (localDateTime == null) {
            unixTimestamp = -1;
        } else {
            unixTimestamp = localDateTime.atZone(berlinZone).toEpochSecond() * 1000;
        }
        write(unixTimestamp);

    }

    public final LocalDateTime readLocalDateTime() throws NotImplementedException {
        long unixTimestamp = readLong();
        if (unixTimestamp == -1) return null;
        Instant instant = Instant.ofEpochMilli(unixTimestamp);
        return instant.atZone(berlinZone).toLocalDateTime();
    }

    public byte[] getArray() throws NotImplementedException {
        trimToSize();
        return buffer;
    }

    public <T extends Serializable> ArrayList<T> readList(Class<T> tClass) throws NotImplementedException {
        ArrayList<T> list = new ArrayList<T>();
        int size = readInt();
        for (int i = 0; i < size; i++) {

            try {
                T t = tClass.getDeclaredConstructor().newInstance();
                t.deserialize(this);
                list.add(t);
            } catch (InstantiationException | IllegalAccessException | NotImplementedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        }
        return list;
    }

    /*---------- method's for handle byte array --------------*/

    private byte[] createNewItems(int size) {
        if (size <= 0) return null;
        return new byte[size];
    }

    /**
     * Increases the size of the backing array to acommodate the specified number of additional buffer. Useful before adding many buffer to
     * avoid multiple backing array resizes.
     */
    private void ensureCapacity(int additionalCapacity) {
        int sizeNeeded = size + additionalCapacity;
        if (sizeNeeded > getItemLength()) resize(Math.max(INITIAL_SIZE, sizeNeeded));
    }

    private void resize(int newSize) {
        if (newSize < INITIAL_SIZE) newSize = INITIAL_SIZE;
        if (this.buffer == null) {
            this.buffer = createNewItems(newSize);
        } else {
            this.buffer = Arrays.copyOf(this.buffer, newSize);
        }
    }

    /**
     * Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is taken.
     */
    public void truncate(int newSize) {
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
        if (array == null) return;
        System.arraycopy(buffer, 0, array, 0, size);
        buffer = array;
    }

}
