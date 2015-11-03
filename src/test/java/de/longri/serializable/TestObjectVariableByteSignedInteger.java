package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectVariableByteSignedInteger extends Serializable {

    protected int IntegerValue1;
    protected int IntegerValue2;


    @Override
    public byte[] serialize() {

        ArrayWriter writer = new ArrayWriter();

        writer.writeVariableSignedInt(IntegerValue1);
        writer.writeVariableSignedInt(IntegerValue2);


        return writer.getArray();
    }

    @Override
    public void deserialize(byte[] array) {

        ArrayReader reader = new ArrayReader(array);
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
