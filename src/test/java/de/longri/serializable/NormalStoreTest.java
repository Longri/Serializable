package de.longri.serializable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Longri on 03.11.15.
 */
public class NormalStoreTest {


    @Test
    public void testByteObject() throws Exception {

        TestObjectByte obj = new TestObjectByte();
        obj.byteValue1 = 26;
        obj.byteValue2 = 117;
        obj.byteValue3 = (byte) 200;

        TestObjectByte obj2 = new TestObjectByte();

        //TODO test with minus values and Byte.MIN_VALUE / Byte.MAX_VALUE

        NormalStore writer = new NormalStore();
        obj.serialize(writer);
        obj2.deserialize(new NormalStore(writer.getArray()));

        assertEquals(obj, obj2);

    }

    @Test
    public void testShortObject() throws Exception {

        TestObjectShort obj = new TestObjectShort();
        obj.shortValue1 = 260;
        obj.shortValue2 = 1170;
        obj.shortValue3 = 2000;

        TestObjectShort obj2 = new TestObjectShort();

        //TODO test with minus values and Short.MIN_VALUE / Short.MAX_VALUE

        NormalStore writer = new NormalStore();
        obj.serialize(writer);
        obj2.deserialize(new NormalStore(writer.getArray()));

        assertEquals(obj, obj2);

    }


    @Test
    public void testIntegerObject() throws Exception {

        TestObjectInteger obj = new TestObjectInteger();
//        obj.IntegerValue1 = 16310;
//        obj.IntegerValue2 = 3242341;
        obj.IntegerValue3 = 31243467;

        TestObjectInteger obj2 = new TestObjectInteger();

        //TODO test with minus values and Integer.MIN_VALUE / Integer.MAX_VALUE

        NormalStore writer = new NormalStore();
        obj.serialize(writer);
        obj2.deserialize(new NormalStore(writer.getArray()));

        assertEquals(obj, obj2);

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