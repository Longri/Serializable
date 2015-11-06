package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectByte extends Serializable {

    protected byte byteValue1 = 0;
    protected byte byteValue2 = 0;
    protected byte byteValue3 = 0;


    @Override
    public void serialize(StoreBase writer) throws NotImplementedException {
        writer.write(byteValue1);
        writer.write(byteValue2);
        writer.write(byteValue3);
    }

    @Override
    public void deserialize(StoreBase reader) throws NotImplementedException {
        byteValue1 = reader.readByte();
        byteValue2 = reader.readByte();
        byteValue3 = reader.readByte();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectByte) {
            TestObjectByte obj = (TestObjectByte) other;

            if (obj.byteValue1 != this.byteValue1) return false;
            if (obj.byteValue2 != this.byteValue2) return false;
            if (obj.byteValue3 != this.byteValue3) return false;

            return true;
        }
        return false;
    }
}
