package de.longri.serializable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Longri on 03.11.15.
 */
public class BitStoreTest {


    @Test
    public void testByteObject() throws Exception {

        TestObjectByte obj = new TestObjectByte();
        obj.byteValue1 = 26;
        obj.byteValue2 = 117;
        obj.byteValue3 = (byte) 200;

        TestObjectByte obj2 = new TestObjectByte();


        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));

        assertEquals(obj, obj2);

    }


    @Test
    public void testIntegerObject() throws Exception {

        TestObjectInteger obj = new TestObjectInteger();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;

        TestObjectInteger obj2 = new TestObjectInteger();


        NormalStore writer = new NormalStore();
        obj.serialize(writer);
        obj2.deserialize(new NormalStore(writer.getArray()));

        assertEquals(obj, obj2);
        assertTrue(writer.getArray().length == 8);
    }


    @Test
    public void testObject() throws Exception {

        TestObject obj = new TestObject();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;
        obj.bool1 = true;
        obj.bool3 = true;

        TestObject obj2 = new TestObject();

        NormalStore writer = new NormalStore();
        obj.serialize(writer);
        obj2.deserialize(new NormalStore(writer.getArray()));

        assertEquals(obj, obj2);
    }

    @Test
    public void testLong() throws Exception {

        TestObjectLong obj = new TestObjectLong(System.currentTimeMillis());
        TestObjectLong obj2 = new TestObjectLong(0);

        NormalStore writer = new NormalStore();
        obj.serialize(writer);
        obj2.deserialize(new NormalStore(writer.getArray()));

        assertEquals(obj, obj2);
    }


}