package com.shrija.orchestrator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Minimal, dependency-free HS256 JWT verifier (uses only javax.crypto / java.util.Base64, no
 * external JWT library) so this module has no new third-party dependency to compile against. It
 * verifies exactly what the Auth Service (:8081) is expected to issue: a standard
 * header.payload.signature HS256 token. Rejects anything with a different/unsupported algorithm, a
 * bad signature, or an expired "exp" claim.
 */
@Component
public class JwtUtil {

  private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public Map<String, Object> verifyAndExtractClaims(String token, String secret) {
    if (token == null || token.isBlank()) {
      throw new JwtAuthenticationException("Missing token.");
    }
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new JwtAuthenticationException("Malformed JWT.");
    }

    Map<String, Object> header = decodeJson(parts[0]);
    Object alg = header.get("alg");
    if (!"HS256".equals(alg)) {
      throw new JwtAuthenticationException("Unsupported JWT algorithm: " + alg);
    }

    byte[] expectedSignature = sign(parts[0] + "." + parts[1], secret);
    byte[] actualSignature = URL_DECODER.decode(parts[2]);
    if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
      throw new JwtAuthenticationException("JWT signature verification failed.");
    }

    Map<String, Object> claims = decodeJson(parts[1]);
    Object exp = claims.get("exp");
    if (exp instanceof Number number && Instant.now().getEpochSecond() > number.longValue()) {
      throw new JwtAuthenticationException("JWT has expired.");
    }
    return claims;
  }

  private byte[] sign(String data, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    } catch (Exception ex) {
      throw new JwtAuthenticationException("Unable to verify JWT signature.", ex);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> decodeJson(String base64UrlSegment) {
    try {
      byte[] json = URL_DECODER.decode(base64UrlSegment);
      return objectMapper.readValue(json, Map.class);
    } catch (Exception ex) {
      throw new JwtAuthenticationException("Malformed JWT segment.", ex);
    }
  }
}
