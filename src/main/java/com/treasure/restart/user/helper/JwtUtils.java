
package com.treasure.restart.user.helper;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtUtils(
            @Value("${jwt.private-key-path}") String privateKeyPath,
            @Value("${jwt.public-key-path}") String publicKeyPath
    ) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(readKey(privateKeyPath, "PRIVATE KEY")));
            this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(readKey(publicKeyPath, "PUBLIC KEY")));
        } catch (Exception e) {
            throw new IllegalStateException("JWT RSA 密钥加载失败", e);
        }
    }

    /**
     * 使用 RSA 私钥签发 JWT
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + EXPIRE_TIME);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expire)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * 使用 RSA 公钥验证 JWT，并获取用户 ID
     */
    public Long getUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.valueOf(subject);
    }

    /**
     * 读取 PEM 格式密钥
     */
    private byte[] readKey(String keyPath, String keyType) throws Exception {
        String pem = Files.readString(Path.of(keyPath));

        pem = pem
                .replace("-----BEGIN " + keyType + "-----", "")
                .replace("-----END " + keyType + "-----", "")
                .replaceAll("\\s", "");

        return Base64.getDecoder().decode(pem);
    }
}
