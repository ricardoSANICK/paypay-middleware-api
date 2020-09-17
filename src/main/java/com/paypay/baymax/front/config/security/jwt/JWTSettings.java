package com.paypay.baymax.front.config.security.jwt;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paypay.security.jwt")
public class JWTSettings {

	private Integer tokenExpirationTime;
	private String tokenIssuer;
	private String tokenSigningKey;
	private String tokenPreFix;
	private String headerString;
	private Integer maxInactiveInterval;
	private Integer maxSessionByUser;

	public Integer getTokenExpirationTime() {
		return tokenExpirationTime != null ? tokenExpirationTime : getMaxInactiveInterval();
	}

	public void setTokenExpirationTime(Integer tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}

	public String getTokenIssuer() {
		return StringUtils.isNotBlank(tokenIssuer) ? tokenIssuer : "Issuer";
	}

	public void setTokenIssuer(String tokenIssuer) {
		this.tokenIssuer = tokenIssuer;
	}

	public String getTokenSigningKey() {
		return StringUtils.isNotBlank(tokenSigningKey) ? tokenSigningKey : "FTOKSEF0$71D3$ALUD";
	}

	public void setTokenSigningKey(String tokenSigningKey) {
		this.tokenSigningKey = tokenSigningKey;
	}

	public String getTokenPreFix() {
		return StringUtils.isNotBlank(tokenPreFix) ? tokenPreFix : "Bearer";
	}

	public void setTokenPreFix(String tokenPreFix) {
		this.tokenPreFix = tokenPreFix;
	}

	public String getHeaderString() {
		return StringUtils.isNotBlank(headerString) ? headerString : "Authorization";
	}

	public void setHeaderString(String headerString) {
		this.headerString = headerString;
	}

	public Integer getMaxInactiveInterval() {
		return maxInactiveInterval != null ? maxInactiveInterval * 60 : 1800;
	}

	public void setMaxInactiveInterval(Integer maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public Integer getMaxSessionByUser() {
		return maxSessionByUser != null ? maxSessionByUser : 1;
	}

	public void setMaxSessionByUser(Integer maxSessionByUser) {
		this.maxSessionByUser = maxSessionByUser;
	}

}