package de.longri.serializable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Longri on 03.11.15.
 */
public class SerializableArrayTest {

    @Test
    public void testArray() throws Exception {
        ListObject o1 = new ListObject();
        o1.booleanStore.store(BooleanStore.Bitmask.BIT_0, true);

        ListObject o2 = new ListObject();
        o2.booleanStore.store(BooleanStore.Bitmask.BIT_1, true);

        ListObject o3 = new ListObject();
        o3.booleanStore.store(BooleanStore.Bitmask.BIT_2, true);

        SerializableArrayList<ListObject> serializeList = new SerializableArrayList<ListObject>(ListObject.class);

        serializeList.add(o1);
        serializeList.add(o2);
        serializeList.add(o3);

        NormalStore writer = new NormalStore();

        serializeList.serialize(writer);

        byte[] byteArray = writer.getArray();

        SerializableArrayList<ListObject> deserializeList = new SerializableArrayList<ListObject>(ListObject.class);

        NormalStore reader = new NormalStore(byteArray);
        deserializeList.deserialize(reader);

        assertEquals(o1, deserializeList.get(0));
        assertEquals(o2, deserializeList.get(1));
        assertEquals(o3, deserializeList.get(2));


    }


    @Test
    public void testArrayObject() throws Exception {

        TestObject obj = new TestObject();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;
        obj.bool1 = true;
        obj.bool2 = true;

        TestObject obj2 = new TestObject();
        obj.IntegerValue1 = 16;
        obj.IntegerValue2 = 322342341;
        obj.bool1 = true;
        obj.bool3 = true;

        TestObject obj3 = new TestObject();
        obj.IntegerValue1 = 845321123;
        obj.IntegerValue2 = 324212;
        obj.bool2 = true;


        SerializableArrayList<TestObject> serializeList = new SerializableArrayList<TestObject>(TestObject.class);

        serializeList.add(obj);
        serializeList.add(obj2);
        serializeList.add(obj3);

        NormalStore writer = new NormalStore();

        serializeList.serialize(writer);

        byte[] byteArray = writer.getArray();

        SerializableArrayList<TestObject> deserializeList = new SerializableArrayList<TestObject>(TestObject.class);

        NormalStore reader = new NormalStore(byteArray);
        deserializeList.deserialize(reader);

        assertEquals(obj, deserializeList.get(0));
        assertEquals(obj2, deserializeList.get(1));
        assertEquals(obj3, deserializeList.get(2));

    }


}