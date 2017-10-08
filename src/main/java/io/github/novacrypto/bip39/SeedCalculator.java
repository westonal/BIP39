/*
 *  BIP39 library, a Java implementation of BIP39
 *  Copyright (C) 2017 Alan Evans, NovaCrypto
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Original source: https://github.com/NovaCrypto/BIP39
 *  You can contact the authors via github issues.
 */

package io.github.novacrypto.bip39;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.Normalizer;
import java.util.Arrays;

public final class SeedCalculator {

    private final byte[] fixedSalt = getUtf8Bytes("mnemonic");
    private SecretKeyFactory skf = getPbkdf2WithHmacSHA512();

    public byte[] calculateSeed(String mnemonic, String passphrase) {
        mnemonic = Normalizer.normalize(mnemonic, Normalizer.Form.NFKD);
        passphrase = Normalizer.normalize(passphrase, Normalizer.Form.NFKD);

        final char[] chars = mnemonic.toCharArray();
        final byte[] salt2 = getUtf8Bytes(passphrase);
        final byte[] salt = combine(fixedSalt, salt2);
        clear(salt2);
        final PBEKeySpec spec = new PBEKeySpec(chars, salt, 2048, 512);
        Arrays.fill(chars, '\0');
        clear(salt);

        try {
            return skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] combine(byte[] array1, byte[] array2) {
        final byte[] bytes = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, bytes, 0, array1.length);
        System.arraycopy(array2, 0, bytes, array1.length, bytes.length - array1.length);
        return bytes;
    }

    private static void clear(byte[] salt) {
        Arrays.fill(salt, (byte) 0);
    }

    private static SecretKeyFactory getPbkdf2WithHmacSHA512() {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getUtf8Bytes(final String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}