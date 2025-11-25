package com.chayasadler.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component

public class JwtUtil {

    @Value("${JWT_PUBLIC_KEY}")
    private String publicKey;

    public String getCustomerId() {
        return customerId;
    }

    private String customerId;

    private RSAPublicKey genKey() {

        String key = publicKey.replaceAll("-----BEGIN PUBLIC KEY-----","")
                .replaceAll("-----END PUBLIC KEY-----","")
                .replaceAll("\\s","");

        byte[] decodePublicKey = Base64.getDecoder().decode(key);
        X509EncodedKeySpec  spec = new X509EncodedKeySpec(decodePublicKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        //gets bunch of claims
        Claims claims = extractClaims(token);

        //takes all the claims and returns only subject
        return claimResolver.apply(claims);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(genKey())
                .build()
                .parseSignedClaims(token).getPayload();

    }

    public Boolean validateToken(String token) {
        if(isTokenExpired(token))
            return false;
        else {
            customerId = extractCustomerId(token);
            return true;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());

    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String extractCustomerId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

}
