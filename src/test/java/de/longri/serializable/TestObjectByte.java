package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectByte extends Serializable {

    protected byte value1 = 0;
    protected byte value2 = 0;
    protected byte value3 = 0;
    protected byte value4 = 0;
    protected byte value5 = 0;
    protected byte value6 = 0;
    protected byte value7 = 0;
    protected byte value8 = 0;
    protected byte value9 = 0;
    protected byte value10 = 0;


    @Override
    public void serialize(StoreBase writer) throws NotImplementedException {
        writer.write(value1);
        writer.write(value2);
        writer.write(value3);
        writer.write(value4);
        writer.write(value5);
        writer.write(value6);
        writer.write(value7);
        writer.write(value8);
        writer.write(value9);
        writer.write(value10);
    }

    @Override
    public void deserialize(StoreBase reader) throws NotImplementedException {
        value1 = reader.readByte();
        value2 = reader.readByte();
        value3 = reader.readByte();
        value4 = reader.readByte();
        value5 = reader.readByte();
        value6 = reader.readByte();
        value7 = reader.readByte();
        value8 = reader.readByte();
        value9 = reader.readByte();
        value10 = reader.readByte();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectByte) {
            TestObjectByte obj = (TestObjectByte) other;

            if (obj.value1 != this.value1) return false;
            if (obj.value2 != this.value2) return false;
            if (obj.value3 != this.value3) return false;
            if (obj.value4 != this.value4) return false;
            if (obj.value5 != this.value5) return false;
            if (obj.value6 != this.value6) return false;
            if (obj.value7 != this.value7) return false;
            if (obj.value8 != this.value8) return false;
            if (obj.value9 != this.value9) return false;
            if (obj.value10 != this.value10) return false;


            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("value1=" + value1 + "\n");
        sb.append("value2=" + value2 + "\n");
        sb.append("value3=" + value3 + "\n");
        sb.append("value4=" + value4 + "\n");
        sb.append("value5=" + value5 + "\n");
        sb.append("value6=" + value6 + "\n");
        sb.append("value7=" + value7 + "\n");
        sb.append("value8=" + value8 + "\n");
        sb.append("value9=" + value9 + "\n");
        sb.append("value10=" + value10 + "\n");
        return sb.toString();
    }
}
