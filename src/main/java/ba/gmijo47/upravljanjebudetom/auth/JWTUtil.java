package ba.gmijo47.upravljanjebudetom.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JWTUtil {


    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-token.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public SecretKey getSigningKey() {
        return new SecretKeySpec(
                secret.getBytes(),
                "HmacSHA256"
        );
    }

    public String generateRefreshToken(Integer userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(new SecretKeySpec(refreshSecret.getBytes(), "HmacSHA256"))
                .compact();
    }

    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
