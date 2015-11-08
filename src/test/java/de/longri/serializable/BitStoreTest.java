package de.longri.serializable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Longri on 03.11.15.
 */
public class BitStoreTest {

//    @Test
//    public void testByteObject() throws Exception {
//        TestObjectByte obj = new TestObjectByte();
//
//        obj.value3 = 86;
//
//
//        TestObjectByte obj2 = new TestObjectByte();
//
//        //TODO test with minus values and Byte.MIN_VALUE / Byte.MAX_VALUE
//
//        BitStore writer = new BitStore();
//        obj.serialize(writer);
//        obj2.deserialize(new BitStore(writer.getArray()));
//
//        assertEquals(obj, obj2);
//
//
//    }


    @Test
    public void testByteObject() throws Exception {
        TestObjectByte obj = new TestObjectByte();
        obj.value1 = 26;
        obj.value2 = 117;
        obj.value3 = -86;
        obj.value4 = Byte.MIN_VALUE;
        obj.value5 = Byte.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Byte.MIN_VALUE + 1;
        obj.value8 = Byte.MAX_VALUE - 1;
        obj.value9 = Byte.MIN_VALUE + (Byte.MAX_VALUE / 2);
        obj.value10 = Byte.MAX_VALUE - (Byte.MAX_VALUE / 2);

        TestObjectByte obj2 = new TestObjectByte();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));

        assertEquals(obj, obj2);
    }


    @Test
    public void testShortObject() throws Exception {

        TestObjectShort obj = new TestObjectShort();
        obj.shortValue1 = 260;
        obj.shortValue2 = 1170;
        obj.shortValue3 = 2000;

//TODO test with minus values and Short.MIN_VALUE / Short.MAX_VALUE

        TestObjectShort obj2 = new TestObjectShort();


        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));

        assertEquals(obj, obj2);

    }


    @Test
    public void testIntegerObject() throws Exception {

        TestObjectInteger obj = new TestObjectInteger();
//        obj.IntegerValue1 = 16310;
//        obj.IntegerValue2 = 3242341;
        obj.IntegerValue3 = 3124;
//        obj.IntegerValue3 = Integer.MIN_VALUE;

        TestObjectInteger obj2 = new TestObjectInteger();


        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));

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