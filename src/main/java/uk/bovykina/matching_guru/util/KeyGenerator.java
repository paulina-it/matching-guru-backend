package uk.bovykina.matching_guru.util;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;

public class KeyGenerator {
    public static void main(String[] args) {
        var key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Generated Key: " + base64Key);
    }
}