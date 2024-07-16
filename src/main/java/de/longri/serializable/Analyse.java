package de.longri.serializable;

/**
 * Created by Hoepfner on 16.11.2015.
 */
public class Analyse {

    private final Serializable seri;

    public Analyse(Serializable seri) {
        this.seri = seri;
    }


    public void printAnalyse() {
        printTime("NormalStore             = ", this.seri, new NormalStore());
        printTime("VariableByteStore       = ", this.seri, new VariableByteStore());
        printTime("BitStore                = ", this.seri, new BitStore());
        printTime("ZippedNormalStore       = ", this.seri, new ZippedNormalStore());
        printTime("ZippedBitStore          = ", this.seri, new ZippedBitStore());
        printTime("BitStoreZippedString    = ", this.seri, new BitStoreZippedString());
        printTime("ZippedVariableByteStore = ", this.seri, new ZippedVariableByteStore());
    }

    public void printTime(String name, Serializable seri, StoreBase store) {
        long start = System.currentTimeMillis();
        String res = name + Integer.toString(getLength(seri, store)) + " bytes";
        long timeMillis = System.currentTimeMillis() - start;
        System.out.println(res + " at " + timeMillis + " ms");
    }

    private int getLength(Serializable seri, StoreBase store) {
        try {
            seri.serialize(store);
            return store.getArray().length;
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
