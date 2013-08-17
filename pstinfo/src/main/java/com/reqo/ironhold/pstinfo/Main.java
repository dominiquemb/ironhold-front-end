package com.reqo.ironhold.pstinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * User: ilya
 * Date: 8/14/13
 * Time: 12:10 AM
 */
public class Main {
    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("\nEnter pst file (type exit to stop):");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String s = bufferRead.readLine();
            if ("exit".equalsIgnoreCase(s)) {
                System.exit(1);
            }

            File f = new File(s);
            if (f.exists()) {
                PSTInfo pstInfo = new PSTInfo(f);
                pstInfo.dump(System.out);
            }


        }
    }
}
