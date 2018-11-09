package com.ohadcn.ardor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.util.Convert;

public class DecryptChunk {
    public static void main(String[] args) {
        if(args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("USAGE: " + args[0] + " <key> <file>");
        }

        byte[] pubkey = Convert.parseHexString(args[0]);

        try {
            FileInputStream is = new FileInputStream(args[1]);
            int len = is.available(), count = 0;
            byte[] data = new byte[len];
            while ((count += is.read(data, count, len-count)) < len) {System.out.println(count);}
            System.out.println(count +" " + data.length);
            data = Crypto.aesDecrypt(data, pubkey);
            FileOutputStream out = new FileOutputStream(args[1] + ".dec");
            out.write(data);
            is.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
