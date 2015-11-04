package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectVariableByteSignedInteger extends Serializable {

    protected int IntegerValue1;
    protected int IntegerValue2;


    @Override
    public void serialize(ArrayWriter writer) {
        writer.writeVariableSignedInt(IntegerValue1);
        writer.writeVariableSignedInt(IntegerValue2);
    }

    @Override
    public void deserialize(ArrayReader reader) {
        IntegerValue1 = reader.readVariableSignedInt();
        IntegerValue2 = reader.readVariableSignedInt();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectVariableByteSignedInteger) {
            TestObjectVariableByteSignedInteger obj = (TestObjectVariableByteSignedInteger) other;

            if (obj.IntegerValue1 != this.IntegerValue1) return false;
            if (obj.IntegerValue2 != this.IntegerValue2) return false;

            return true;
        }
        return false;
    }
}
