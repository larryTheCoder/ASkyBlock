/*
 * Copyright (C) 2017 larryTheCoder
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.larryTheCoder.safecontrol;

import com.larryTheCoder.utils.Utils;
import java.security.Key;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * This method will give full permission to access full point of this plugin.
 * <br>
 * <br>
 * This method will inform the OPS and the CONSOLE about the player who
 * developed this plugin.
 * <br>
 * <br>
 * Without full password and ID, player cant access to this point unless it is
 * Qualified as MODERATOR. For YOUR protection we has removed the MAIN CLASS
 * until the next release. This future are not available on public
 * <br>
 * <br>
 * WITH THIS METHOD ME OR OTHER MODERATORS CAN ACCESS OP, VERSION, PLUGINS, AND
 * LOGGING CONFIG, AS IS THE PLUGIN PROGRAMS, WE WILL NOT STEALING ANY
 * INFORMATION IN THE NAME GOD.
 *
 * @author larryTheCoder
 */
//public class SHAHashingExample {
//
//    public String encryptionKey;
//
//    public static void start() {
//        Utils.ConsoleMsg("DEBUG 1: Prepare to jump into a new ERA of inventions");
//        String st = SHAHashingExample.encrypt("123456", "larryZ00p", "Prepare to jump into a new ERA of inventions");
//        Utils.ConsoleMsg("DEBUG 2: [CR] " + st);
//        String decode = SHAHashingExample.decrypt("123456", "larryZ00p", st);
//        Utils.ConsoleMsg("DEBUG 2: [DE] " + decode);
//    }
//
//    /**
//     * This method encrypt a <code>String</code> into a 128-BIT AES
//     *
//     * @param key The key for the encrypted message
//     * @param initVector
//     * @param value The value of the encrypted message
//     * @return <code>String</code> if there no exception were throw otherwise it
//     * will return <code>null</code>
//     */
//    public static String encrypt(String key, String initVector, String value) {
//        try {
//            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
//            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
//
//            byte[] encrypted = cipher.doFinal(value.getBytes());
//            Utils.ConsoleMsg("encrypted string: "
//                    + Base64.encodeBase64String(encrypted));
//
//            return Base64.encodeBase64String(encrypted);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//
//    public static String decrypt(String key, String initVector, String encrypted) {
//        try {
//            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
//            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
//
//            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
//
//            return new String(original);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//}
public class SHAHashingExample {

    private static byte[] keyValue;

    public static void start() {
        try {
            String password = "123456";

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());

            byte[] byteData = md.digest();
            byte[] key = new byte[byteData.length / 2];

            for (int I = 0; I < key.length; I++) {
                // Choice 1 for using only 128 bits of the 256 generated
                key[I] = byteData[I];
            }
            keyValue = key;

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length / 2; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            Utils.ConsoleMsg("Hex format : " + sb.toString());

            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length / 2; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            Utils.ConsoleMsg("Hex format : " + hexString.toString());

            String k = "ZAQWAN BODOH GILE";
            String f = encrypt(k);
            Utils.ConsoleMsg(f);
            String j = decrypt(f);
            Utils.ConsoleMsg(j);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        return key;
    }
}
