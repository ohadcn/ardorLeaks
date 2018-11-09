package com.ohadcn.ardor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.util.Convert;

public class GenKeyGui {
    public static void main(String[] args) {
        if(args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("USAGE: " + args[0] + " <password> <chunk number>");
        }
        int chunk = Integer.valueOf(args[1]);

        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a password:");
        JPasswordField pass = new JPasswordField(10);
        JTextField num = new JTextField(10);
        panel.add(label);
        panel.add(pass);

        panel.add(new JLabel("Number of chunks:"));
        panel.add(num);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "The title",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
        if(option == 0) // pressing OK button
        {
            char[] password = pass.getPassword();
            args[0] = new String(password);
            System.out.println("Your password is: " + args[0]);
            chunk = Integer.valueOf(new String(num.getText()));
        }

        byte[] pubkey = Crypto.getPublicKey(args[0]);
        byte[] derivationKey = Crypto.getPrivateKey(Crypto.sha3().digest(pubkey));

        for(int i = 0; i <= chunk; i++) {
            pubkey = Crypto.getSharedKey(pubkey, derivationKey);
        }

        String id = Convert.rsAccount(Account.getId(pubkey));

        System.out.println("account to pay: " + id);

        String key = Convert.toHexString(pubkey);
        System.out.println("pubkey: " + Convert.toHexString(pubkey));

        JOptionPane.showMessageDialog(null, "The key to publish is: " + key
                , "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(key), null);


    }
}
