package de.longri.serializable;

import java.util.ArrayList;


/**
 * Created by Longri on 05.11.15.
 */
public class BitStore extends StoreBase {

    @Override
    protected void _write(byte b) {

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
        throw new NotImplementedException("Read Byte not implemented from \"BitStore\"");
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
