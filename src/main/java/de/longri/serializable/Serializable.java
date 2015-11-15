package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public interface Serializable {

    public abstract void serialize(StoreBase writer) throws NotImplementedException;

    public abstract void deserialize(StoreBase reader) throws NotImplementedException;


}
