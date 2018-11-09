package com.ohadcn.ardor;

import org.bouncycastle.util.Arrays;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import nxt.account.Account;
import nxt.crypto.Crypto;

public class MakeLeak {
    public static void main(String[] args) {
        if(args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("USAGE: " + args[0] + " <filename> <chunks> <privkey>");
        }
        String fileToEncrypt = args[0];
        int chunks = Integer.valueOf(args[1]);

        InputStream is;
        ZipOutputStream zip;
        int chunkSize, fileSize;
        try {
            is = new FileInputStream(fileToEncrypt);
            zip = new ZipOutputStream(new FileOutputStream(fileToEncrypt + ".zip"));
            fileSize = is.available();
            chunkSize = fileSize / chunks;
        } catch (FileNotFoundException e) {
            System.out.println("file " + fileToEncrypt +" not found");
            return;
        } catch (IOException e) {
            System.out.println("can't read from " + fileToEncrypt);
            return;
        }

//        byte[] privkey = Crypto.getPrivateKey(args[2]);
        byte[] pubkey = Crypto.getPublicKey(args[2]);
        byte[] derivationKey = Crypto.getPrivateKey(Crypto.sha3().digest(pubkey));
        long id = Account.getId(pubkey);

        System.out.println("account to pay to: " + Long.toUnsignedString(id));

        byte[] chunk = new byte[chunkSize], buffer = new byte[0], temp;
        for(int i = 0; i < chunks; i++) {
            try {
                pubkey = Crypto.getSharedKey(pubkey, derivationKey);
                int size = is.read(chunk);
                if(size != chunk.length) {
                    byte[] buf = new byte[size];
                    System.arraycopy(chunk, 0, buf, 0, size);
                    chunk = buf;
                }
                temp = Crypto.aesEncrypt(chunk, pubkey);
                zip.putNextEntry(new ZipEntry("f" + i));
                zip.write(temp);
                zip.closeEntry();
            } catch (IOException e) {
                System.out.println("can't read from " + fileToEncrypt);
                return;
            }
        }
        try {
            zip.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
