package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectInteger extends Serializable {

    protected int IntegerValue1;
    protected int IntegerValue2;


    @Override
    public void serialize(StoreBase writer) {
        writer.write(IntegerValue1);
        writer.write(IntegerValue2);
    }

    @Override
    public void deserialize(StoreBase reader) {
        IntegerValue1 = reader.readInt();
        IntegerValue2 = reader.readInt();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectInteger) {
            TestObjectInteger obj = (TestObjectInteger) other;

            if (obj.IntegerValue1 != this.IntegerValue1) return false;
            if (obj.IntegerValue2 != this.IntegerValue2) return false;

            return true;
        }
        return false;
    }
}
