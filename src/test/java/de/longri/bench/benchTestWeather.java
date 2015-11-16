package de.longri.bench;

import de.longri.serializable.Analyse;
import de.longri.serializable.SerializableArrayList;
import org.junit.Test;

import java.util.Date;


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

        Analyse analyse = new Analyse(serializeList);
        analyse.printAnalyse();

    }


}
