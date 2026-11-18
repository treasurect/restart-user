package com.treasure.restart.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@SpringBootTest
class RestartUserApplicationTests {

    private static final KeyPair TEST_KEY_PAIR = generateTestKeyPair();
    private static final Path PRIVATE_KEY_PATH = writeTestKeyFile("private", "PRIVATE KEY", TEST_KEY_PAIR.getPrivate().getEncoded());
    private static final Path PUBLIC_KEY_PATH = writeTestKeyFile("public", "PUBLIC KEY", TEST_KEY_PAIR.getPublic().getEncoded());

    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.password", () -> "test");
        registry.add("jwt.private-key-path", () -> PRIVATE_KEY_PATH.toString());
        registry.add("jwt.public-key-path", () -> PUBLIC_KEY_PATH.toString());
    }

    @Test
    void contextLoads() {
    }

    private static KeyPair generateTestKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("生成测试 RSA 密钥失败", e);
        }
    }

    private static Path writeTestKeyFile(String name, String pemType, byte[] encoded) {
        try {
            Path path = Files.createTempFile("restart-user-test-" + name + "-", ".pem");
            Files.writeString(path, toPem(pemType, encoded));
            path.toFile().deleteOnExit();
            return path;
        } catch (Exception e) {
            throw new IllegalStateException("写入测试 RSA 密钥失败", e);
        }
    }

    private static String toPem(String pemType, byte[] encoded) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .encodeToString(encoded);
        return "-----BEGIN " + pemType + "-----\n" + base64 + "\n-----END " + pemType + "-----";
    }

}
