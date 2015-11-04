package de.longri.serializable;


import java.util.ArrayList;

/**
 * Created by Longri on 04.11.15.
 */
public class SerializableArrayList<T extends Serializable> extends Serializable {

    private ArrayList<T> list = new ArrayList<T>();
    private final Class<T> tClass;

    public SerializableArrayList(Class<T> tClass) {
        this.tClass = tClass;
    }


    @Override
    public void serialize(ArrayWriter writer) {

        writer.writeVariableUnsignedInt(list.size());

        ArrayList<Byte> byteArrayList = new ArrayList<Byte>();

        for (T t : list) {
            t.serialize(writer);
        }
    }

    @Override
    public void deserialize(ArrayReader reader) {
        list = reader.readList(tClass);
    }

    public void add(T object) {
        list.add(object);
    }

    public T get(int index) {
        return list.get(index);
    }

}
