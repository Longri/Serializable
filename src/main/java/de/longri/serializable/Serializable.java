package de.longri.serializable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Longri on 03.11.15.
 */
public abstract class Serializable {

    public Serializable() {

    }

    public Serializable(StoreBase reader) {
        deserialize(reader);
    }

    public abstract void serialize(StoreBase writer);

    public abstract void deserialize(StoreBase reader);


}
