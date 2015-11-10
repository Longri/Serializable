package de.longri.serializable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Longri on 03.11.15.
 */
public class BitStoreTest {

    @Test
    public void testBooleanObject() throws Exception {

        TestObjectBoolean obj = new TestObjectBoolean();
        obj.value1 = true;
        obj.value2 = false;
        obj.value3 = true;

        TestObjectBoolean obj2 = new TestObjectBoolean();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Boolean: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }


    @Test
    public void testByteObject() throws Exception {
        TestObjectByte obj = new TestObjectByte();

        obj.value1 = -36;
        obj.value2 = 117;
        obj.value3 = 36;
        obj.value4 = Byte.MIN_VALUE;
        obj.value5 = Byte.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Byte.MIN_VALUE + 1;
        obj.value8 = Byte.MAX_VALUE - 1;

        obj.value9 = Byte.MIN_VALUE + (Byte.MAX_VALUE / 2);
        obj.value10 = Byte.MAX_VALUE - (Byte.MAX_VALUE / 2);

        obj.value11 = Byte.MIN_VALUE + (Byte.MAX_VALUE / 4);
        obj.value12 = Byte.MAX_VALUE - (Byte.MAX_VALUE / 4);

        obj.value13 = Byte.MIN_VALUE + (Byte.MAX_VALUE / 8);
        obj.value14 = Byte.MAX_VALUE - (Byte.MAX_VALUE / 8);


        TestObjectByte obj2 = new TestObjectByte();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Byte: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }


    @Test
    public void testShortObject() throws Exception {

        TestObjectShort obj = new TestObjectShort();

        obj.value1 = -36;
        obj.value2 = 117;
        obj.value3 = 36;
        obj.value4 = Short.MIN_VALUE;
        obj.value5 = Short.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Short.MIN_VALUE + 1;
        obj.value8 = Short.MAX_VALUE - 1;

        obj.value9 = Short.MIN_VALUE + (Short.MAX_VALUE / 2);
        obj.value10 = Short.MAX_VALUE - (Short.MAX_VALUE / 2);

        obj.value11 = Short.MIN_VALUE + (Short.MAX_VALUE / 4);
        obj.value12 = Short.MAX_VALUE - (Short.MAX_VALUE / 4);

        obj.value13 = Short.MIN_VALUE + (Short.MAX_VALUE / 8);
        obj.value14 = Short.MAX_VALUE - (Short.MAX_VALUE / 8);


        TestObjectShort obj2 = new TestObjectShort();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Short: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }


    @Test
    public void testIntegerObject() throws Exception {

        TestObjectInteger obj = new TestObjectInteger();

        obj.value1 = -36;
        obj.value2 = 117;
        obj.value3 = 36;
        obj.value4 = Integer.MIN_VALUE;
        obj.value5 = Integer.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Integer.MIN_VALUE + 1;
        obj.value8 = Integer.MAX_VALUE - 1;

        obj.value9 = Integer.MIN_VALUE + (Integer.MAX_VALUE / 2);
        obj.value10 = Integer.MAX_VALUE - (Integer.MAX_VALUE / 2);

        obj.value11 = Integer.MIN_VALUE + (Integer.MAX_VALUE / 4);
        obj.value12 = Integer.MAX_VALUE - (Integer.MAX_VALUE / 4);

        obj.value13 = Integer.MIN_VALUE + (Integer.MAX_VALUE / 8);
        obj.value14 = Integer.MAX_VALUE - (Integer.MAX_VALUE / 8);

        TestObjectInteger obj2 = new TestObjectInteger();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Integer: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }

    @Test
    public void testLongObject() throws Exception {


        TestObjectLong obj = new TestObjectLong();

        obj.value1 = Long.MAX_VALUE;

//        obj.value1 = -36;
//        obj.value2 = 117;
//        obj.value3 = 36;
//        obj.value4 = Long.MIN_VALUE;
//        obj.value5 = Long.MAX_VALUE;
//        obj.value6 = 0;
//        obj.value7 = Long.MIN_VALUE + 1;
//        obj.value8 = Long.MAX_VALUE - 1;
//
//        obj.value9 = Long.MIN_VALUE + (Long.MAX_VALUE / 2);
//        obj.value10 = Long.MAX_VALUE - (Long.MAX_VALUE / 2);
//
//        obj.value11 = Long.MIN_VALUE + (Long.MAX_VALUE / 4);
//        obj.value12 = Long.MAX_VALUE - (Long.MAX_VALUE / 4);
//
//        obj.value13 = Long.MIN_VALUE + (Long.MAX_VALUE / 8);
//        obj.value14 = Long.MAX_VALUE - (Long.MAX_VALUE / 8);


        TestObjectLong obj2 = new TestObjectLong();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Long: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }

}