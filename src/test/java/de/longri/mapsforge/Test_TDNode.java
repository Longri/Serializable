package de.longri.mapsforge;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;


/**
 * Created by Longri on 15.11.15.
 */
public class Test_TDNode {

    @Test
    public void testTDNode_Memory() throws Exception {
    /*
        For Berlin Map creation wie create 350.000 instance of class TDNode!
        39.000 TDNode instance have a name property!
        This name are stored (for test) in resources/StringList.txt

    */

        //Reset memory measurement
        MemoryUsage.resetMemoryUsage();


        //create store array
        CB_TDNode[] array = new CB_TDNode[350000];


        // create 311.000 instances without a name property
        int latitude = 53000000;
        int longitude = 13000000;
        int id = 0;
        for (id = 0; id < 310999; id++) {
            array[id] = new CB_TDNode(id, latitude++, longitude++, (short) 0, (byte) 18, null, null);
            MemoryUsage.chekMemory();
        }

        //create 39.000 instance with names, readed from resources/StringList.txt
        File nameFile = new File("src/test/resources/StringList.txt");
        id++;
        try (BufferedReader br = new BufferedReader(new FileReader(nameFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                array[id++] = new CB_TDNode(id, latitude++, longitude++, (short) 0, (byte) 18, null, line);
                MemoryUsage.chekMemory();
            }
        }

        String usedMem = readableFileSize(MemoryUsage.getMaxMemory());
        System.out.print("Used Memory =" + usedMem);


    }


    // Reference: http://stackoverflow.com/a/5599842
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


}
