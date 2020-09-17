package com.paypay.baymax.front.config.security.jwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class JWTTokenUtils {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final JWTSettings jwtSettings;
	private final JWTSettingsResetPassword jwtSettingsRPwd;

	public JWTTokenUtils(JWTSettings jwtSettings, JWTSettingsResetPassword jwtSettingsRPwd) {
		this.jwtSettings = jwtSettings;
		this.jwtSettingsRPwd = jwtSettingsRPwd;
	}

	/**
	 * 
	 * @param tokenSigningKey
	 * @param tokenPreFix
	 * @param token
	 * @return
	 */
	public Claims validateTokenClaims(String tokenSigningKey, String tokenPreFix, String token)
			throws ExpiredJwtException, SignatureException, ArrayIndexOutOfBoundsException, Exception {
		try {
			Claims claims = Jwts.parser().setSigningKey(tokenSigningKey).parseClaimsJws(token.replace(tokenPreFix, ""))
					.getBody();

			return claims;
		} catch (ExpiredJwtException eje) {
			log.error("Token expirado: " + eje.getMessage());
			throw eje;
		} catch (SignatureException e) {
			log.error("Firma no confiable: " + e.getMessage());
			throw e;
		} catch (ArrayIndexOutOfBoundsException aie) {
			log.error("Firma malformada: " + aie.getMessage());
			throw aie;
		} catch (Exception e) {
			log.error("Error al procesar token: " + e.getMessage());
			throw e;
		}

	}

	/**
	 * 
	 * generateClaims
	 * 
	 * @param username
	 * @param authorities
	 * @return
	 */
	public Claims generateClaims(String username, Collection<? extends GrantedAuthority> authorities) {
		Claims claims = Jwts.claims().setSubject(username);
		claims.put("scopes", authorities.stream().map(s -> s.toString()).collect(Collectors.toList()));
		return claims;
	}

	/**
	 * 
	 * generateClaims
	 * 
	 * @param username
	 * @param authorities
	 * @return
	 */
	public Claims generateClaims(String username, List<String> authorities) {
		Claims claims = Jwts.claims().setSubject(username);
		claims.put("scopes", authorities);
		return claims;
	}

	/**
	 * 
	 * generateJWTToken
	 * 
	 * @param claims
	 * @param issuer
	 * @param subject
	 * @param jwtSettings
	 * @return
	 */
	public String generateJWTToken(Claims claims) {

		LocalDateTime currentTime = LocalDateTime.now();

		String JWTToken = Jwts.builder().setClaims(claims).setIssuer(jwtSettings.getTokenIssuer())
				.setSubject(claims.getSubject())
				.setExpiration(Date.from(currentTime.plusMinutes(jwtSettings.getTokenExpirationTime())
						.atZone(ZoneId.systemDefault()).toInstant()))
				.signWith(SignatureAlgorithm.HS512, jwtSettings.getTokenSigningKey()).compact();

		return JWTToken;
	}

	/**
	 * 
	 * generateJWTTokenResetPwd
	 * 
	 * @param claims
	 * @param issuer
	 * @param subject
	 * @param jwtSettings
	 * @return
	 */
	public String generateJWTTokenResetPwd(Claims claims, Date expiration) {

		String JWTToken = Jwts.builder().setClaims(claims).setIssuer(jwtSettingsRPwd.getTokenIssuer())
				.setSubject(claims.getSubject()).setExpiration(expiration)
				.signWith(SignatureAlgorithm.HS512, jwtSettingsRPwd.getTokenSigningKey()).compact();

		return JWTToken;
	}

	public JWTSettings getJwtSettings() {
		return jwtSettings;
	}

	public JWTSettingsResetPassword getJwtSettingsRPwd() {
		return jwtSettingsRPwd;
	}

}