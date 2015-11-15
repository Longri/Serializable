package de.longri.bench;

import de.longri.serializable.NotImplementedException;
import de.longri.serializable.Serializable;
import de.longri.serializable.StoreBase;

import java.util.Date;


/**
 * Created by Longri on 15.11.15.
 */
public class WeatherInfo implements Serializable {

    byte temp;
    byte IconId;
    Date date;


    @Override
    public void serialize(StoreBase writer) throws NotImplementedException {
        writer.write(temp);
        writer.write(IconId);
        writer.write(date.getTime());
    }

    @Override
    public void deserialize(StoreBase reader) throws NotImplementedException {
        temp = reader.readByte();
        IconId = reader.readByte();
        date = new Date(reader.readLong());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof WeatherInfo) {
            WeatherInfo obj = (WeatherInfo) other;

            if (obj.temp != this.temp) return false;
            if (obj.IconId != this.IconId) return false;
            if (obj.date.getTime() != this.date.getTime()) return false;
            return true;
        }
        return false;
    }
}
