package com.ohadcn.ardor;

import org.bouncycastle.util.Arrays;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import nxt.account.Account;
import nxt.crypto.Crypto;

public class DecryptLeak {
    public static void main(String[] args) {
        if (args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("USAGE: " + args[0] + " <filename> <chunks> <privkey>");
        }
        String fileToDecrypt = args[0];
        OutputStream os;
        ZipFile zip;
        int chunkSize, fileSize;
        try {
            os = new FileOutputStream(fileToDecrypt + ".orig");
            zip = new ZipFile(fileToDecrypt + ".zip");
        } catch (FileNotFoundException e) {
            System.out.println("file " + fileToDecrypt +" not found");
            return;
        } catch (IOException e) {
            System.out.println("can't read from " + fileToDecrypt);
            return;
        }

        byte[] pubkey = Crypto.getPublicKey(args[1]);
        byte[] derivationKey = Crypto.getPrivateKey(Crypto.sha3().digest(pubkey));

        Enumeration<? extends ZipEntry> entries = zip.entries();

        try {
            while (entries.hasMoreElements()) {
                pubkey = Crypto.getPrivateKey(Crypto.sha3().digest(Arrays.concatenate(pubkey, derivationKey)));
                ZipEntry entry = entries.nextElement();
                InputStream is = zip.getInputStream(entry);
                int available = is.available(), padding = 16 - available % 16;
                byte[] chunk = new byte[available + padding];
                is.read(chunk);
                chunk = Crypto.aesDecrypt(chunk, pubkey);
                os.write(chunk, 0, available);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            zip.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
