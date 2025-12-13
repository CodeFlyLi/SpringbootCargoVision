package com.app.springbootcargovision.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT (JSON Web Token) 工具类
 * 负责生成、解析和验证 JWT Token
 */
@Component
public class JwtUtils {
    // 签名密钥，生产环境应存储在配置文件中
    // 这里为了演示使用自动生成的安全密钥 (HMAC-SHA256)
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token 过期时间：24小时 (毫秒)
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * 生成 Token
     * 
     * @param username 用户名
     * @param role     用户角色
     * @return 加密后的 JWT 字符串
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // 将角色信息存入 Token 载荷
        // 添加签发时间 (iat)
        claims.put("iat", new Date(System.currentTimeMillis()));
        return createToken(claims, username);
    }

    /**
     * 创建 Token 的内部方法
     * 设置 Claims (载荷)、Subject (主体/用户名)、IssuedAt (签发时间)、Expiration (过期时间)
     * 并使用密钥进行签名
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    /**
     * 验证 Token 是否有效
     * 1. 用户名是否匹配
     * 2. Token 是否过期
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 从 Token 中提取用户名 (Subject)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 Token 中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从 Token 中提取签发时间 (IssuedAt)
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * 通用方法：提取 Token 中的某个 Claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析 Token 获取所有的 Claims
     * 这里会验证签名，如果签名无效或 Token 格式错误会抛出异常
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token).getBody();
    }

    /**
     * 检查 Token 是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 从 Token 中提取角色信息
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}
