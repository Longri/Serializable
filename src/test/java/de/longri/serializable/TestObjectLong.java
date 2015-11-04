package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectLong extends Serializable {

    protected long LongValue1;

    public TestObjectLong(long value1) {
        LongValue1 = value1;
    }


    @Override
    public void serialize(ArrayWriter writer) {
        writer.writeLong(LongValue1);
    }

    @Override
    public void deserialize(ArrayReader reader) {
        LongValue1 = reader.readLong();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectLong) {
            TestObjectLong obj = (TestObjectLong) other;

            if (obj.LongValue1 != this.LongValue1) return false;

            return true;
        }
        return false;
    }

    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(LongValue1);

        return stringBuilder.toString();
    }

}
