package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectShort extends Serializable {

    protected short shortValue1 = 0;
    protected short shortValue2 = 0;
    protected short shortValue3 = 0;


    @Override
    public void serialize(StoreBase writer) throws NotImplementedException {
        writer.write(shortValue1);
        writer.write(shortValue2);
        writer.write(shortValue3);
    }

    @Override
    public void deserialize(StoreBase reader) throws NotImplementedException {
        shortValue1 = reader.readShort();
        shortValue2 = reader.readShort();
        shortValue3 = reader.readShort();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectShort) {
            TestObjectShort obj = (TestObjectShort) other;

            if (obj.shortValue1 != this.shortValue1) return false;
            if (obj.shortValue2 != this.shortValue2) return false;
            if (obj.shortValue3 != this.shortValue3) return false;

            return true;
        }
        return false;
    }
}
