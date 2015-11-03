package de.longri.serializable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Longri on 03.11.15.
 */
public class SerializableTest {

    @Test
    public void testIntegerObject() throws Exception {

        TestObjectInteger obj = new TestObjectInteger();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;

        TestObjectInteger obj2 = new TestObjectInteger();

        obj2.deserialize(obj.serialize());

        assertEquals(obj, obj2);
        assertTrue(obj.serialize().length == 8);
    }

    @Test
    public void testVariableSignedIntegerObject() throws Exception {

        TestObjectVariableByteSignedInteger obj = new TestObjectVariableByteSignedInteger();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;

        TestObjectVariableByteSignedInteger obj2 = new TestObjectVariableByteSignedInteger();

        obj2.deserialize(obj.serialize());

        assertEquals(obj, obj2);
        assertTrue(obj.serialize().length == 7);

    }

    @Test
    public void testVariableUnignedIntegerObject() throws Exception {

        TestObjectVariableByteUnsignedInteger obj = new TestObjectVariableByteUnsignedInteger();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;

        TestObjectVariableByteUnsignedInteger obj2 = new TestObjectVariableByteUnsignedInteger();

        obj2.deserialize(obj.serialize());

        assertEquals(obj, obj2);
        assertTrue(obj.serialize().length == 6);

    }

    @Test
    public void testObject() throws Exception {

        TestObject obj = new TestObject();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;
        obj.bool1 = true;
        obj.bool3 = true;

        TestObject obj2 = new TestObject();

        obj2.deserialize(obj.serialize());

        assertEquals(obj, obj2);
        assertTrue(obj.serialize().length == 9);
    }


}