package project;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public final class Encryption {

    private static final byte[] salt = {0, 5, 0, 9, 2, 0, 1, 5};
    private static final byte[] hash = {81, -51, -72, -7, 58, -118, -72, 4, -102, -97, -51, 58, 0, -19, -4, 105};

    protected static boolean encrypting(final char[] password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final byte[] hashPassword = factory.generateSecret(spec).getEncoded();

        return Arrays.equals(hash, hashPassword);
    }
}
