package com.paypay.baymax.front.controller.security.login;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxn.sau.security.ErrorCodes;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.front.config.security.dto.ChangePasswordCredentials;
import com.paypay.baymax.front.config.security.jwt.JWTSettings;
import com.paypay.baymax.front.config.security.jwt.JWTSettingsResetPassword;
import com.paypay.baymax.front.config.security.jwt.JWTTokenUtils;
import com.paypay.baymax.front.config.security.jwt.TokenAuthenticationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

@RestController
public class ResetPwdController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final JWTSettings jwtSettings;

	private final JWTSettingsResetPassword jwtSettingsResetPWD;

	private final UserDetailsService userDetailsService;

	private final SessionRegistry sessionRegistry;

	@Value("${com.paypay.baymax.web.front.urlResetPwd}")
	private String urlResetPwd;

	@Autowired
	public ResetPwdController(JWTSettings jwtSettings, JWTSettingsResetPassword jwtSettingsResetPWD,
			UserDetailsService userDetailsService, SessionRegistry sessionRegistry) {
		this.jwtSettings = jwtSettings;
		this.jwtSettingsResetPWD = jwtSettingsResetPWD;
		this.userDetailsService = userDetailsService;
		this.sessionRegistry = sessionRegistry;
	}

	@GetMapping(value = EndPoints.RESET_PASSWORD)
	@ResponseStatus(HttpStatus.OK)
	public void resetPassword(@RequestParam("token") String token, HttpServletRequest request,
			HttpServletResponse response) {
		log.info("TOKEN:" + token);

		HashMap<String, Object> map = new HashMap<>();
		map.put("token", token);

		response.setStatus(HttpStatus.OK.value());

		response.addHeader(jwtSettingsResetPWD.getHeaderString(), jwtSettingsResetPWD.getTokenPreFix() + " " + token);

		try {
			response.sendRedirect("/auth/resetpwd?token=" + token);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@GetMapping(value = "/resetSessions/{username:.+}")
	@PreAuthorize("hasRole('ROLE_ACCESO_SEGURIDAD')")
	@ResponseStatus(HttpStatus.OK)
	public void resetSession(@PathVariable("username") String username, HttpServletRequest request,
			HttpServletResponse response) {

		if (username != null) {
			this.killSessions(username);
		}
	}

	@PostMapping(value = "/resetPassword")
	@ResponseStatus(HttpStatus.OK)
	public void resetPassword(Principal principal, HttpServletRequest request, HttpServletResponse response) {

		log.info("Usuario reseta password");

		String code = DefinicionesComunes.CODIGO_OK;
		String type = null;
		String message = null;
		String detail = null;
		boolean hasError = false;

		try {
			/**
			 * 1. Obtiene usuario de request
			 */

			ChangePasswordCredentials cpc = (ChangePasswordCredentials) new ObjectMapper()
					.readValue(request.getInputStream(), ChangePasswordCredentials.class);

			if (cpc != null) {
				if (StringUtils.isNotBlank(cpc.getNewpassword()) && StringUtils.isNotBlank(cpc.getConfirmpassword())) {
					if (cpc.getNewpassword().compareTo(cpc.getConfirmpassword()) == 0) {
						try {
							/**
							 * 2. Extrae Token de header
							 */

							JWTTokenUtils jwtTokensUtils = new JWTTokenUtils(jwtSettings, jwtSettingsResetPWD);

							String token = request.getHeader(jwtSettingsResetPWD.getHeaderString());

							/**
							 * 3. Obtiene objeto de claims
							 */
							Claims claims = jwtTokensUtils.validateTokenClaims(jwtSettingsResetPWD.getTokenSigningKey(),
									jwtSettingsResetPWD.getTokenPreFix(), token);

							log.info("subject: " + claims.getSubject());

							/**
							 * 4. Carga los detalles del usuario para la autenticación
							 */
							UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

							if (userDetails != null) {
								cpc.setUsername(userDetails.getUsername());
								cpc.setPassword(null);

								/**
								 * 7. Elimina las sesiones actuales del usuario, si es que las hay
								 */
								this.killSessions(userDetails.getUsername());

								/**
								 * 6. Autentica y cambia contraseña (se reusa metodo de changePassword)
								 */

								TokenAuthenticationService.addAuthentication(
										request, response, new UsernamePasswordAuthenticationToken(cpc.getUsername(),
												null, userDetails.getAuthorities()),
										cpc, token, claims.getExpiration());

							} else {

							}

						} catch (ExpiredJwtException eje) {
							code = ErrorCodes.EXPIRED_JWT;
							type = eje.getClass().getSimpleName();
							message = eje.getMessage();
							hasError = true;
							log.error("Token expirado: " + eje.getMessage());
						} catch (SignatureException se) {
							code = ErrorCodes.UNTRUSTED_JWT;
							type = se.getClass().getSimpleName();
							message = se.getMessage();
							hasError = true;
							log.error("Error de firma de JWT: " + se.getMessage());
						} catch (ArrayIndexOutOfBoundsException aie) {
							code = ErrorCodes.UNTRUSTED_JWT;
							type = aie.getClass().getSimpleName();
							message = aie.getMessage();
							hasError = true;
							log.error("Firma malformada: " + aie.getMessage());
						} catch (Exception e) {
							code = ErrorCodes.UNTRUSTED_JWT;
							type = e.getClass().getSimpleName();
							message = e.getMessage();
							hasError = true;
							log.error("Error al procesar token: " + e.getMessage());
						}
					}
				}
			}

		} catch (IOException e) {
			code = DefinicionesComunes.CODIGO_ERROR;
			type = e.getClass().getSimpleName();
			message = e.getMessage();
			hasError = true;
			// log.error("Error al procesar solicitud: " + e.getMessage());
		}

		if (hasError) {
			ObjectMapper mapper = new ObjectMapper();

			response.setStatus(HttpStatus.OK.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			try {
				mapper.writeValue(response.getWriter(),
						new ResponseDTO(new MetaDTO(request.hashCode(), code, type, message, detail), null));
			} catch (IOException e) {
				// log.error("Error al procesar solicitud: " + e.getMessage());
				// e.printStackTrace();
			}
		}

	}

	private void killSessions(String username) {
		for (Object principals : sessionRegistry.getAllPrincipals()) {
			User principal = (User) principals;

			if (principal.getUsername().equals(username)) {
				List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
				if (sessions != null) {
					if (!sessions.isEmpty()) {
						for (SessionInformation sif : sessions) {
							sif.expireNow();
							log.info("Sesión expirada para: " + sif.getPrincipal());
						}
					}
				}
			}
		}
	}
}
