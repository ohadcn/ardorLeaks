package com.ohadcn.ardor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.util.Convert;

public class MakeLeakGui {
    public static void main(String[] args) {
        if(args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("USAGE: " + args[0] + " <filename> <chunks> <privkey>");
        }

        String fileToEncrypt = args[0];
        int chunks = Integer.valueOf(args[1]);

        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a password:");
        JPasswordField pass = new JPasswordField(10);
//        JTextField num = new JTextField(10);
        panel.add(label);
        panel.add(pass);

//        panel.add(new JLabel("Number of chunks:"));
//        panel.add(num);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "The title",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
        if(option == 0) // pressing OK button
        {
            char[] password = pass.getPassword();
            args[2] = new String(password);
            System.out.println("Your password is: " + args[2]);
//            chunks = Integer.valueOf(new String(num.getText()));
        }

//        JLabel fileLabel = new JLabel("Choose a file to encrypt:");
        JFileChooser fileChooser = new JFileChooser("Choose a file to encrypt:");
//        panel.add(fileLabel);
//        panel.add(fileChooser);
        panel = new JPanel();
//        panel.add(fileLabel);
        panel.add(fileChooser);
        option = JOptionPane.showOptionDialog(null, panel, "The title",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
        if(option == 0) // pressing OK button
        {
            fileToEncrypt = fileChooser.getSelectedFile().getAbsolutePath();
            System.out.println("Your password is: " + fileToEncrypt);
        }


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

        byte[] pubkey = Crypto.getPublicKey(args[2]);
        byte[] derivationKey = Crypto.getPrivateKey(Crypto.sha3().digest(pubkey));
//        long id = Account.getId(pubkey);
        String id = Convert.rsAccount(Account.getId(pubkey));

        System.out.println("account to pay: " + id);

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
        JOptionPane.showMessageDialog(null, "your file created successfully\nYour address is: " + id +
                "\nand your password is " + args[2], "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(id), null);
    }
}
