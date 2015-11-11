package de.longri.serializable;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by Hoepfner on 11.11.2015.
 */
public class ByteArrayTest extends TestCase {


    @Test
    public void testByteArrayConstructors() throws Exception {



        short s = -1;

        ByteArray ba = new ByteArray((short) s);
        assertTrue(ba.bitLength() == 16);
        assertTrue(ba.toByteArray().length == 2);
        assertTrue(ba.intValue() == -1);

        ba = ba.shiftLeft(1);
        //  assertTrue(ba.bitLength() == 16);
        assertTrue(ba.toByteArray().length == 2);
        assertTrue(ba.intValue() == -2);

        ba = new ByteArray(3, s);
        assertTrue(ba.bitLength() == 16);
        assertTrue(ba.toByteArray().length == 3);
        assertTrue(ba.intValue() == -1);
        assertTrue(ba.longValue() == 4294967295L);

        ba = ba.shiftLeft(1);
        assertTrue(ba.bitLength() == 17);
        assertTrue(ba.toByteArray().length == 3);
        assertTrue(ba.intValue() == -2);
        assertTrue(ba.toByteArray()[0] == 1);


        ba = new ByteArray(4, s);
        assertTrue(ba.bitLength() == 16);
        assertTrue(ba.toByteArray().length == 4);
        assertTrue(ba.intValue() == -1);


        int i = -1;

        ba = new ByteArray(i);
        assertTrue(ba.bitLength() == 32);
        assertTrue(ba.toByteArray().length == 4);
        assertTrue(ba.intValue() == -1);

        ba = ba.shiftLeft(1);
        assertTrue(ba.bitLength() == 32);
        assertTrue(ba.toByteArray().length == 4);
        assertTrue(ba.intValue() == -2);

        ba = new ByteArray(5, i);
        assertTrue(ba.bitLength() == 32);
        assertTrue(ba.toByteArray().length == 5);
        assertTrue(ba.intValue() == -1);
        assertTrue(ba.longValue() == 4294967295L);

        ba = ba.shiftLeft(1);
        assertTrue(ba.bitLength() == 33);
        assertTrue(ba.toByteArray().length == 5);
        assertTrue(ba.intValue() == -2);
        assertTrue(ba.toByteArray()[0] == 1);


        ba = new ByteArray(6, i);
        assertTrue(ba.bitLength() == 32);
        assertTrue(ba.toByteArray().length == 6);
        assertTrue(ba.intValue() == -1);


        long l = -1;

        ba = new ByteArray(l);
        assertTrue(ba.bitLength() == 64);
        assertTrue(ba.toByteArray().length == 8);
        assertTrue(ba.longValue() == -1);

        ba = ba.shiftLeft(1);
        assertTrue(ba.bitLength() == 64);
        assertTrue(ba.toByteArray().length == 8);
        assertTrue(ba.intValue() == -2);


        ba = new ByteArray(9, l);
        assertTrue(ba.bitLength() == 64);
        assertTrue(ba.toByteArray().length == 9);
        assertTrue(ba.longValue() == -1);

        ba = ba.shiftLeft(1);
        assertTrue(ba.bitLength() == 65);
        assertTrue(ba.toByteArray().length == 9);
        assertTrue(ba.intValue() == -2);
        assertTrue(ba.toByteArray()[0] == 1);

        ba = new ByteArray(10, l);
        assertTrue(ba.bitLength() == 64);
        assertTrue(ba.toByteArray().length == 10);
        assertTrue(ba.longValue() == -1);

    }

}