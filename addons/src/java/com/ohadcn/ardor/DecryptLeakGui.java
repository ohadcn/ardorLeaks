package com.ohadcn.ardor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import nxt.crypto.Crypto;
import nxt.util.Convert;

public class DecryptLeakGui {
    public static void main(String[] args) {
        if (args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("USAGE: " + args[0] + " <filename> <chunks> <privkey>");
        }

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
            args[1] = new String(password);
            System.out.println("Your password is: " + args[1]);
//            chunks = Integer.valueOf(new String(num.getText()));
        }

//        JLabel fileLabel = new JLabel("Choose a file to encrypt:");
        JFileChooser fileChooser = new JFileChooser("Choose a file to decrypt:");
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
            args[0] = fileChooser.getSelectedFile().getAbsolutePath();
            System.out.println("Your password is: " + args[0]);
        }
        String fileToDecrypt = args[0];
        OutputStream os;
        ZipFile zip;
        try {
            os = new FileOutputStream(fileToDecrypt.replace(".zip", ".orig"));
            zip = new ZipFile(fileToDecrypt );
        } catch (FileNotFoundException e) {
            System.out.println("file " + fileToDecrypt +" not found");
            return;
        } catch (IOException e) {
            System.out.println("can't read from " + fileToDecrypt);
            return;
        }

        byte[] pubkey;
        try {
            pubkey = Convert.parseHexString(args[1]);
        } catch (NumberFormatException _) {
            pubkey = Crypto.getPublicKey(args[1]);
        }
        byte[] derivationKey = Crypto.getPrivateKey(Crypto.sha3().digest(pubkey));

        Enumeration<? extends ZipEntry> entries = zip.entries();

        try {
            while (entries.hasMoreElements()) {
                pubkey = Crypto.getSharedKey(pubkey, derivationKey);
                ZipEntry entry = entries.nextElement();
                InputStream is = zip.getInputStream(entry);
                int available = is.available();
                byte[] chunk = new byte[available];
                int count = 0;
                while((count += is.read(chunk, count, available - count)) < available) {}
                chunk = Crypto.aesDecrypt(chunk, pubkey);
                os.write(chunk);
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

        JOptionPane.showMessageDialog(null, "The decrypted file found in: " + fileToDecrypt
                , "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);


    }
}
