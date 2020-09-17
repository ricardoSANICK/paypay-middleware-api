package com.paypay.baymax.front.config.security.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paypay.baymax.commons.DTO.security.SAUDTO;
import com.paypay.baymax.commons.DTO.security.UsersFormDTO;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.util.CommonUtils;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.front.config.security.dto.ChangePasswordCredentials;
import com.paypay.baymax.front.config.security.sau.AuthUtils;
import com.paypay.baymax.front.config.security.sau.SAUUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

public class TokenAuthenticationService {

	public static JWTTokenUtils jwtTokensUtils;
	public static UserDetailsService userDetailsService;
	public static SAUUtils sauUtils;

	/**
	 *
	 * @param req
	 * @param res
	 * @param auth
	 * @param cpc
	 * @param token
	 * @param expiration
	 * @throws IOException
	 */
	public static void addAuthentication(HttpServletRequest req, HttpServletResponse res, Authentication auth,
			ChangePasswordCredentials cpc, String token, Date expiration) throws IOException {

		if (StringUtils.isBlank(auth.getName()))
			throw new IllegalArgumentException("No se peude crear un Token JWT sin username");

		if (auth.getAuthorities() == null || auth.getAuthorities().isEmpty())
			throw new IllegalArgumentException("El usuario no tiene privilegios");

		HashMap<String, Object> map = new HashMap<>();
		String code = null;
		ResponseEntity<ResponseDTO> rp = null;
		SAUDTO saudtoUser = null;
		UsersFormDTO usersFormDTO = new UsersFormDTO(auth.getName());
		Integer diasvencimiento = null;
		DateTime fechaVencimiento = null;
		boolean userNameNotFound = false;

		HashMap<String, Object> usersMaps = new HashMap<>(5);
		if (cpc != null) {
			rp = sauUtils.applyChangePassword(cpc, token, expiration, req);
		}
		if (rp == null) {
			HashMap<String, Object> respMap = sauUtils.applySessionPolicies(auth.getName(), req);

			code = respMap.get("code").toString();
			saudtoUser = (SAUDTO) respMap.get("user");
			diasvencimiento = Integer.parseInt(respMap.get("diasvencimiento").toString());
			fechaVencimiento = (DateTime) respMap.get("fechavencimiento");
		} else {
			try {
				code = rp.getBody().getMeta().getCode();
			} catch (Exception e) {
				code = "ERROR";
			}
		}
		if (code.compareTo(DefinicionesComunes.CODIGO_OK) == 0) {

			UserDetails userDetails = null;
			try {
				userDetails = userDetailsService.loadUserByUsername(auth.getName());

				SecurityContext sc = SecurityContextHolder.getContext();
				sc.setAuthentication(auth);

				HttpSession session = req.getSession(true);
				session.setMaxInactiveInterval(jwtTokensUtils.getJwtSettings().getMaxInactiveInterval());
				session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

				Claims claims = jwtTokensUtils.generateClaims(auth.getName(), userDetails.getAuthorities());

				String JWTToken = jwtTokensUtils.generateJWTToken(claims);

				res.setStatus(HttpStatus.OK.value());
				res.setContentType(MediaType.APPLICATION_JSON_VALUE);
				res.addHeader(jwtTokensUtils.getJwtSettings().getHeaderString(),
						jwtTokensUtils.getJwtSettings().getTokenPreFix() + " " + JWTToken);

				usersMaps.put("username", saudtoUser.getUsername());
				usersMaps.put("fullName", CommonUtils.getFullName(saudtoUser.getLastName(), saudtoUser.getFirstName()));
				usersMaps.put("lastName", saudtoUser.getLastName());
				usersMaps.put("firstName", saudtoUser.getFirstName());
				usersMaps.put("cellphone", saudtoUser.getCellphone());
				usersMaps.put("telephone", saudtoUser.getTelephone());
				usersMaps.put("email", saudtoUser.getEmail());
				usersMaps.put("groups", saudtoUser.getGroups());
				usersMaps.put("avatar", saudtoUser.getAvatar());
				usersMaps.put("lastpasswordchange",
						saudtoUser.getLastPasswordChange() != null ? saudtoUser.getLastPasswordChange()
								: new DateTime(1900, 1, 1, 1, 1).toDate());
				usersMaps.put("diasvencimiento", diasvencimiento);
				usersMaps.put("fechaVencimiento", fechaVencimiento != null ? fechaVencimiento.toDate() : null);

				map.put("token", JWTToken);
				map.put("roles", (ArrayList<String>) userDetails.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
				map.put("users", usersMaps);
			} catch (UsernameNotFoundException unfe) {
				if (unfe.getMessage().contains("has no GrantedAuthority")) {

					userNameNotFound = true;

					ObjectMapper mapper = new ObjectMapper();

					res.setStatus(HttpStatus.UNAUTHORIZED.value());
					res.setContentType(MediaType.APPLICATION_JSON_VALUE);

					mapper.writeValue(res.getWriter(), new ResponseDTO(new MetaDTO(req.hashCode(),
							AuthUtils.ERROR_USER_NOT_ASSIGNED, AuthUtils.ERROR_USER_NOT_ASSIGNED), null));

				}

			}

		} else {
			try {
				usersMaps.put("username", usersFormDTO.getUsername());
				usersMaps.put("fullName", CommonUtils.getFullName(saudtoUser.getLastName(), saudtoUser.getFirstName()));
				usersMaps.put("lastName", saudtoUser.getLastName());
				usersMaps.put("firstName", saudtoUser.getFirstName());
				usersMaps.put("nombre", usersFormDTO.getFirstName());
				map.put("users", usersMaps);
			} catch (Exception e) {

			}

			SecurityContextHolder.clearContext();
			HttpSession session = req.getSession(false);
			if (session != null) {
				session.invalidate();
			}

			res.setStatus(HttpStatus.UNAUTHORIZED.value());
			res.setContentType(MediaType.APPLICATION_JSON_VALUE);

		}

		if (userNameNotFound == false) {
			ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(req.hashCode(), code), map);

			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(res.getWriter(), rp == null ? responseDTO : rp.getBody());
		}

	}

	public static Authentication getAuthentication(HttpServletRequest request, JWTSettings jwtSettings,
			JWTSettingsResetPassword jwtSettingsResetPassword) {

		String token = request.getHeader(jwtSettings.getHeaderString());

		if (token != null) {

			Claims claims = null;
			try {

				if (token.contains(jwtSettingsResetPassword.getTokenPreFix())) {
					claims = jwtTokensUtils.validateTokenClaims(jwtSettingsResetPassword.getTokenSigningKey(),
							jwtSettingsResetPassword.getTokenPreFix(), token);
				} else {
					claims = jwtTokensUtils.validateTokenClaims(jwtSettings.getTokenSigningKey(), jwtSettings.getTokenPreFix(), token);
				}

			} catch (ExpiredJwtException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (claims != null) {
				String user = claims.getSubject();

				@SuppressWarnings("unchecked")
				List<String> authoritiesList = (List<String>) claims.get("scopes");

				if (StringUtils.isNotBlank(user) && CollectionUtils.isNotEmpty(authoritiesList)) {

					Collection<? extends GrantedAuthority> authorities = authoritiesList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

					SecurityContext sc = SecurityContextHolder.getContext();
					if (sc != null) {
						Authentication auth = sc.getAuthentication();
						if (auth != null) {
							if (auth.isAuthenticated()) {
								return auth;
							}
						}
					}

					return StringUtils.isNotBlank(user) && CollectionUtils.isNotEmpty(authorities)
							? new UsernamePasswordAuthenticationToken(user, null, authorities)
							: null;
				}
			}
		}
		return null;
	}

}
