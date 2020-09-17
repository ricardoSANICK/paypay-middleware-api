package com.paypay.baymax.front.config.security.filters;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.front.config.security.dto.AccountCredentials;
import com.paypay.baymax.front.config.security.jwt.TokenAuthenticationService;
import com.paypay.baymax.front.config.security.sau.AuthUtils;
import com.paypay.baymax.front.config.security.sau.SAUUtils;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	static final String ORIGIN = "http://localhost:8080";
	private SessionAuthenticationStrategy sessionStrategy;
	private boolean continueChainBeforeSuccessfulAuthentication = false;
	private UserDetailsService userDetailsService;
	private SAUUtils sauUtils;

	public JWTLoginFilter(String url, AuthenticationManager authManager, SessionAuthenticationStrategy sessionStrategy,
			SAUUtils sauUtils, UserDetailsService userDetailsService) {

		super(new AntPathRequestMatcher(url));
		setAuthenticationManager(authManager);
		setSessionAuthenticationStrategy(sessionStrategy);

		this.sessionStrategy = sessionStrategy;
		this.userDetailsService = userDetailsService;
		this.sauUtils = sauUtils;

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String principal = null;

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (!requiresAuthentication(request, response)) {
			chain.doFilter(request, response);
			return;
		}

		Authentication authResult = null;

		AccountCredentials creds = null;

		try {

			creds = getCredentials(request);

			if (creds != null)
				principal = creds.getUsername();

			log.info("Usuario en login: " + principal);

			authResult = attemptAuthentication(request, response, creds);

			if (authResult == null)
				return;

			sessionStrategy.onAuthentication(authResult, request, response);

		} catch (InternalAuthenticationServiceException failed) {
			log.error("An internal error occurred while trying to authenticate the user.", failed);
			unsuccessfulAuthentication(request, response, failed, principal);
			return;
		} catch (AuthenticationException failed) {
			unsuccessfulAuthentication(request, response, failed, principal);
			return;
		} catch (Exception failed) {
			// unsuccessfulAuthentication(request, response, failed, principal);
			return;
		}

		// Authentication success
		if (continueChainBeforeSuccessfulAuthentication) {
			chain.doFilter(request, response);
		}

		successfulAuthentication(request, response, chain, authResult);
	}

	public AccountCredentials getCredentials(HttpServletRequest request)
			throws JsonParseException, JsonMappingException, IOException, Exception {
		return (AccountCredentials) new ObjectMapper().readValue(request.getInputStream(), AccountCredentials.class);
	}

	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response,
			AccountCredentials creds) throws IOException {

		return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(creds.getUsername(),
				creds.getPassword(), Collections.emptyList()));
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		TokenAuthenticationService.addAuthentication(request, response, auth, null, null, null);
	}

	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed, String principal) throws IOException, ServletException {

		log.info("El usuario " + (principal) + " no se pudo conectar: ");
		log.debug("Error: " + failed.getMessage());
		log.debug("Causa: " + failed.getCause());

		HashMap<String, Object> map = new HashMap<>();
		int subErrorCode = 0;

		SecurityContextHolder.clearContext();

		subErrorCode = sauUtils.unsuccessfulAuthentication(request, failed, principal);
		map.put("failedAttemps", sauUtils.getFailedAttemps(request, principal, subErrorCode));

		getRememberMeServices().loginFail(request, response);

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		UserDetails userDetails = null;

		try {
			userDetails = userDetailsService.loadUserByUsername(principal);
			log.debug("Usuario asignado a perfiles", userDetails.getUsername());
		} catch (UsernameNotFoundException unfe) {
			if (unfe.getMessage().contains("has no GrantedAuthority")) {
				subErrorCode = AuthUtils.USER_NOT_ASSIGNED;
			}
			unfe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String subCodeToLogMessage = AuthUtils.subCodeToLogMessage(subErrorCode);

		ResponseDTO responseDTO = new ResponseDTO(
				new MetaDTO(request.hashCode(), subCodeToLogMessage, "UnsuccessfulAuthentication",
						failed != null ? failed.getMessage() : "UnsuccessfulAuthentication", null),
				map);

		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getWriter(), responseDTO);

	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

}
