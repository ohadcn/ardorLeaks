package com.ohadcn.ardor;

import java.util.Arrays;

import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.util.Convert;

public class GenKey {
    public static void main(String[] args) {
        if(args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("USAGE: " + args[0] + " <password> <chunk number>");
        }
        int chunk = Integer.valueOf(args[1]);

        byte[] pubkey = Crypto.getPublicKey(args[0]);
        byte[] derivationKey = Crypto.getPrivateKey(Crypto.sha3().digest(pubkey));

        for(int i = 0; i <= chunk; i++) {
            pubkey = Crypto.getSharedKey(pubkey, derivationKey);
        }

        String id = Convert.rsAccount(Account.getId(pubkey));

        System.out.println("account to pay: " + id);

        System.out.println("pubkey: " + Convert.toHexString(pubkey));

    }
}
