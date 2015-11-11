package de.longri.serializable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Hoepfner on 11.11.2015.
 */
public class ByteArray {
    /**
     * The signum of this ByteArray: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the ByteArray zero <i>must</i> have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each ByteArray value.
     *
     * @serial
     */
    final int signum;

    /**
     * The magnitude of this ByteArray, in <i>big-endian</i> order: the
     * zeroth element of this array is the most-significant int of the
     * magnitude.  The magnitude must be "minimal" in that the most-significant
     * int ({@code mag[0]}) must be non-zero.  This is necessary to
     * ensure that there is exactly one representation for each ByteArray
     * value.  Note that this implies that the ByteArray zero has a
     * zero-length mag array.
     */
    final int[] mag;
    private int maxByteCount = 0; // TODO make final

    // These "redundant fields" are initialized with recognizable nonsense
    // values, and cached the first time they are needed (or never, if they
    // aren't needed).

    /**
     * One plus the bitCount of this ByteArray. Zeros means unitialized.
     *
     * @serial
     * @see #bitCount
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int bitCount;

    /**
     * One plus the internalBitLength of this ByteArray. Zeros means unitialized.
     * (either value is acceptable).
     *
     * @serial
     * @see #internalBitLength()
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int bitLength;

    /**
     * Two plus the lowest set bit of this ByteArray, as returned by
     * getLowestSetBit().
     *
     * @serial
     * @see #getLowestSetBit
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int lowestSetBit;

    /**
     * Two plus the index of the lowest-order int in the magnitude of this
     * ByteArray that contains a nonzero int, or -2 (either value is acceptable).
     * The least significant int has int-number 0, the next int in order of
     * increasing significance has int-number 1, and so forth.
     *
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int firstNonzeroIntNum;

    /**
     * This mask is used to obtain the value of an int as if it were unsigned.
     */
    final static long LONG_MASK = 0xffffffffL;

    /**
     * This constant limits {@code mag.length} of BigIntegers to the supported
     * range.
     */
    private static final int MAX_MAG_LENGTH = Integer.MAX_VALUE / Integer.SIZE + 1; // (1 << 26)

    /**
     * Bit lengths larger than this constant can cause overflow in searchLen
     * calculation and in BitSieve.singleSearch method.
     */
    private static final int PRIME_SEARCH_BIT_LENGTH_LIMIT = 500000000;

    /**
     * The threshold value for using Karatsuba multiplication.  If the number
     * of ints in both mag arrays are greater than this number, then
     * Karatsuba multiplication will be used.   This value is found
     * experimentally to work well.
     */
    private static final int KARATSUBA_THRESHOLD = 80;

    /**
     * The threshold value for using 3-way Toom-Cook multiplication.
     * If the number of ints in each mag array is greater than the
     * Karatsuba threshold, and the number of ints in at least one of
     * the mag arrays is greater than this threshold, then Toom-Cook
     * multiplication will be used.
     */
    private static final int TOOM_COOK_THRESHOLD = 240;

    /**
     * The threshold value for using Karatsuba squaring.  If the number
     * of ints in the number are larger than this value,
     * Karatsuba squaring will be used.   This value is found
     * experimentally to work well.
     */
    private static final int KARATSUBA_SQUARE_THRESHOLD = 128;

    /**
     * The threshold value for using Toom-Cook squaring.  If the number
     * of ints in the number are larger than this value,
     * Toom-Cook squaring will be used.   This value is found
     * experimentally to work well.
     */
    private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;

    /**
     * The threshold value for using Burnikel-Ziegler division.  If the number
     * of ints in the divisor are larger than this value, Burnikel-Ziegler
     * division may be used.  This value is found experimentally to work well.
     */
    static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;

    /**
     * The offset value for using Burnikel-Ziegler division.  If the number
     * of ints in the divisor exceeds the Burnikel-Ziegler threshold, and the
     * number of ints in the dividend is greater than the number of ints in the
     * divisor plus this value, Burnikel-Ziegler division will be used.  This
     * value is found experimentally to work well.
     */
    static final int BURNIKEL_ZIEGLER_OFFSET = 40;

    /**
     * The threshold value for using Schoenhage recursive base conversion. If
     * the number of ints in the number are larger than this value,
     * the Schoenhage algorithm will be used.  In practice, it appears that the
     * Schoenhage routine is faster for any threshold down to 2, and is
     * relatively flat for thresholds between 2-25, so this choice may be
     * varied within this range for very small effect.
     */
    private static final int SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20;

    /**
     * The threshold value for using squaring code to perform multiplication
     * of a {@code ByteArray} instance by itself.  If the number of ints in
     * the number are larger than this value, {@code multiply(this)} will
     * return {@code square()}.
     */
    private static final int MULTIPLY_SQUARE_THRESHOLD = 20;

    // Constructors

    /**
     * Translates a byte array containing the two's-complement binary
     * representation of a ByteArray into a ByteArray.  The input array is
     * assumed to be in <i>big-endian</i> byte-order: the most significant
     * byte is in the zeroth element.
     *
     * @param val big-endian two's-complement binary representation of
     *            ByteArray.
     * @throws NumberFormatException {@code val} is zero bytes long.
     */
    public ByteArray(byte[] val) {
        this.maxByteCount = val.length;
        this.mag = stripLeadingZeroBytes(val);
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            this.signum = 1;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    public ByteArray(int maxByteCount, byte[] val) {
        this.maxByteCount = maxByteCount;
        this.mag = stripLeadingZeroBytes(val);
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            this.signum = 1;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    public ByteArray(short val) {
        this(2, val);
    }

    public ByteArray(int byteCount, short val) {
        this.maxByteCount = byteCount;
        signum = 1;
        mag = new int[1];
        mag[0] = (short) val;
    }


    /**
     * This private constructor translates an int array containing the
     * two's-complement binary representation of a ByteArray into a
     * ByteArray. The input array is assumed to be in <i>big-endian</i>
     * int-order: the most significant int is in the zeroth element.
     */
    private ByteArray(int[] val) {
        if (val.length == 0)
            throw new NumberFormatException("Zero length ByteArray");

        if (val[0] < 0) {
            mag = makePositive(val);
            signum = -1;
        } else {
            mag = trustedStripLeadingZeroInts(val);
            signum = (mag.length == 0 ? 0 : 1);
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }


    /**
     * A constructor for internal use that translates the sign-magnitude
     * representation of a ByteArray into a ByteArray. It checks the
     * arguments and copies the magnitude so this constructor would be
     * safe for external use.
     */
    private ByteArray(int signum, int[] magnitude) {
        this.mag = stripLeadingZeroInts(magnitude);

        if (signum < -1 || signum > 1)
            throw (new NumberFormatException("Invalid signum value"));

        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw (new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

//    /**
//     * Translates the String representation of a ByteArray in the
//     * specified radix into a ByteArray.  The String representation
//     * consists of an optional minus or plus sign followed by a
//     * sequence of one or more digits in the specified radix.  The
//     * character-to-digit mapping is provided by {@code
//     * Character.digit}.  The String may not contain any extraneous
//     * characters (whitespace, for example).
//     *
//     * @param val   String representation of ByteArray.
//     * @param radix radix to be used in interpreting {@code val}.
//     * @throws NumberFormatException {@code val} is not a valid representation
//     *                               of a ByteArray in the specified radix, or {@code radix} is
//     *                               outside the range from {@link Character#MIN_RADIX} to
//     *                               {@link Character#MAX_RADIX}, inclusive.
//     * @see Character#digit
//     */
//    public ByteArray(String val, int radix) {
//        int cursor = 0, numDigits;
//        final int len = val.length();
//
//        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
//            throw new NumberFormatException("Radix out of range");
//        if (len == 0)
//            throw new NumberFormatException("Zero length ByteArray");
//
//        // Check for at most one leading sign
//        int sign = 1;
//        int index1 = val.lastIndexOf('-');
//        int index2 = val.lastIndexOf('+');
//        if (index1 >= 0) {
//            if (index1 != 0 || index2 >= 0) {
//                throw new NumberFormatException("Illegal embedded sign character");
//            }
//            sign = -1;
//            cursor = 1;
//        } else if (index2 >= 0) {
//            if (index2 != 0) {
//                throw new NumberFormatException("Illegal embedded sign character");
//            }
//            cursor = 1;
//        }
//        if (cursor == len)
//            throw new NumberFormatException("Zero length ByteArray");
//
//        // Skip leading zeros and compute number of digits in magnitude
//        while (cursor < len &&
//                Character.digit(val.charAt(cursor), radix) == 0) {
//            cursor++;
//        }
//
//        if (cursor == len) {
//            signum = 0;
//            mag = ZERO.mag;
//            return;
//        }
//
//        numDigits = len - cursor;
//        signum = sign;
//
//        // Pre-allocate array of expected size. May be too large but can
//        // never be too small. Typically exact.
//        long numBits = ((numDigits * bitsPerDigit[radix]) >>> 10) + 1;
//        if (numBits + 31 >= (1L << 32)) {
//            reportOverflow();
//        }
//        int numWords = (int) (numBits + 31) >>> 5;
//        int[] magnitude = new int[numWords];
//
//        // Process first (potentially short) digit group
//        int firstGroupLen = numDigits % digitsPerInt[radix];
//        if (firstGroupLen == 0)
//            firstGroupLen = digitsPerInt[radix];
//        String group = val.substring(cursor, cursor += firstGroupLen);
//        magnitude[numWords - 1] = Integer.parseInt(group, radix);
//        if (magnitude[numWords - 1] < 0)
//            throw new NumberFormatException("Illegal digit");
//
//        // Process remaining digit groups
//        int superRadix = intRadix[radix];
//        int groupVal = 0;
//        while (cursor < len) {
//            group = val.substring(cursor, cursor += digitsPerInt[radix]);
//            groupVal = Integer.parseInt(group, radix);
//            if (groupVal < 0)
//                throw new NumberFormatException("Illegal digit");
//            destructiveMulAdd(magnitude, superRadix, groupVal);
//        }
//        // Required for cases where the array was overallocated.
//        mag = trustedStripLeadingZeroInts(magnitude);
//        if (mag.length >= MAX_MAG_LENGTH) {
//            checkRange();
//        }
//    }
//
//    /*
//         * Constructs a new ByteArray using a char array with radix=10.
//         * Sign is precalculated outside and not allowed in the val.
//         */
//    ByteArray(char[] val, int sign, int len) {
//        int cursor = 0, numDigits;
//
//        // Skip leading zeros and compute number of digits in magnitude
//        while (cursor < len && Character.digit(val[cursor], 10) == 0) {
//            cursor++;
//        }
//        if (cursor == len) {
//            signum = 0;
//            mag = ZERO.mag;
//            return;
//        }
//
//        numDigits = len - cursor;
//        signum = sign;
//        // Pre-allocate array of expected size
//        int numWords;
//        if (len < 10) {
//            numWords = 1;
//        } else {
//            long numBits = ((numDigits * bitsPerDigit[10]) >>> 10) + 1;
//            if (numBits + 31 >= (1L << 32)) {
//                reportOverflow();
//            }
//            numWords = (int) (numBits + 31) >>> 5;
//        }
//        int[] magnitude = new int[numWords];
//
//        // Process first (potentially short) digit group
//        int firstGroupLen = numDigits % digitsPerInt[10];
//        if (firstGroupLen == 0)
//            firstGroupLen = digitsPerInt[10];
//        magnitude[numWords - 1] = parseInt(val, cursor, cursor += firstGroupLen);
//
//        // Process remaining digit groups
//        while (cursor < len) {
//            int groupVal = parseInt(val, cursor, cursor += digitsPerInt[10]);
//            destructiveMulAdd(magnitude, intRadix[10], groupVal);
//        }
//        mag = trustedStripLeadingZeroInts(magnitude);
//        if (mag.length >= MAX_MAG_LENGTH) {
//            checkRange();
//        }
//    }
//
//    // Create an integer with the digits between the two indexes
//    // Assumes start < end. The result may be negative, but it
//    // is to be treated as an unsigned value.
//    private int parseInt(char[] source, int start, int end) {
//        int result = Character.digit(source[start++], 10);
//        if (result == -1)
//            throw new NumberFormatException(new String(source));
//
//        for (int index = start; index < end; index++) {
//            int nextVal = Character.digit(source[index], 10);
//            if (nextVal == -1)
//                throw new NumberFormatException(new String(source));
//            result = 10 * result + nextVal;
//        }
//
//        return result;
//    }

    // bitsPerDigit in the given radix times 1024
// Rounded up to avoid underallocation.
    private static long bitsPerDigit[] = {0, 0,
            1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543, 3672,
            3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633,
            4696, 4756, 4814, 4870, 4923, 4975, 5025, 5074, 5120, 5166, 5210,
            5253, 5295};

    // Multiply x array times word y in place, and add word z
    private static void destructiveMulAdd(int[] x, int y, int z) {
        // Perform the multiplication word by word
        long ylong = y & LONG_MASK;
        long zlong = z & LONG_MASK;
        int len = x.length;

        long product = 0;
        long carry = 0;
        for (int i = len - 1; i >= 0; i--) {
            product = ylong * (x[i] & LONG_MASK) + carry;
            x[i] = (int) product;
            carry = product >>> 32;
        }

        // Perform the addition
        long sum = (x[len - 1] & LONG_MASK) + zlong;
        x[len - 1] = (int) sum;
        carry = sum >>> 32;
        for (int i = len - 2; i >= 0; i--) {
            sum = (x[i] & LONG_MASK) + carry;
            x[i] = (int) sum;
            carry = sum >>> 32;
        }
    }

//    /**
//     * Translates the decimal String representation of a ByteArray into a
//     * ByteArray.  The String representation consists of an optional minus
//     * sign followed by a sequence of one or more decimal digits.  The
//     * character-to-digit mapping is provided by {@code Character.digit}.
//     * The String may not contain any extraneous characters (whitespace, for
//     * example).
//     *
//     * @param val decimal String representation of ByteArray.
//     * @throws NumberFormatException {@code val} is not a valid representation
//     *                               of a ByteArray.
//     * @see Character#digit
//     */
//    public ByteArray(String val) {
//        this(val, 10);
//    }

//    /**
//     * Constructs a randomly generated ByteArray, uniformly distributed over
//     * the range 0 to (2<sup>{@code numBits}</sup> - 1), inclusive.
//     * The uniformity of the distribution assumes that a fair source of random
//     * bits is provided in {@code rnd}.  Note that this constructor always
//     * constructs a non-negative ByteArray.
//     *
//     * @param numBits maximum internalBitLength of the new ByteArray.
//     * @param rnd     source of randomness to be used in computing the new
//     *                ByteArray.
//     * @throws IllegalArgumentException {@code numBits} is negative.
//     * @see #internalBitLength()
//     */
//    public ByteArray(int numBits, Random rnd) {
//        this(1, randomBits(numBits, rnd));
//    }
//
//    private static byte[] randomBits(int numBits, Random rnd) {
//        if (numBits < 0)
//            throw new IllegalArgumentException("numBits must be non-negative");
//        int numBytes = (int) (((long) numBits + 7) / 8); // avoid overflow
//        byte[] randomBits = new byte[numBytes];
//
//        // Generate random bytes and mask out any excess bits
//        if (numBytes > 0) {
//            rnd.nextBytes(randomBits);
//            int excessBits = 8 * numBytes - numBits;
//            randomBits[0] &= (1 << (8 - excessBits)) - 1;
//        }
//        return randomBits;
//    }
//
//
//    // Minimum size in bits that the requested prime number has
//// before we use the large prime number generating algorithms.
//// The cutoff of 95 was chosen empirically for best performance.
//    private static final int SMALL_PRIME_THRESHOLD = 95;
//
//    // Certainty required to meet the spec of probablePrime
//    private static final int DEFAULT_PRIME_CERTAINTY = 100;

    /**
     * This internal constructor differs from its public cousin
     * with the arguments reversed in two ways: it assumes that its
     * arguments are correct, and it doesn't copy the magnitude array.
     */
    ByteArray(int maxByteCount, int[] magnitude, int signum) {

        this.maxByteCount = maxByteCount;

        if (magnitude.length * 4 > maxByteCount) {
            ArrayList<Byte> intByteArray = new ArrayList<>();
            for (int i : magnitude) {
                intByteArray.add((byte) (i >> 24));
                intByteArray.add((byte) (i >> 16));
                intByteArray.add((byte) (i >> 8));
                intByteArray.add((byte) (i));
            }
            byte[] bytes = new byte[intByteArray.size()];
            int index = 0;
            for (byte b : intByteArray) {
                bytes[index++] = b;
            }

            byte[] newArray = Arrays.copyOfRange(bytes, bytes.length - maxByteCount, bytes.length);

            this.signum = (newArray.length == 0 ? 0 : signum);
            this.mag = stripLeadingZeroBytes(newArray);
            if (mag.length >= MAX_MAG_LENGTH) {
                checkRange();
            }
        } else {
            this.signum = (magnitude.length == 0 ? 0 : signum);
            this.mag = magnitude;
            if (mag.length >= MAX_MAG_LENGTH) {
                checkRange();
            }
        }


    }

    private ByteArray(int[] magnitude, int signum) {
        this.maxByteCount = magnitude.length * 4;

        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = magnitude;
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * This private constructor is for internal use and assumes that its
     * arguments are correct.
     */
    private ByteArray(byte[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = stripLeadingZeroBytes(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * Throws an {@code ArithmeticException} if the {@code ByteArray} would be
     * out of the supported range.
     *
     * @throws ArithmeticException if {@code this} exceeds the supported range.
     */
    private void checkRange() {
        if (mag.length > MAX_MAG_LENGTH || mag.length == MAX_MAG_LENGTH && mag[0] < 0) {
            reportOverflow();
        }
    }

    private static void reportOverflow() {
        throw new ArithmeticException("ByteArray would overflow supported range");
    }

    //Static Factory Methods

    public ByteArray(int val) {
        this(4, val);
    }

    public ByteArray(int byteCount, int val) {
        this.maxByteCount = byteCount;
        signum = 1;
        mag = new int[1];
        mag[0] = (int) val;
    }


    public ByteArray(long val) {
        this(8, val);
    }

    public ByteArray(int byteCount, long val) {
        this.maxByteCount = byteCount;

        signum = 1;
        int highWord = (int) (val >>> 32);

        mag = new int[2];
        mag[0] = highWord;
        mag[1] = (int) val;

//
//        if (highWord == 0) {
//            mag = new int[1];
//            mag[0] = (int) val;
//        } else {
//            mag = new int[2];
//            mag[0] = highWord;
//            mag[1] = (int) val;
//        }
    }

    /**
     * Returns a ByteArray with the given two's complement representation.
     * Assumes that the input array will not be modified (the returned
     * ByteArray will reference the input array if feasible).
     */
    private static ByteArray valueOf(int val[]) {
        return (val[0] > 0 ? new ByteArray(val, 1) : new ByteArray(val));
    }

    // Constants

    /**
     * Initialize static constant array when class is loaded.
     */
    private final static int MAX_CONSTANT = 16;
    private static ByteArray posConst[] = new ByteArray[MAX_CONSTANT + 1];
    private static ByteArray negConst[] = new ByteArray[MAX_CONSTANT + 1];

    /**
     * The cache of powers of each radix.  This allows us to not have to
     * recalculate powers of radix^(2^n) more than once.  This speeds
     * Schoenhage recursive base conversion significantly.
     */
    private static volatile ByteArray[][] powerCache;

    /**
     * The cache of logarithms of radices for base conversion.
     */
    //  private static final double[] logCache;

    /**
     * The natural log of 2.  This is used in computing cache indices.
     */
    private static final double LOG_TWO = Math.log(2.0);

    static {
        for (int i = 1; i <= MAX_CONSTANT; i++) {
            int[] magnitude = new int[1];
            magnitude[0] = i;
            posConst[i] = new ByteArray(magnitude, 1);
            negConst[i] = new ByteArray(magnitude, -1);
        }

//        /*
//         * Initialize the cache of radix^(2^x) values used for base conversion
//         * with just the very first value.  Additional values will be created
//         * on demand.
//         */
//        powerCache = new ByteArray[Character.MAX_RADIX + 1][];
//        logCache = new double[Character.MAX_RADIX + 1];
//
//        for (int i = Character.MIN_RADIX; i <= Character.MAX_RADIX; i++) {
//            powerCache[i] = new ByteArray[]{new ByteArray(i)};
//            logCache[i] = Math.log(i);
//        }
    }

    /**
     * The ByteArray constant zero.
     *
     * @since 1.2
     */
    public static final ByteArray ZERO = new ByteArray(new int[0], 0);

//    /**
//     * The ByteArray constant one.
//     *
//     * @since 1.2
//     */
//    public static final ByteArray ONE = valueOf(1);
//
//    /**
//     * The ByteArray constant two.  (Not exported.)
//     */
//    private static final ByteArray TWO = valueOf(2);
//
//    /**
//     * The ByteArray constant -1.  (Not exported.)
//     */
//    private static final ByteArray NEGATIVE_ONE = valueOf(-1);
//
//    /**
//     * The ByteArray constant ten.
//     *
//     * @since 1.5
//     */
//    public static final ByteArray TEN = valueOf(10);

// Arithmetic Operations

    /**
     * Returns a ByteArray whose value is {@code (this + val)}.
     *
     * @param val value to be added to this ByteArray.
     * @return {@code this + val}
     */
    public ByteArray add(ByteArray val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val;
        if (val.signum == signum)
            return new ByteArray(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);

        return new ByteArray(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Package private methods used by BigDecimal code to add a ByteArray
     * with a long. Assumes val is not equal to INFLATED.
     */
    ByteArray add(long val) {
        if (val == 0)
            return this;
        if (signum == 0)
            return new ByteArray(val);
        if (Long.signum(val) == signum)
            return new ByteArray(add(mag, Math.abs(val)), signum);
        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, Math.abs(val)) : subtract(Math.abs(val), mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new ByteArray(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Adds the contents of the int array x and long value val. This
     * method allocates a new int array to hold the answer and returns
     * a reference to that array.  Assumes x.length &gt; 0 and val is
     * non-negative
     */
    private static int[] add(int[] x, long val) {
        int[] y;
        long sum = 0;
        int xIndex = x.length;
        int[] result;
        int highWord = (int) (val >>> 32);
        if (highWord == 0) {
            result = new int[xIndex];
            sum = (x[--xIndex] & LONG_MASK) + val;
            result[xIndex] = (int) sum;
        } else {
            if (xIndex == 1) {
                result = new int[2];
                sum = val + (x[0] & LONG_MASK);
                result[1] = (int) sum;
                result[0] = (int) (sum >>> 32);
                return result;
            } else {
                result = new int[xIndex];
                sum = (x[--xIndex] & LONG_MASK) + (val & LONG_MASK);
                result[xIndex] = (int) sum;
                sum = (x[--xIndex] & LONG_MASK) + (highWord & LONG_MASK) + (sum >>> 32);
                result[xIndex] = (int) sum;
            }
        }
        // Copy remainder of longer number while carry propagation is required
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry)
            carry = ((result[--xIndex] = x[xIndex] + 1) == 0);
        // Copy remainder of longer number
        while (xIndex > 0)
            result[--xIndex] = x[xIndex];
        // Grow result if necessary
        if (carry) {
            int bigger[] = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    /**
     * Adds the contents of the int arrays x and y. This method allocates
     * a new int array to hold the answer and returns a reference to that
     * array.
     */
    private static int[] add(int[] x, int[] y) {
        // If x is shorter, swap the two arrays
        if (x.length < y.length) {
            int[] tmp = x;
            x = y;
            y = tmp;
        }

        int xIndex = x.length;
        int yIndex = y.length;
        int result[] = new int[xIndex];
        long sum = 0;
        if (yIndex == 1) {
            sum = (x[--xIndex] & LONG_MASK) + (y[0] & LONG_MASK);
            result[xIndex] = (int) sum;
        } else {
            // Add common parts of both numbers
            while (yIndex > 0) {
                sum = (x[--xIndex] & LONG_MASK) +
                        (y[--yIndex] & LONG_MASK) + (sum >>> 32);
                result[xIndex] = (int) sum;
            }
        }
        // Copy remainder of longer number while carry propagation is required
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry)
            carry = ((result[--xIndex] = x[xIndex] + 1) == 0);

        // Copy remainder of longer number
        while (xIndex > 0)
            result[--xIndex] = x[xIndex];

        // Grow result if necessary
        if (carry) {
            int bigger[] = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    private static int[] subtract(long val, int[] little) {
        int highWord = (int) (val >>> 32);
        if (highWord == 0) {
            int result[] = new int[1];
            result[0] = (int) (val - (little[0] & LONG_MASK));
            return result;
        } else {
            int result[] = new int[2];
            if (little.length == 1) {
                long difference = ((int) val & LONG_MASK) - (little[0] & LONG_MASK);
                result[1] = (int) difference;
                // Subtract remainder of longer number while borrow propagates
                boolean borrow = (difference >> 32 != 0);
                if (borrow) {
                    result[0] = highWord - 1;
                } else {        // Copy remainder of longer number
                    result[0] = highWord;
                }
                return result;
            } else { // little.length == 2
                long difference = ((int) val & LONG_MASK) - (little[1] & LONG_MASK);
                result[1] = (int) difference;
                difference = (highWord & LONG_MASK) - (little[0] & LONG_MASK) + (difference >> 32);
                result[0] = (int) difference;
                return result;
            }
        }
    }

    /**
     * Subtracts the contents of the second argument (val) from the
     * first (big).  The first int array (big) must represent a larger number
     * than the second.  This method allocates the space necessary to hold the
     * answer.
     * assumes val &gt;= 0
     */
    private static int[] subtract(int[] big, long val) {
        int highWord = (int) (val >>> 32);
        int bigIndex = big.length;
        int result[] = new int[bigIndex];
        long difference = 0;

        if (highWord == 0) {
            difference = (big[--bigIndex] & LONG_MASK) - val;
            result[bigIndex] = (int) difference;
        } else {
            difference = (big[--bigIndex] & LONG_MASK) - (val & LONG_MASK);
            result[bigIndex] = (int) difference;
            difference = (big[--bigIndex] & LONG_MASK) - (highWord & LONG_MASK) + (difference >> 32);
            result[bigIndex] = (int) difference;
        }

        // Subtract remainder of longer number while borrow propagates
        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow)
            borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

        // Copy remainder of longer number
        while (bigIndex > 0)
            result[--bigIndex] = big[bigIndex];

        return result;
    }

    /**
     * Returns a ByteArray whose value is {@code (this - val)}.
     *
     * @param val value to be subtracted from this ByteArray.
     * @return {@code this - val}
     */
    public ByteArray subtract(ByteArray val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val.negate();
        if (val.signum != signum)
            return new ByteArray(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new ByteArray(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Subtracts the contents of the second int arrays (little) from the
     * first (big).  The first int array (big) must represent a larger number
     * than the second.  This method allocates the space necessary to hold the
     * answer.
     */
    private static int[] subtract(int[] big, int[] little) {
        int bigIndex = big.length;
        int result[] = new int[bigIndex];
        int littleIndex = little.length;
        long difference = 0;

        // Subtract common parts of both numbers
        while (littleIndex > 0) {
            difference = (big[--bigIndex] & LONG_MASK) -
                    (little[--littleIndex] & LONG_MASK) +
                    (difference >> 32);
            result[bigIndex] = (int) difference;
        }

        // Subtract remainder of longer number while borrow propagates
        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow)
            borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

        // Copy remainder of longer number
        while (bigIndex > 0)
            result[--bigIndex] = big[bigIndex];

        return result;
    }

    /**
     * Returns a ByteArray whose value is {@code (this * val)}.
     *
     * @param val value to be multiplied by this ByteArray.
     * @return {@code this * val}
     * @implNote An implementation may offer better algorithmic
     * performance when {@code val == this}.
     */
    public ByteArray multiply(ByteArray val) {
        if (val.signum == 0 || signum == 0)
            return ZERO;

        int xlen = mag.length;

        if (val == this && xlen > MULTIPLY_SQUARE_THRESHOLD) {
            return square();
        }

        int ylen = val.mag.length;

        if ((xlen < KARATSUBA_THRESHOLD) || (ylen < KARATSUBA_THRESHOLD)) {
            int resultSign = signum == val.signum ? 1 : -1;
            if (val.mag.length == 1) {
                return multiplyByInt(mag, val.mag[0], resultSign);
            }
            if (mag.length == 1) {
                return multiplyByInt(val.mag, mag[0], resultSign);
            }
            int[] result = multiplyToLen(mag, xlen,
                    val.mag, ylen, null);
            result = trustedStripLeadingZeroInts(result);
            return new ByteArray(result, resultSign);
        } else {
            if ((xlen < TOOM_COOK_THRESHOLD) && (ylen < TOOM_COOK_THRESHOLD)) {
                return multiplyKaratsuba(this, val);
            } else {
                return multiplyToomCook3(this, val);
            }
        }
    }

    private static ByteArray multiplyByInt(int[] x, int y, int sign) {
        if (Integer.bitCount(y) == 1) {
            return new ByteArray(shiftLeft(x, Integer.numberOfTrailingZeros(y)), sign);
        }
        int xlen = x.length;
        int[] rmag = new int[xlen + 1];
        long carry = 0;
        long yl = y & LONG_MASK;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (x[i] & LONG_MASK) * yl + carry;
            rmag[rstart--] = (int) product;
            carry = product >>> 32;
        }
        if (carry == 0L) {
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        } else {
            rmag[rstart] = (int) carry;
        }
        return new ByteArray(rmag, sign);
    }


    /**
     * Multiplies int arrays x and y to the specified lengths and places
     * the result into z. There will be no leading zeros in the resultant array.
     */
    private int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        int xstart = xlen - 1;
        int ystart = ylen - 1;

        if (z == null || z.length < (xlen + ylen))
            z = new int[xlen + ylen];

        long carry = 0;
        for (int j = ystart, k = ystart + 1 + xstart; j >= 0; j--, k--) {
            long product = (y[j] & LONG_MASK) *
                    (x[xstart] & LONG_MASK) + carry;
            z[k] = (int) product;
            carry = product >>> 32;
        }
        z[xstart] = (int) carry;

        for (int i = xstart - 1; i >= 0; i--) {
            carry = 0;
            for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
                long product = (y[j] & LONG_MASK) *
                        (x[i] & LONG_MASK) +
                        (z[k] & LONG_MASK) + carry;
                z[k] = (int) product;
                carry = product >>> 32;
            }
            z[i] = (int) carry;
        }
        return z;
    }

    /**
     * Multiplies two BigIntegers using the Karatsuba multiplication
     * algorithm.  This is a recursive divide-and-conquer algorithm which is
     * more efficient for large numbers than what is commonly called the
     * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
     * multiplied have length n, the "grade-school" algorithm has an
     * asymptotic complexity of O(n^2).  In contrast, the Karatsuba algorithm
     * has complexity of O(n^(log2(3))), or O(n^1.585).  It achieves this
     * increased performance by doing 3 multiplies instead of 4 when
     * evaluating the product.  As it has some overhead, should be used when
     * both numbers are larger than a certain threshold (found
     * experimentally).
     * <p/>
     * See:  http://en.wikipedia.org/wiki/Karatsuba_algorithm
     */
    private static ByteArray multiplyKaratsuba(ByteArray x, ByteArray y) {
        int xlen = x.mag.length;
        int ylen = y.mag.length;

        // The number of ints in each half of the number.
        int half = (Math.max(xlen, ylen) + 1) / 2;

        // xl and yl are the lower halves of x and y respectively,
        // xh and yh are the upper halves.
        ByteArray xl = x.getLower(half);
        ByteArray xh = x.getUpper(half);
        ByteArray yl = y.getLower(half);
        ByteArray yh = y.getUpper(half);

        ByteArray p1 = xh.multiply(yh);  // p1 = xh*yh
        ByteArray p2 = xl.multiply(yl);  // p2 = xl*yl

        // p3=(xh+xl)*(yh+yl)
        ByteArray p3 = xh.add(xl).multiply(yh.add(yl));

        // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half) + p2
        ByteArray result = p1.shiftLeft(32 * half).add(p3.subtract(p1).subtract(p2)).shiftLeft(32 * half).add(p2);

        if (x.signum != y.signum) {
            return result.negate();
        } else {
            return result;
        }
    }

    /**
     * Multiplies two BigIntegers using a 3-way Toom-Cook multiplication
     * algorithm.  This is a recursive divide-and-conquer algorithm which is
     * more efficient for large numbers than what is commonly called the
     * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
     * multiplied have length n, the "grade-school" algorithm has an
     * asymptotic complexity of O(n^2).  In contrast, 3-way Toom-Cook has a
     * complexity of about O(n^1.465).  It achieves this increased asymptotic
     * performance by breaking each number into three parts and by doing 5
     * multiplies instead of 9 when evaluating the product.  Due to overhead
     * (additions, shifts, and one division) in the Toom-Cook algorithm, it
     * should only be used when both numbers are larger than a certain
     * threshold (found experimentally).  This threshold is generally larger
     * than that for Karatsuba multiplication, so this algorithm is generally
     * only used when numbers become significantly larger.
     * <p/>
     * The algorithm used is the "optimal" 3-way Toom-Cook algorithm outlined
     * by Marco Bodrato.
     * <p/>
     * See: http://bodrato.it/toom-cook/
     * http://bodrato.it/papers/#WAIFI2007
     * <p/>
     * "Towards Optimal Toom-Cook Multiplication for Univariate and
     * Multivariate Polynomials in Characteristic 2 and 0." by Marco BODRATO;
     * In C.Carlet and B.Sunar, Eds., "WAIFI'07 proceedings", p. 116-133,
     * LNCS #4547. Springer, Madrid, Spain, June 21-22, 2007.
     */
    private static ByteArray multiplyToomCook3(ByteArray a, ByteArray b) {
        int alen = a.mag.length;
        int blen = b.mag.length;

        int largest = Math.max(alen, blen);

        // k is the size (in ints) of the lower-order slices.
        int k = (largest + 2) / 3;   // Equal to ceil(largest/3)

        // r is the size (in ints) of the highest-order slice.
        int r = largest - 2 * k;

        // Obtain slices of the numbers. a2 and b2 are the most significant
        // bits of the numbers a and b, and a0 and b0 the least significant.
        ByteArray a0, a1, a2, b0, b1, b2;
        a2 = a.getToomSlice(k, r, 0, largest);
        a1 = a.getToomSlice(k, r, 1, largest);
        a0 = a.getToomSlice(k, r, 2, largest);
        b2 = b.getToomSlice(k, r, 0, largest);
        b1 = b.getToomSlice(k, r, 1, largest);
        b0 = b.getToomSlice(k, r, 2, largest);

        ByteArray v0, v1, v2, vm1, vinf, t1, t2, tm1, da1, db1;

        v0 = a0.multiply(b0);
        da1 = a2.add(a0);
        db1 = b2.add(b0);
        vm1 = da1.subtract(a1).multiply(db1.subtract(b1));
        da1 = da1.add(a1);
        db1 = db1.add(b1);
        v1 = da1.multiply(db1);
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).multiply(
                db1.add(b2).shiftLeft(1).subtract(b0));
        vinf = a2.multiply(b2);

        // The algorithm requires two divisions by 2 and one by 3.
        // All divisions are known to be exact, that is, they do not produce
        // remainders, and all results are positive.  The divisions by 2 are
        // implemented as right shifts which are relatively efficient, leaving
        // only an exact division by 3, which is done by a specialized
        // linear-time algorithm.
        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        // Number of bits to shift left.
        int ss = k * 32;

        ByteArray result = vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);

        if (a.signum != b.signum) {
            return result.negate();
        } else {
            return result;
        }
    }


    /**
     * Returns a slice of a ByteArray for use in Toom-Cook multiplication.
     *
     * @param lowerSize The size of the lower-order bit slices.
     * @param upperSize The size of the higher-order bit slices.
     * @param slice     The index of which slice is requested, which must be a
     *                  number from 0 to size-1. Slice 0 is the highest-order bits, and slice
     *                  size-1 are the lowest-order bits. Slice 0 may be of different size than
     *                  the other slices.
     * @param fullsize  The size of the larger integer array, used to align
     *                  slices to the appropriate position when multiplying different-sized
     *                  numbers.
     */
    private ByteArray getToomSlice(int lowerSize, int upperSize, int slice,
                                   int fullsize) {
        int start, end, sliceSize, len, offset;

        len = mag.length;
        offset = fullsize - len;

        if (slice == 0) {
            start = 0 - offset;
            end = upperSize - 1 - offset;
        } else {
            start = upperSize + (slice - 1) * lowerSize - offset;
            end = start + lowerSize - 1;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            return ZERO;
        }

        sliceSize = (end - start) + 1;

        if (sliceSize <= 0) {
            return ZERO;
        }

        // While performing Toom-Cook, all slices are positive and
        // the sign is adjusted when the final number is composed.
        if (start == 0 && sliceSize >= len) {
            return this.abs();
        }

        int intSlice[] = new int[sliceSize];
        System.arraycopy(mag, start, intSlice, 0, sliceSize);

        return new ByteArray(trustedStripLeadingZeroInts(intSlice), 1);
    }

    /**
     * Does an exact division (that is, the remainder is known to be zero)
     * of the specified number by 3.  This is used in Toom-Cook
     * multiplication.  This is an efficient algorithm that runs in linear
     * time.  If the argument is not exactly divisible by 3, results are
     * undefined.  Note that this is expected to be called with positive
     * arguments only.
     */
    private ByteArray exactDivideBy3() {
        int len = mag.length;
        int[] result = new int[len];
        long x, w, q, borrow;
        borrow = 0L;
        for (int i = len - 1; i >= 0; i--) {
            x = (mag[i] & LONG_MASK);
            w = x - borrow;
            if (borrow > x) {      // Did we make the number go negative?
                borrow = 1L;
            } else {
                borrow = 0L;
            }

            // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32).  Thus,
            // the effect of this is to divide by 3 (mod 2^32).
            // This is much faster than division on most architectures.
            q = (w * 0xAAAAAAABL) & LONG_MASK;
            result[i] = (int) q;

            // Now check the borrow. The second check can of course be
            // eliminated if the first fails.
            if (q >= 0x55555556L) {
                borrow++;
                if (q >= 0xAAAAAAABL)
                    borrow++;
            }
        }
        result = trustedStripLeadingZeroInts(result);
        return new ByteArray(result, signum);
    }

    /**
     * Returns a new ByteArray representing n lower ints of the number.
     * This is used by Karatsuba multiplication and Karatsuba squaring.
     */
    private ByteArray getLower(int n) {
        int len = mag.length;

        if (len <= n) {
            return abs();
        }

        int lowerInts[] = new int[n];
        System.arraycopy(mag, len - n, lowerInts, 0, n);

        return new ByteArray(trustedStripLeadingZeroInts(lowerInts), 1);
    }

    /**
     * Returns a new ByteArray representing mag.length-n upper
     * ints of the number.  This is used by Karatsuba multiplication and
     * Karatsuba squaring.
     */
    private ByteArray getUpper(int n) {
        int len = mag.length;

        if (len <= n) {
            return ZERO;
        }

        int upperLen = len - n;
        int upperInts[] = new int[upperLen];
        System.arraycopy(mag, 0, upperInts, 0, upperLen);

        return new ByteArray(trustedStripLeadingZeroInts(upperInts), 1);
    }

// Squaring

    /**
     * Returns a ByteArray whose value is {@code (this<sup>2</sup>)}.
     *
     * @return {@code this<sup>2</sup>}
     */
    private ByteArray square() {
        if (signum == 0) {
            return ZERO;
        }
        int len = mag.length;

        if (len < KARATSUBA_SQUARE_THRESHOLD) {
            int[] z = squareToLen(mag, len, null);
            return new ByteArray(trustedStripLeadingZeroInts(z), 1);
        } else {
            if (len < TOOM_COOK_SQUARE_THRESHOLD) {
                return squareKaratsuba();
            } else {
                return squareToomCook3();
            }
        }
    }

    /**
     * Squares the contents of the int array x. The result is placed into the
     * int array z.  The contents of x are not changed.
     */
    private static final int[] squareToLen(int[] x, int len, int[] z) {
        /*
         * The algorithm used here is adapted from Colin Plumb's C library.
         * Technique: Consider the partial products in the multiplication
         * of "abcde" by itself:
         *
         *               a  b  c  d  e
         *            *  a  b  c  d  e
         *          ==================
         *              ae be ce de ee
         *           ad bd cd dd de
         *        ac bc cc cd ce
         *     ab bb bc bd be
         *  aa ab ac ad ae
         *
         * Note that everything above the main diagonal:
         *              ae be ce de = (abcd) * e
         *           ad bd cd       = (abc) * d
         *        ac bc             = (ab) * c
         *     ab                   = (a) * b
         *
         * is a copy of everything below the main diagonal:
         *                       de
         *                 cd ce
         *           bc bd be
         *     ab ac ad ae
         *
         * Thus, the sum is 2 * (off the diagonal) + diagonal.
         *
         * This is accumulated beginning with the diagonal (which
         * consist of the squares of the digits of the input), which is then
         * divided by two, the off-diagonal added, and multiplied by two
         * again.  The low bit is simply a copy of the low bit of the
         * input, so it doesn't need special care.
         */
        int zlen = len << 1;
        if (z == null || z.length < zlen)
            z = new int[zlen];

        // Store the squares, right shifted one bit (i.e., divided by 2)
        int lastProductLowWord = 0;
        for (int j = 0, i = 0; j < len; j++) {
            long piece = (x[j] & LONG_MASK);
            long product = piece * piece;
            z[i++] = (lastProductLowWord << 31) | (int) (product >>> 33);
            z[i++] = (int) (product >>> 1);
            lastProductLowWord = (int) product;
        }

        // Add in off-diagonal sums
        for (int i = len, offset = 1; i > 0; i--, offset += 2) {
            int t = x[i - 1];
            t = mulAdd(z, x, offset, i - 1, t);
            addOne(z, offset - 1, i, t);
        }

        // Shift back up and set low bit
        primitiveLeftShift(z, zlen, 1);
        z[zlen - 1] |= x[len - 1] & 1;

        return z;
    }

    /**
     * Squares a ByteArray using the Karatsuba squaring algorithm.  It should
     * be used when both numbers are larger than a certain threshold (found
     * experimentally).  It is a recursive divide-and-conquer algorithm that
     * has better asymptotic performance than the algorithm used in
     * squareToLen.
     */
    private ByteArray squareKaratsuba() {
        int half = (mag.length + 1) / 2;

        ByteArray xl = getLower(half);
        ByteArray xh = getUpper(half);

        ByteArray xhs = xh.square();  // xhs = xh^2
        ByteArray xls = xl.square();  // xls = xl^2

        // xh^2 << 64  +  (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
        return xhs.shiftLeft(half * 32).add(xl.add(xh).square().subtract(xhs.add(xls))).shiftLeft(half * 32).add(xls);
    }

    /**
     * Squares a ByteArray using the 3-way Toom-Cook squaring algorithm.  It
     * should be used when both numbers are larger than a certain threshold
     * (found experimentally).  It is a recursive divide-and-conquer algorithm
     * that has better asymptotic performance than the algorithm used in
     * squareToLen or squareKaratsuba.
     */
    private ByteArray squareToomCook3() {
        int len = mag.length;

        // k is the size (in ints) of the lower-order slices.
        int k = (len + 2) / 3;   // Equal to ceil(largest/3)

        // r is the size (in ints) of the highest-order slice.
        int r = len - 2 * k;

        // Obtain slices of the numbers. a2 is the most significant
        // bits of the number, and a0 the least significant.
        ByteArray a0, a1, a2;
        a2 = getToomSlice(k, r, 0, len);
        a1 = getToomSlice(k, r, 1, len);
        a0 = getToomSlice(k, r, 2, len);
        ByteArray v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;

        v0 = a0.square();
        da1 = a2.add(a0);
        vm1 = da1.subtract(a1).square();
        da1 = da1.add(a1);
        v1 = da1.square();
        vinf = a2.square();
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).square();

        // The algorithm requires two divisions by 2 and one by 3.
        // All divisions are known to be exact, that is, they do not produce
        // remainders, and all results are positive.  The divisions by 2 are
        // implemented as right shifts which are relatively efficient, leaving
        // only a division by 3.
        // The division by 3 is done by an optimized algorithm for this case.
        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        // Number of bits to shift left.
        int ss = k * 32;

        return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
    }


//    /**
//     * Returns a ByteArray whose value is <tt>(this<sup>exponent</sup>)</tt>.
//     * Note that {@code exponent} is an integer rather than a ByteArray.
//     *
//     * @param exponent exponent to which this ByteArray is to be raised.
//     * @return <tt>this<sup>exponent</sup></tt>
//     * @throws ArithmeticException {@code exponent} is negative.  (This would
//     *                             cause the operation to yield a non-integer value.)
//     */
//    public ByteArray pow(int exponent) {
//        if (exponent < 0) {
//            throw new ArithmeticException("Negative exponent");
//        }
//        if (signum == 0) {
//            return (exponent == 0 ? ONE : this);
//        }
//
//        ByteArray partToSquare = this.abs();
//
//        // Factor out powers of two from the base, as the exponentiation of
//        // these can be done by left shifts only.
//        // The remaining part can then be exponentiated faster.  The
//        // powers of two will be multiplied back at the end.
//        int powersOfTwo = partToSquare.getLowestSetBit();
//        long bitsToShift = (long) powersOfTwo * exponent;
//        if (bitsToShift > Integer.MAX_VALUE) {
//            reportOverflow();
//        }
//
//        int remainingBits;
//
//        // Factor the powers of two out quickly by shifting right, if needed.
//        if (powersOfTwo > 0) {
//            partToSquare = partToSquare.shiftRight(powersOfTwo);
//            remainingBits = partToSquare.internalBitLength();
//            if (remainingBits == 1) {  // Nothing left but +/- 1?
//                if (signum < 0 && (exponent & 1) == 1) {
//                    return NEGATIVE_ONE.shiftLeft(powersOfTwo * exponent);
//                } else {
//                    return ONE.shiftLeft(powersOfTwo * exponent);
//                }
//            }
//        } else {
//            remainingBits = partToSquare.internalBitLength();
//            if (remainingBits == 1) { // Nothing left but +/- 1?
//                if (signum < 0 && (exponent & 1) == 1) {
//                    return NEGATIVE_ONE;
//                } else {
//                    return ONE;
//                }
//            }
//        }
//
//        // This is a quick way to approximate the size of the result,
//        // similar to doing log2[n] * exponent.  This will give an upper bound
//        // of how big the result can be, and which algorithm to use.
//        long scaleFactor = (long) remainingBits * exponent;
//
//        // Use slightly different algorithms for small and large operands.
//        // See if the result will safely fit into a long. (Largest 2^63-1)
//        if (partToSquare.mag.length == 1 && scaleFactor <= 62) {
//            // Small number algorithm.  Everything fits into a long.
//            int newSign = (signum < 0 && (exponent & 1) == 1 ? -1 : 1);
//            long result = 1;
//            long baseToPow2 = partToSquare.mag[0] & LONG_MASK;
//
//            int workingExponent = exponent;
//
//            // Perform exponentiation using repeated squaring trick
//            while (workingExponent != 0) {
//                if ((workingExponent & 1) == 1) {
//                    result = result * baseToPow2;
//                }
//
//                if ((workingExponent >>>= 1) != 0) {
//                    baseToPow2 = baseToPow2 * baseToPow2;
//                }
//            }
//
//            // Multiply back the powers of two (quickly, by shifting left)
//            if (powersOfTwo > 0) {
//                if (bitsToShift + scaleFactor <= 62) { // Fits in long?
//                    return valueOf((result << bitsToShift) * newSign);
//                } else {
//                    return valueOf(result * newSign).shiftLeft((int) bitsToShift);
//                }
//            } else {
//                return valueOf(result * newSign);
//            }
//        } else {
//            // Large number algorithm.  This is basically identical to
//            // the algorithm above, but calls multiply() and square()
//            // which may use more efficient algorithms for large numbers.
//            ByteArray answer = ONE;
//
//            int workingExponent = exponent;
//            // Perform exponentiation using repeated squaring trick
//            while (workingExponent != 0) {
//                if ((workingExponent & 1) == 1) {
//                    answer = answer.multiply(partToSquare);
//                }
//
//                if ((workingExponent >>>= 1) != 0) {
//                    partToSquare = partToSquare.square();
//                }
//            }
//            // Multiply back the (exponentiated) powers of two (quickly,
//            // by shifting left)
//            if (powersOfTwo > 0) {
//                answer = answer.shiftLeft(powersOfTwo * exponent);
//            }
//
//            if (signum < 0 && (exponent & 1) == 1) {
//                return answer.negate();
//            } else {
//                return answer;
//            }
//        }
//    }


    /**
     * Package private method to return bit length for an integer.
     */
    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    /**
     * Left shift int array a up to len by n bits. Returns the array that
     * results from the shift since space may have to be reallocated.
     */
    private static int[] leftShift(int[] a, int len, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        int bitsInHighWord = bitLengthForInt(a[0]);

        // If shift can be done without recopy, do so
        if (n <= (32 - bitsInHighWord)) {
            primitiveLeftShift(a, len, nBits);
            return a;
        } else { // Array must be resized
            if (nBits <= (32 - bitsInHighWord)) {
                int result[] = new int[nInts + len];
                System.arraycopy(a, 0, result, 0, len);
                primitiveLeftShift(result, result.length, nBits);
                return result;
            } else {
                int result[] = new int[nInts + len + 1];
                System.arraycopy(a, 0, result, 0, len);
                primitiveRightShift(result, result.length, 32 - nBits);
                return result;
            }
        }
    }

    // shifts a up to len right n bits assumes no leading zeros, 0<n<32
    static void primitiveRightShift(int[] a, int len, int n) {
        int n2 = 32 - n;
        for (int i = len - 1, c = a[i]; i > 0; i--) {
            int b = c;
            c = a[i - 1];
            a[i] = (c << n2) | (b >>> n);
        }
        a[0] >>>= n;
    }

    // shifts a up to len left n bits assumes no leading zeros, 0<=n<32
    static void primitiveLeftShift(int[] a, int len, int n) {
        if (len == 0 || n == 0)
            return;

        int n2 = 32 - n;
        for (int i = 0, c = a[i], m = i + len - 1; i < m; i++) {
            int b = c;
            c = a[i + 1];
            a[i] = (b << n) | (c >>> n2);
        }
        a[len - 1] <<= n;
    }

    /**
     * Calculate bitlength of contents of the first len elements an int array,
     * assuming there are no leading zero ints.
     */
    private static int internalBitLength(int[] val, int len) {
        if (len == 0)
            return 0;
        return ((len - 1) << 5) + bitLengthForInt(val[0]);
    }

    /**
     * Returns a ByteArray whose value is the absolute value of this
     * ByteArray.
     *
     * @return {@code abs(this)}
     */
    public ByteArray abs() {
        return (signum >= 0 ? this : this.negate());
    }

    /**
     * Returns a ByteArray whose value is {@code (-this)}.
     *
     * @return {@code -this}
     */
    public ByteArray negate() {
        return new ByteArray(this.mag, -this.signum);
    }

    /**
     * Returns the signum function of this ByteArray.
     *
     * @return -1, 0 or 1 as the value of this ByteArray is negative, zero or
     * positive.
     */
    public int signum() {
        return this.signum;
    }

// Modular Arithmetic Operations


    static int[] bnExpModThreshTable = {7, 25, 81, 241, 673, 1793,
            Integer.MAX_VALUE}; // Sentinel


    /**
     * Montgomery reduce n, modulo mod.  This reduces modulo mod and divides
     * by 2^(32*mlen). Adapted from Colin Plumb's C library.
     */
    private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
        int c = 0;
        int len = mlen;
        int offset = 0;

        do {
            int nEnd = n[n.length - 1 - offset];
            int carry = mulAdd(n, mod, offset, mlen, inv * nEnd);
            c += addOne(n, offset, mlen, carry);
            offset++;
        } while (--len > 0);

        while (c > 0)
            c += subN(n, mod, mlen);

        while (intArrayCmpToLen(n, mod, mlen) >= 0)
            subN(n, mod, mlen);

        return n;
    }


    /*
     * Returns -1, 0 or +1 as big-endian unsigned int array arg1 is less than,
     * equal to, or greater than arg2 up to length len.
     */
    private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
        for (int i = 0; i < len; i++) {
            long b1 = arg1[i] & LONG_MASK;
            long b2 = arg2[i] & LONG_MASK;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    /**
     * Subtracts two numbers of same length, returning borrow.
     */
    private static int subN(int[] a, int[] b, int len) {
        long sum = 0;

        while (--len >= 0) {
            sum = (a[len] & LONG_MASK) -
                    (b[len] & LONG_MASK) + (sum >> 32);
            a[len] = (int) sum;
        }

        return (int) (sum >> 32);
    }

    /**
     * Multiply an array by one word k and add to result, return the carry
     */
    static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
        long kLong = k & LONG_MASK;
        long carry = 0;

        offset = out.length - offset - 1;
        for (int j = len - 1; j >= 0; j--) {
            long product = (in[j] & LONG_MASK) * kLong +
                    (out[offset] & LONG_MASK) + carry;
            out[offset--] = (int) product;
            carry = product >>> 32;
        }
        return (int) carry;
    }

    /**
     * Add one word to the number a mlen words into a. Return the resulting
     * carry.
     */
    static int addOne(int[] a, int offset, int mlen, int carry) {
        offset = a.length - 1 - mlen - offset;
        long t = (a[offset] & LONG_MASK) + (carry & LONG_MASK);

        a[offset] = (int) t;
        if ((t >>> 32) == 0)
            return 0;
        while (--mlen >= 0) {
            if (--offset < 0) { // Carry out of number
                return 1;
            } else {
                a[offset]++;
                if (a[offset] != 0)
                    return 0;
            }
        }
        return 1;
    }

//    /**
//     * Returns a ByteArray whose value is (this ** exponent) mod (2**p)
//     */
//    private ByteArray modPow2(ByteArray exponent, int p) {
//        /*
//         * Perform exponentiation using repeated squaring trick, chopping off
//         * high order bits as indicated by modulus.
//         */
//        ByteArray result = ONE;
//        ByteArray baseToPow2 = this.mod2(p);
//        int expOffset = 0;
//
//        int limit = exponent.internalBitLength();
//
//        if (this.testBit(0))
//            limit = (p - 1) < limit ? (p - 1) : limit;
//
//        while (expOffset < limit) {
//            if (exponent.testBit(expOffset))
//                result = result.multiply(baseToPow2).mod2(p);
//            expOffset++;
//            if (expOffset < limit)
//                baseToPow2 = baseToPow2.square().mod2(p);
//        }
//
//        return result;
//    }

    /**
     * Returns a ByteArray whose value is this mod(2**p).
     * Assumes that this {@code ByteArray >= 0} and {@code p > 0}.
     */
    private ByteArray mod2(int p) {
        if (internalBitLength() <= p)
            return this;

        // Copy remaining ints of mag
        int numInts = (p + 31) >>> 5;
        int[] mag = new int[numInts];
        System.arraycopy(this.mag, (this.mag.length - numInts), mag, 0, numInts);

        // Mask out any excess bits
        int excessBits = (numInts << 5) - p;
        mag[0] &= (1L << (32 - excessBits)) - 1;

        return (mag[0] == 0 ? new ByteArray(1, mag) : new ByteArray(mag, 1));
    }


// Shift Operations

    /**
     * Returns a ByteArray whose value is {@code (this << n)}.
     * The shift distance, {@code n}, may be negative, in which case
     * this method performs a right shift.
     * (Computes <tt>floor(this * 2<sup>n</sup>)</tt>.)
     *
     * @param n shift distance, in bits.
     * @return {@code this << n}
     * @see #shiftRight
     */
    public ByteArray shiftLeft(int n) {
        if (signum == 0)
            return ZERO;
        if (n > 0) {
            return new ByteArray(this.maxByteCount, shiftLeft(mag, n), signum);
        } else if (n == 0) {
            return this;
        } else {
            // Possible int overflow in (-n) is not a trouble,
            // because shiftRightImpl considers its argument unsigned
            return shiftRightImpl(-n);
        }
    }

    /**
     * Returns a magnitude array whose value is {@code (mag << n)}.
     * The shift distance, {@code n}, is considered unnsigned.
     * (Computes <tt>this * 2<sup>n</sup></tt>.)
     *
     * @param mag magnitude, the most-significant int ({@code mag[0]}) must be non-zero.
     * @param n   unsigned shift distance, in bits.
     * @return {@code mag << n}
     */
    private static int[] shiftLeft(int[] mag, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int newMag[] = null;

        if (nBits == 0) {
            newMag = new int[magLen + nInts];
            System.arraycopy(mag, 0, newMag, 0, magLen);
        } else {
            int i = 0;
            int nBits2 = 32 - nBits;
            int highBits = mag[0] >>> nBits2;
            if (highBits != 0) {
                newMag = new int[magLen + nInts + 1];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen + nInts];
            }
            int j = 0;
            while (j < magLen - 1)
                newMag[i++] = mag[j++] << nBits | mag[j] >>> nBits2;
            newMag[i] = mag[j] << nBits;
        }
        return newMag;
    }

    /**
     * Returns a ByteArray whose value is {@code (this >> n)}.  Sign
     * extension is performed.  The shift distance, {@code n}, may be
     * negative, in which case this method performs a left shift.
     * (Computes <tt>floor(this / 2<sup>n</sup>)</tt>.)
     *
     * @param n shift distance, in bits.
     * @return {@code this >> n}
     * @see #shiftLeft
     */
    public ByteArray shiftRight(int n) {
        if (signum == 0)
            return ZERO;
        if (n > 0) {
            return shiftRightImpl(n);
        } else if (n == 0) {
            return this;
        } else {
            // Possible int overflow in {@code -n} is not a trouble,
            // because shiftLeft considers its argument unsigned
            return new ByteArray(shiftLeft(mag, -n), signum);
        }
    }

    /**
     * Returns a ByteArray whose value is {@code (this >> n)}. The shift
     * distance, {@code n}, is considered unsigned.
     * (Computes <tt>floor(this * 2<sup>-n</sup>)</tt>.)
     *
     * @param n unsigned shift distance, in bits.
     * @return {@code this >> n}
     */
    private ByteArray shiftRightImpl(int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int newMag[] = null;

        // Special case: entire contents shifted off the end
        if (nInts >= magLen)
            return (signum >= 0 ? ZERO : negConst[1]);

        if (nBits == 0) {
            int newMagLen = magLen - nInts;
            newMag = Arrays.copyOf(mag, newMagLen);
        } else {
            int i = 0;
            int highBits = mag[0] >>> nBits;
            if (highBits != 0) {
                newMag = new int[magLen - nInts];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen - nInts - 1];
            }

            int nBits2 = 32 - nBits;
            int j = 0;
            while (j < magLen - nInts - 1)
                newMag[i++] = (mag[j++] << nBits2) | (mag[j] >>> nBits);
        }

        if (signum < 0) {
            // Find out whether any one-bits were shifted off the end.
            boolean onesLost = false;
            for (int i = magLen - 1, j = magLen - nInts; i >= j && !onesLost; i--)
                onesLost = (mag[i] != 0);
            if (!onesLost && nBits != 0)
                onesLost = (mag[magLen - nInts - 1] << (32 - nBits) != 0);

            if (onesLost)
                newMag = javaIncrement(newMag);
        }

        return new ByteArray(newMag, signum);
    }

    int[] javaIncrement(int[] val) {
        int lastSum = 0;
        for (int i = val.length - 1; i >= 0 && lastSum == 0; i--)
            lastSum = (val[i] += 1);
        if (lastSum == 0) {
            val = new int[val.length + 1];
            val[0] = 1;
        }
        return val;
    }

// Bitwise Operations

    /**
     * Returns a ByteArray whose value is {@code (this & val)}.  (This
     * method returns a negative ByteArray if and only if this and val are
     * both negative.)
     *
     * @param val value to be AND'ed with this ByteArray.
     * @return {@code this & val}
     */
    public ByteArray and(ByteArray val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++)
            result[i] = (getInt(result.length - i - 1)
                    & val.getInt(result.length - i - 1));

        return valueOf(result);
    }

    /**
     * Returns a ByteArray whose value is {@code (this | val)}.  (This method
     * returns a negative ByteArray if and only if either this or val is
     * negative.)
     *
     * @param val value to be OR'ed with this ByteArray.
     * @return {@code this | val}
     */
    public ByteArray or(ByteArray val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++)
            result[i] = (getInt(result.length - i - 1)
                    | val.getInt(result.length - i - 1));

        return valueOf(result);
    }

    /**
     * Returns a ByteArray whose value is {@code (this ^ val)}.  (This method
     * returns a negative ByteArray if and only if exactly one of this and
     * val are negative.)
     *
     * @param val value to be XOR'ed with this ByteArray.
     * @return {@code this ^ val}
     */
    public ByteArray xor(ByteArray val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++)
            result[i] = (getInt(result.length - i - 1)
                    ^ val.getInt(result.length - i - 1));

        return valueOf(result);
    }

    /**
     * Returns a ByteArray whose value is {@code (~this)}.  (This method
     * returns a negative value if and only if this ByteArray is
     * non-negative.)
     *
     * @return {@code ~this}
     */
    public ByteArray not() {
        int[] result = new int[intLength()];
        for (int i = 0; i < result.length; i++)
            result[i] = ~getInt(result.length - i - 1);

        return valueOf(result);
    }

    /**
     * Returns a ByteArray whose value is {@code (this & ~val)}.  This
     * method, which is equivalent to {@code and(val.not())}, is provided as
     * a convenience for masking operations.  (This method returns a negative
     * ByteArray if and only if {@code this} is negative and {@code val} is
     * positive.)
     *
     * @param val value to be complemented and AND'ed with this ByteArray.
     * @return {@code this & ~val}
     */
    public ByteArray andNot(ByteArray val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++)
            result[i] = (getInt(result.length - i - 1)
                    & ~val.getInt(result.length - i - 1));

        return valueOf(result);
    }


// Single Bit Operations

    /**
     * Returns {@code true} if and only if the designated bit is set.
     * (Computes {@code ((this & (1<<n)) != 0)}.)
     *
     * @param n index of bit to test.
     * @return {@code true} if and only if the designated bit is set.
     * @throws ArithmeticException {@code n} is negative.
     */
    public boolean testBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
    }

    /**
     * Returns a ByteArray whose value is equivalent to this ByteArray
     * with the designated bit set.  (Computes {@code (this | (1<<n))}.)
     *
     * @param n index of bit to set.
     * @return {@code this | (1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public ByteArray setBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum + 2)];

        for (int i = 0; i < result.length; i++)
            result[result.length - i - 1] = getInt(i);

        result[result.length - intNum - 1] |= (1 << (n & 31));

        return valueOf(result);
    }

    /**
     * Returns a ByteArray whose value is equivalent to this ByteArray
     * with the designated bit cleared.
     * (Computes {@code (this & ~(1<<n))}.)
     *
     * @param n index of bit to clear.
     * @return {@code this & ~(1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public ByteArray clearBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), ((n + 1) >>> 5) + 1)];

        for (int i = 0; i < result.length; i++)
            result[result.length - i - 1] = getInt(i);

        result[result.length - intNum - 1] &= ~(1 << (n & 31));

        return valueOf(result);
    }

    /**
     * Returns a ByteArray whose value is equivalent to this ByteArray
     * with the designated bit flipped.
     * (Computes {@code (this ^ (1<<n))}.)
     *
     * @param n index of bit to flip.
     * @return {@code this ^ (1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public ByteArray flipBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum + 2)];

        for (int i = 0; i < result.length; i++)
            result[result.length - i - 1] = getInt(i);

        result[result.length - intNum - 1] ^= (1 << (n & 31));

        return valueOf(result);
    }

    /**
     * Returns the index of the rightmost (lowest-order) one bit in this
     * ByteArray (the number of zero bits to the right of the rightmost
     * one bit).  Returns -1 if this ByteArray contains no one bits.
     * (Computes {@code (this == 0? -1 : log2(this & -this))}.)
     *
     * @return index of the rightmost one bit in this ByteArray.
     */
    public int getLowestSetBit() {
        @SuppressWarnings("deprecation") int lsb = lowestSetBit - 2;
        if (lsb == -2) {  // lowestSetBit not initialized yet
            lsb = 0;
            if (signum == 0) {
                lsb -= 1;
            } else {
                // Search for lowest order nonzero int
                int i, b;
                for (i = 0; (b = getInt(i)) == 0; i++)
                    ;
                lsb += (i << 5) + Integer.numberOfTrailingZeros(b);
            }
            lowestSetBit = lsb + 2;
        }
        return lsb;
    }


// Miscellaneous Bit Operations

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this ByteArray, <i>excluding</i> a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  (Computes
     * {@code (ceil(log2(this < 0 ? -this : this+1)))}.)
     *
     * @return number of bits in the minimal two's-complement
     * representation of this ByteArray, <i>excluding</i> a sign bit.
     */
    private int internalBitLength() {
        @SuppressWarnings("deprecation") int n = bitLength - 1;
        if (n == -1) { // internalBitLength not initialized yet
            int[] m = mag;
            int len = m.length;
            if (len == 0) {
                n = 0; // offset by one to initialize
            } else {
                // Calculate the bit length of the magnitude
                int magBitLength = ((len - 1) << 5) + bitLengthForInt(mag[0]);
                if (signum < 0) {
                    // Check if magnitude is a power of two
                    boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                    for (int i = 1; i < len && pow2; i++)
                        pow2 = (mag[i] == 0);

                    n = (pow2 ? magBitLength - 1 : magBitLength);
                } else {
                    n = magBitLength;
                }
            }
            bitLength = n + 1;
        }
        return n;
    }

    /**
     * Returns the number of bits in the two's complement representation
     * of this ByteArray that differ from its sign bit.  This method is
     * useful when implementing bit-vector style sets atop BigIntegers.
     *
     * @return number of bits in the two's complement representation
     * of this ByteArray that differ from its sign bit.
     */
    public int bitCount() {
        @SuppressWarnings("deprecation") int bc = bitCount - 1;
        if (bc == -1) {  // bitCount not initialized yet
            bc = 0;      // offset by one to initialize
            // Count the bits in the magnitude
            for (int i = 0; i < mag.length; i++)
                bc += Integer.bitCount(mag[i]);
            if (signum < 0) {
                // Count the trailing zeros in the magnitude
                int magTrailingZeroCount = 0, j;
                for (j = mag.length - 1; mag[j] == 0; j--)
                    magTrailingZeroCount += 32;
                magTrailingZeroCount += Integer.numberOfTrailingZeros(mag[j]);
                bc += magTrailingZeroCount - 1;
            }
            bitCount = bc + 1;
        }
        return bc;
    }

// Primality Testing


// Comparison Operations

    /**
     * Compares this ByteArray with the specified ByteArray.  This
     * method is provided in preference to individual methods for each
     * of the six boolean comparison operators ({@literal <}, ==,
     * {@literal >}, {@literal >=}, !=, {@literal <=}).  The suggested
     * idiom for performing these comparisons is: {@code
     * (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}, where
     * &lt;<i>op</i>&gt; is one of the six comparison operators.
     *
     * @param val ByteArray to which this ByteArray is to be compared.
     * @return -1, 0 or 1 as this ByteArray is numerically less than, equal
     * to, or greater than {@code val}.
     */
    public int compareTo(ByteArray val) {
        if (signum == val.signum) {
            switch (signum) {
                case 1:
                    return compareMagnitude(val);
                case -1:
                    return val.compareMagnitude(this);
                default:
                    return 0;
            }
        }
        return signum > val.signum ? 1 : -1;
    }

    /**
     * Compares the magnitude array of this ByteArray with the specified
     * ByteArray's. This is the version of compareTo ignoring sign.
     *
     * @param val ByteArray whose magnitude array to be compared.
     * @return -1, 0 or 1 as this magnitude array is less than, equal to or
     * greater than the magnitude aray for the specified ByteArray's.
     */
    final int compareMagnitude(ByteArray val) {
        int[] m1 = mag;
        int len1 = m1.length;
        int[] m2 = val.mag;
        int len2 = m2.length;
        if (len1 < len2)
            return -1;
        if (len1 > len2)
            return 1;
        for (int i = 0; i < len1; i++) {
            int a = m1[i];
            int b = m2[i];
            if (a != b)
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
        }
        return 0;
    }

    /**
     * Version of compareMagnitude that compares magnitude with long value.
     * val can't be Long.MIN_VALUE.
     */
    final int compareMagnitude(long val) {
        assert val != Long.MIN_VALUE;
        int[] m1 = mag;
        int len = m1.length;
        if (len > 2) {
            return 1;
        }
        if (val < 0) {
            val = -val;
        }
        int highWord = (int) (val >>> 32);
        if (highWord == 0) {
            if (len < 1)
                return -1;
            if (len > 1)
                return 1;
            int a = m1[0];
            int b = (int) val;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
            }
            return 0;
        } else {
            if (len < 2)
                return -1;
            int a = m1[0];
            int b = highWord;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
            }
            a = m1[1];
            b = (int) val;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
            }
            return 0;
        }
    }

    /**
     * Compares this ByteArray with the specified Object for equality.
     *
     * @param x Object to which this ByteArray is to be compared.
     * @return {@code true} if and only if the specified Object is a
     * ByteArray whose value is numerically equal to this ByteArray.
     */
    public boolean equals(Object x) {
        // This test is just an optimization, which may or may not help
        if (x == this)
            return true;

        if (!(x instanceof ByteArray))
            return false;

        ByteArray xInt = (ByteArray) x;
        if (xInt.signum != signum)
            return false;

        int[] m = mag;
        int len = m.length;
        int[] xm = xInt.mag;
        if (len != xm.length)
            return false;

        for (int i = 0; i < len; i++)
            if (xm[i] != m[i])
                return false;

        return true;
    }

    /**
     * Returns the minimum of this ByteArray and {@code val}.
     *
     * @param val value with which the minimum is to be computed.
     * @return the ByteArray whose value is the lesser of this ByteArray and
     * {@code val}.  If they are equal, either may be returned.
     */
    public ByteArray min(ByteArray val) {
        return (compareTo(val) < 0 ? this : val);
    }

    /**
     * Returns the maximum of this ByteArray and {@code val}.
     *
     * @param val value with which the maximum is to be computed.
     * @return the ByteArray whose value is the greater of this and
     * {@code val}.  If they are equal, either may be returned.
     */
    public ByteArray max(ByteArray val) {
        return (compareTo(val) > 0 ? this : val);
    }


// Hash Function

    /**
     * Returns the hash code for this ByteArray.
     *
     * @return hash code for this ByteArray.
     */
    public int hashCode() {
        int hashCode = 0;

        for (int i = 0; i < mag.length; i++)
            hashCode = (int) (31 * hashCode + (mag[i] & LONG_MASK));

        return hashCode * signum;
    }

    /* zero[i] is a string of i consecutive zeros. */
    private static String zeros[] = new String[64];

    static {
        zeros[63] =
                "000000000000000000000000000000000000000000000000000000000000000";
        for (int i = 0; i < 63; i++)
            zeros[i] = zeros[63].substring(0, i);
    }


    /**
     * Returns a byte array containing the two's-complement
     * representation of this ByteArray.  The byte array will be in
     * <i>big-endian</i> byte-order: the most significant byte is in
     * the zeroth element.  The array will contain the minimum number
     * of bytes required to represent this ByteArray, including at
     * least one sign bit, which is {@code (ceil((this.internalBitLength() +
     * 1)/8))}.  (This representation is compatible with the
     * {@link #ByteArray(byte[]) (byte[])} constructor.)
     *
     * @return a byte array containing the two's-complement representation of
     * this ByteArray.
     * @see #ByteArray(byte[])
     */
    public byte[] toByteArray() {
        int byteLen = internalBitLength() / 8 + 1;

        byteLen = this.maxByteCount;
        byte[] byteArray = new byte[byteLen];
        for (int i = byteLen - 1, bytesCopied = 4, nextInt = 0, intIndex = 0; i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = getInt(intIndex++);
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            byteArray[i] = (byte) nextInt;
        }
        return byteArray;
    }

    /**
     * Converts this ByteArray to an {@code int}.  This
     * conversion is analogous to a
     * <i>narrowing primitive conversion</i> from {@code long} to
     * {@code int} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this ByteArray is too big to fit in an
     * {@code int}, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the ByteArray value as well as return a
     * result with the opposite sign.
     *
     * @return this ByteArray converted to an {@code int}.
     * @see #intValueExact()
     */
    public int intValue() {
        int result = 0;
        result = getInt(0);
        return result;
    }

    /**
     * Converts this ByteArray to a {@code long}.  This
     * conversion is analogous to a
     * <i>narrowing primitive conversion</i> from {@code long} to
     * {@code int} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this ByteArray is too big to fit in a
     * {@code long}, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the ByteArray value as well as return a
     * result with the opposite sign.
     *
     * @return this ByteArray converted to a {@code long}.
     * @see #longValueExact()
     */
    public long longValue() {
        long result = 0;

        for (int i = 1; i >= 0; i--)
            result = (result << 32) + (getInt(i) & LONG_MASK);
        return result;
    }


    /**
     * Returns a copy of the input array stripped of any leading zero bytes.
     */
    private static int[] stripLeadingZeroInts(int val[]) {
        int vlen = val.length;
        int keep;

        // Find first nonzero byte
        for (keep = 0; keep < vlen && val[keep] == 0; keep++)
            ;
        return java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    /**
     * Returns the input array stripped of any leading zero bytes.
     * Since the source is trusted the copying may be skipped.
     */
    private static int[] trustedStripLeadingZeroInts(int val[]) {
        int vlen = val.length;
        int keep;

        // Find first nonzero byte
        for (keep = 0; keep < vlen && val[keep] == 0; keep++)
            ;
        return keep == 0 ? val : java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    /**
     * Returns a copy of the input array stripped of any leading zero bytes.
     */
    private static int[] stripLeadingZeroBytes(byte a[]) {
        int byteLength = a.length;
        int keep;

        // Find first nonzero byte
        for (keep = 0; keep < byteLength && a[keep] == 0; keep++)
            ;

        // Allocate new array and copy relevant part of input array
        int intLength = ((byteLength - keep) + 3) >>> 2;
        int[] result = new int[intLength];
        int b = byteLength - 1;
        for (int i = intLength - 1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int bytesRemaining = b - keep + 1;
            int bytesToTransfer = Math.min(3, bytesRemaining);
            for (int j = 8; j <= (bytesToTransfer << 3); j += 8)
                result[i] |= ((a[b--] & 0xff) << j);
        }
        return result;
    }

    /**
     * Takes an array a representing a negative 2's-complement number and
     * returns the minimal (no leading zero bytes) unsigned whose value is -a.
     */
    private static int[] makePositive(byte a[]) {
        int keep, k;
        int byteLength = a.length;

        // Find first non-sign (0xff) byte of input
        for (keep = 0; keep < byteLength && a[keep] == -1; keep++)
            ;


        /* Allocate output array.  If all non-sign bytes are 0x00, we must
         * allocate space for one extra output byte. */
        for (k = keep; k < byteLength && a[k] == 0; k++)
            ;

        int extraByte = (k == byteLength) ? 1 : 0;
        int intLength = ((byteLength - keep + extraByte) + 3) >>> 2;
        int result[] = new int[intLength];

        /* Copy one's complement of input into output, leaving extra
         * byte (if it exists) == 0x00 */
        int b = byteLength - 1;
        for (int i = intLength - 1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int numBytesToTransfer = Math.min(3, b - keep + 1);
            if (numBytesToTransfer < 0)
                numBytesToTransfer = 0;
            for (int j = 8; j <= 8 * numBytesToTransfer; j += 8)
                result[i] |= ((a[b--] & 0xff) << j);

            // Mask indicates which bits must be complemented
            int mask = -1 >>> (8 * (3 - numBytesToTransfer));
            result[i] = ~result[i] & mask;
        }

        // Add one to one's complement to generate two's complement
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = (int) ((result[i] & LONG_MASK) + 1);
            if (result[i] != 0)
                break;
        }

        return result;
    }

    /**
     * Takes an array a representing a negative 2's-complement number and
     * returns the minimal (no leading zero ints) unsigned whose value is -a.
     */
    private static int[] makePositive(int a[]) {
        int keep, j;

        // Find first non-sign (0xffffffff) int of input
        for (keep = 0; keep < a.length && a[keep] == -1; keep++)
            ;

        /* Allocate output array.  If all non-sign ints are 0x00, we must
         * allocate space for one extra output int. */
        for (j = keep; j < a.length && a[j] == 0; j++)
            ;
        int extraInt = (j == a.length ? 1 : 0);
        int result[] = new int[a.length - keep + extraInt];

        /* Copy one's complement of input into output, leaving extra
         * int (if it exists) == 0x00 */
        for (int i = keep; i < a.length; i++)
            result[i - keep + extraInt] = ~a[i];

        // Add one to one's complement to generate two's complement
        for (int i = result.length - 1; ++result[i] == 0; i--)
            ;

        return result;
    }
//
//    /*
//     * The following two arrays are used for fast String conversions.  Both
//     * are indexed by radix.  The first is the number of digits of the given
//     * radix that can fit in a Java long without "going negative", i.e., the
//     * highest integer n such that radix**n < 2**63.  The second is the
//     * "long radix" that tears each number into "long digits", each of which
//     * consists of the number of digits in the corresponding element in
//     * digitsPerLong (longRadix[i] = i**digitPerLong[i]).  Both arrays have
//     * nonsense values in their 0 and 1 elements, as radixes 0 and 1 are not
//     * used.
//     */
//    private static int digitsPerLong[] = {0, 0,
//            62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14,
//            14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12};

//    private static ByteArray longRadix[] = {null, null,
//            valueOf(0x4000000000000000L), valueOf(0x383d9170b85ff80bL),
//            valueOf(0x4000000000000000L), valueOf(0x6765c793fa10079dL),
//            valueOf(0x41c21cb8e1000000L), valueOf(0x3642798750226111L),
//            valueOf(0x1000000000000000L), valueOf(0x12bf307ae81ffd59L),
//            valueOf(0xde0b6b3a7640000L), valueOf(0x4d28cb56c33fa539L),
//            valueOf(0x1eca170c00000000L), valueOf(0x780c7372621bd74dL),
//            valueOf(0x1e39a5057d810000L), valueOf(0x5b27ac993df97701L),
//            valueOf(0x1000000000000000L), valueOf(0x27b95e997e21d9f1L),
//            valueOf(0x5da0e1e53c5c8000L), valueOf(0xb16a458ef403f19L),
//            valueOf(0x16bcc41e90000000L), valueOf(0x2d04b7fdd9c0ef49L),
//            valueOf(0x5658597bcaa24000L), valueOf(0x6feb266931a75b7L),
//            valueOf(0xc29e98000000000L), valueOf(0x14adf4b7320334b9L),
//            valueOf(0x226ed36478bfa000L), valueOf(0x383d9170b85ff80bL),
//            valueOf(0x5a3c23e39c000000L), valueOf(0x4e900abb53e6b71L),
//            valueOf(0x7600ec618141000L), valueOf(0xaee5720ee830681L),
//            valueOf(0x1000000000000000L), valueOf(0x172588ad4f5f0981L),
//            valueOf(0x211e44f7d02c1000L), valueOf(0x2ee56725f06e5c71L),
//            valueOf(0x41c21cb8e1000000L)};

    /*
     * These two arrays are the integer analogue of above.
     */
    private static int digitsPerInt[] = {0, 0, 30, 19, 15, 13, 11,
            11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};

    private static int intRadix[] = {0, 0,
            0x40000000, 0x4546b3db, 0x40000000, 0x48c27395, 0x159fd800,
            0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00, 0xcc6db61,
            0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f, 0x10000000,
            0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000, 0x6b5a6e1d,
            0x6c20a40, 0x8d2d931, 0xb640000, 0xe8d4a51, 0x1269ae40,
            0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840, 0x34e63b41,
            0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519, 0x39aa400
    };

/**
 * These routines provide access to the two's complement representation
 * of BigIntegers.
 */

    /**
     * Returns the length of the two's complement representation in ints,
     * including space for at least one sign bit.
     */
    private int intLength() {
        return (internalBitLength() >>> 5) + 1;
    }

    /* Returns sign bit */
    private int signBit() {
        return signum < 0 ? 1 : 0;
    }

    /* Returns an int of sign bits */
    private int signInt() {
        return signum < 0 ? -1 : 0;
    }

    /**
     * Returns the specified int of the little-endian two's complement
     * representation (int 0 is the least significant).  The int number can
     * be arbitrarily high (values are logically preceded by infinitely many
     * sign ints).
     */
    private int getInt(int n) {
        if (n < 0)
            return 0;
        if (n >= mag.length)
            return signInt();

        int magInt = mag[mag.length - n - 1];

        return (signum >= 0 ? magInt :
                (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
    }

    /**
     * Returns the index of the int that contains the first nonzero int in the
     * little-endian binary representation of the magnitude (int 0 is the
     * least significant). If the magnitude is zero, return value is undefined.
     */
    private int firstNonzeroIntNum() {
        int fn = firstNonzeroIntNum - 2;
        if (fn == -2) { // firstNonzeroIntNum not initialized yet
            fn = 0;

            // Search for the first nonzero int
            int i;
            int mlen = mag.length;
            for (i = mlen - 1; i >= 0 && mag[i] == 0; i--)
                ;
            fn = mlen - i - 1;
            firstNonzeroIntNum = fn + 2; // offset by two to initialize
        }
        return fn;
    }

    /**
     * use serialVersionUID from JDK 1.1. for interoperability
     */
    private static final long serialVersionUID = -8287574255936472291L;

    public int bitLength() {


        int length = 0;

        int index = -1;
        for (int i : this.mag) {
            if (i != 0) {
                index++;
            } else break;
        }

        length = 32 * index;

        //count last bit's

        int count = 0;
        int v = this.mag[index];
        while (v != 0) {
            count++;
            v = v >>> 1;
        }


        return length + count;
    }


    // Support for resetting final fields while deserializing
    private static class UnsafeHolder {
        private static final sun.misc.Unsafe unsafe;
        private static final long signumOffset;
        private static final long magOffset;

        static {
            try {
                unsafe = sun.misc.Unsafe.getUnsafe();
                signumOffset = unsafe.objectFieldOffset
                        (ByteArray.class.getDeclaredField("signum"));
                magOffset = unsafe.objectFieldOffset
                        (ByteArray.class.getDeclaredField("mag"));
            } catch (Exception ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        static void putSign(ByteArray bi, int sign) {
            unsafe.putIntVolatile(bi, signumOffset, sign);
        }

        static void putMag(ByteArray bi, int[] magnitude) {
            unsafe.putObjectVolatile(bi, magOffset, magnitude);
        }
    }

    /**
     * Save the {@code ByteArray} instance to a stream.
     * The magnitude of a ByteArray is serialized as a byte array for
     * historical reasons.
     *
     * @serialData two necessary fields are written as well as obsolete
     * fields for compatibility with older versions.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        // set the values of the Serializable fields
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("signum", signum);
        fields.put("magnitude", magSerializedForm());
        // The values written for cached fields are compatible with older
        // versions, but are ignored in readObject so don't otherwise matter.
        fields.put("bitCount", -1);
        fields.put("internalBitLength", -1);
        fields.put("lowestSetBit", -2);
        fields.put("firstNonzeroByteNum", -2);

        // save them
        s.writeFields();
    }

    /**
     * Returns the mag array as an array of bytes.
     */
    private byte[] magSerializedForm() {
        int len = mag.length;

        int bitLen = (len == 0 ? 0 : ((len - 1) << 5) + bitLengthForInt(mag[0]));
        int byteLen = (bitLen + 7) >>> 3;
        byte[] result = new byte[byteLen];

        for (int i = byteLen - 1, bytesCopied = 4, intIndex = len - 1, nextInt = 0;
             i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            result[i] = (byte) nextInt;
        }
        return result;
    }

    /**
     * Converts this {@code ByteArray} to a {@code long}, checking
     * for lost information.  If the value of this {@code ByteArray}
     * is out of the range of the {@code long} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code ByteArray} converted to a {@code long}.
     * @throws ArithmeticException if the value of {@code this} will
     *                             not exactly fit in a {@code long}.
     * @see ByteArray#longValue
     * @since 1.8
     */
    public long longValueExact() {
        if (mag.length <= 2 && internalBitLength() <= 63)
            return longValue();
        else
            throw new ArithmeticException("ByteArray out of long range");
    }

    /**
     * Converts this {@code ByteArray} to an {@code int}, checking
     * for lost information.  If the value of this {@code ByteArray}
     * is out of the range of the {@code int} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code ByteArray} converted to an {@code int}.
     * @throws ArithmeticException if the value of {@code this} will
     *                             not exactly fit in a {@code int}.
     * @see ByteArray#intValue
     * @since 1.8
     */
    public int intValueExact() {
        if (mag.length <= 1 && internalBitLength() <= 31)
            return intValue();
        else
            throw new ArithmeticException("ByteArray out of int range");
    }


    public String toString() {

        byte[] value = toByteArray();

        StringBuilder builder = new StringBuilder();
        byte[] masks = {-128, 64, 32, 16, 8, 4, 2, 1};


        builder.append(value.length + " bytes :");


        int writtenByte = 0;
        for (byte write : value) {
            int index = 0;
            for (short m : masks) {
                if (index++ == 4) builder.append(' ');
                if ((write & m) == m) {
                    builder.append('1');
                } else {
                    builder.append('0');
                }
            }
            if (writtenByte++ < value.length - 1) builder.append(" | ");
        }
        return builder.toString();
    }

}