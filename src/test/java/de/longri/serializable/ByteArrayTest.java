package de.longri.serializable;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by Hoepfner on 11.11.2015.
 */
public class ByteArrayTest extends TestCase {


    @Test
    public void testByteArrayConstructors() throws Exception {

        long l = -1;

        ByteArray ba = new ByteArray(l);
        assertTrue(ba.bitLength() == 64);
        assertTrue(ba.toByteArray().length == 8);

        ba = new ByteArray(9, l);
        assertTrue(ba.bitLength() == 64);
        assertTrue(ba.toByteArray().length == 9);
    }

}