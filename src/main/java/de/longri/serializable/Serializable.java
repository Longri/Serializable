package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public abstract class Serializable {

    public Serializable() {

    }

    public Serializable(byte[] array) {
        deserialize(array);
    }

    public abstract byte[] serialize();

    public abstract void deserialize(byte[] array);
}
