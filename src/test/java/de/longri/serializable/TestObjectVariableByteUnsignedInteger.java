package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectVariableByteUnsignedInteger extends Serializable {

    protected int IntegerValue1;
    protected int IntegerValue2;

    @Override
    public void serialize(ArrayWriter writer) {
        writer.writeVariableUnsignedInt(IntegerValue1);
        writer.writeVariableUnsignedInt(IntegerValue2);
    }

    @Override
    public void deserialize(ArrayReader reader) {
        IntegerValue1 = reader.readVariableUnsignedInt();
        IntegerValue2 = reader.readVariableUnsignedInt();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectVariableByteUnsignedInteger) {
            TestObjectVariableByteUnsignedInteger obj = (TestObjectVariableByteUnsignedInteger) other;

            if (obj.IntegerValue1 != this.IntegerValue1) return false;
            if (obj.IntegerValue2 != this.IntegerValue2) return false;

            return true;
        }
        return false;
    }
}
