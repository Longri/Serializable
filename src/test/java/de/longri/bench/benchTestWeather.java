package de.longri.bench;

import de.longri.serializable.*;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;


/**
 * Created by Longri on 15.11.15.
 */
public class benchTestWeather {
    WeatherInfo wi1 = new WeatherInfo();
    WeatherInfo wi2 = new WeatherInfo();
    WeatherInfo wi3 = new WeatherInfo();

    @Test
    public void testWeatherInfo() throws Exception {

        wi1.temp = 65;
        wi1.IconId = 27;
        wi1.date = new Date();


        wi2.temp = 63;
        wi2.IconId = 21;
        wi2.date = new Date();


        wi3.temp = 55;
        wi3.IconId = 13;
        wi3.date = new Date();


        SerializableArrayList<WeatherInfo> serializeList = new SerializableArrayList<WeatherInfo>(WeatherInfo.class);

        serializeList.add(wi1);
        serializeList.add(wi2);
        serializeList.add(wi3);

        NormalStore(serializeList);
        BitStore(serializeList);
        ZippedBitStore(serializeList);
        ZippedNormalStore(serializeList);

    }

    private void NormalStore(SerializableArrayList<WeatherInfo> serializeList) throws Exception {
        NormalStore writer = new NormalStore();
        serializeList.serialize(writer);
        byte[] byteArray = writer.getArray();
        SerializableArrayList<WeatherInfo> deserializeList = new SerializableArrayList<WeatherInfo>(WeatherInfo.class);
        NormalStore reader = new NormalStore(byteArray);
        deserializeList.deserialize(reader);
        assertEquals(wi1, deserializeList.get(0));
        assertEquals(wi2, deserializeList.get(1));
        assertEquals(wi3, deserializeList.get(2));
        System.out.println("NormalStore:" + byteArray.length + " bytes");
    }

    private void BitStore(SerializableArrayList<WeatherInfo> serializeList) throws Exception {
        BitStore writer = new BitStore();
        serializeList.serialize(writer);
        byte[] byteArray = writer.getArray();
        SerializableArrayList<WeatherInfo> deserializeList = new SerializableArrayList<WeatherInfo>(WeatherInfo.class);
        BitStore reader = new BitStore(byteArray);
        deserializeList.deserialize(reader);
        assertEquals(wi1, deserializeList.get(0));
        assertEquals(wi2, deserializeList.get(1));
        assertEquals(wi3, deserializeList.get(2));
        System.out.println("BitStore:" + byteArray.length + " bytes");
    }

    private void ZippedBitStore(SerializableArrayList<WeatherInfo> serializeList) throws Exception {
        ZippedBitStore writer = new ZippedBitStore();
        serializeList.serialize(writer);
        byte[] byteArray = writer.getArray();
        SerializableArrayList<WeatherInfo> deserializeList = new SerializableArrayList<WeatherInfo>(WeatherInfo.class);
        ZippedBitStore reader = new ZippedBitStore(byteArray);
        deserializeList.deserialize(reader);
        assertEquals(wi1, deserializeList.get(0));
        assertEquals(wi2, deserializeList.get(1));
        assertEquals(wi3, deserializeList.get(2));
        System.out.println("ZippedBitStore:" + byteArray.length + " bytes");
    }

    private void ZippedNormalStore(SerializableArrayList<WeatherInfo> serializeList) throws Exception {
        ZippedNormalStore writer = new ZippedNormalStore();
        serializeList.serialize(writer);
        byte[] byteArray = writer.getArray();
        SerializableArrayList<WeatherInfo> deserializeList = new SerializableArrayList<WeatherInfo>(WeatherInfo.class);
        ZippedNormalStore reader = new ZippedNormalStore(byteArray);
        deserializeList.deserialize(reader);
        assertEquals(wi1, deserializeList.get(0));
        assertEquals(wi2, deserializeList.get(1));
        assertEquals(wi3, deserializeList.get(2));
        System.out.println("ZippedNormalStore:" + byteArray.length + " bytes");
    }

}
