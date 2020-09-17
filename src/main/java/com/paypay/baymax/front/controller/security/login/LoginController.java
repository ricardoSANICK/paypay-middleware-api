package com.paypay.baymax.front.controller.security.login;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.front.config.security.dto.AccountCredentials;
import com.paypay.baymax.front.config.security.jwt.JWTSettings;
import com.paypay.baymax.front.config.security.jwt.JWTSettingsResetPassword;
import com.paypay.baymax.front.config.security.jwt.JWTTokenUtils;
import com.paypay.baymax.front.config.security.sau.AuthUtils;
import com.paypay.baymax.front.controller.VueEndPoints;
import com.paypay.baymax.front.service.CommonMethodsService;
import com.paypay.baymax.front.service.ResetPasswordService;
import com.paypay.baymax.front.service.security.SAUService;

@RequestMapping("/")
@Controller
public class LoginController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JWTSettings jwtSettings;

	@Autowired
	private JWTSettingsResetPassword jwtSettingsResetPWD;

	@Autowired
	private SAUService sauService;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private CommonMethodsService<Object, Object> service;

	@Value("${com.paypay.baymax.web.front.urlResetPwd}")
	private String urlResetPwd;

	@GetMapping(value = { 
			"", 
			"/", 
			"/login", 
			"/error", 
			VueEndPoints.HOME, 
			VueEndPoints.AUTH, 
			VueEndPoints.RESET_PWD,
			VueEndPoints.FORGET_PWD, 
			VueEndPoints.SESSION_EXPIRED_MSG, 
			VueEndPoints.EXPIRED_PWD })
	public String index() {
		return "index.html";
	}

	@GetMapping(value = "/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		log.info("La sesión del usuario ha vencido, entro a endpoint");
		response.setStatus(HttpStatus.ACCEPTED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(response.getWriter(), new ResponseDTO(
					new MetaDTO(request.hashCode(), DefinicionesComunes.CODIGO_LOGOUT, "Logout correcto"), null));
		} catch (IOException e) {
			log.error("Error al invocar logout");
			e.printStackTrace();
		}
		return "index.html";
	}

	@GetMapping(value = VueEndPoints.HOME + "/{path}")
	public String home() {
		return "../index.html";
	}

	@GetMapping(value = VueEndPoints.AUTH_RESET_PWD)
	public String authResetPwd() {
		return "../index.html";
	}

	@PostMapping(value = EndPoints.REQUEST_RESET_PASSWORD)
	@ResponseStatus(HttpStatus.OK)
	public void requestResetPassword(Principal principal, HttpServletRequest request, HttpServletResponse response) {

		log.info("Usuario solicita cambiar password");

		JWTTokenUtils jwtTokenUtils = new JWTTokenUtils(jwtSettings, jwtSettingsResetPWD);

		try {
			log.info("Obteniendo datos para reset password");
			/**
			 * Obtiene datos para reset password
			 */
			AccountCredentials acc = (AccountCredentials) new ObjectMapper().readValue(request.getInputStream(),
					AccountCredentials.class);

			if (acc != null) {
				if (StringUtils.isNotBlank(acc.getUsername())) {
					/**
					 * Carga los detalles del usuario para la autenticación
					 */

					log.info("Username: " + acc.getUsername());
					UserDetails userDetails = null;

					try {
						userDetails = userDetailsService.loadUserByUsername(acc.getUsername());
					} catch (UsernameNotFoundException unfe) {
						ObjectMapper mapper = new ObjectMapper();
						if (unfe.getMessage().contains("has no GrantedAuthority")) {
							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);

							mapper.writeValue(response.getWriter(), new ResponseDTO(new MetaDTO(request.hashCode(),
									AuthUtils.ERROR_USER_NOT_ASSIGNED, AuthUtils.ERROR_USER_NOT_ASSIGNED), null));

						} else if (unfe.getMessage().contains("Username " + acc.getUsername() + " not found")) {
							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);

							mapper.writeValue(response.getWriter(), new ResponseDTO(new MetaDTO(request.hashCode(),
									AuthUtils.ERROR_USERNAME_NOT_FOUND, AuthUtils.ERROR_USERNAME_NOT_FOUND), null));
						}

					}

					if (userDetails != null) {
						log.info("Generando autenticación");
						/**
						 * 1. Generar autenticación
						 */
						Authentication auth = new UsernamePasswordAuthenticationToken(acc.getUsername(), null,
								userDetails.getAuthorities());

						SecurityContext sc = SecurityContextHolder.getContext();
						sc.setAuthentication(auth);

						HttpSession session = request.getSession(true);
						session.setMaxInactiveInterval(120);
						session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

						/**
						 * 2. Enviar correo electronico
						 */
						ResetPasswordService resetPasswordService = new ResetPasswordService(service);
						ResponseEntity<ResponseDTO> responseEntity = resetPasswordService.sendResetPassword(
								userDetails.getUsername(), urlResetPwd, jwtTokenUtils, sauService, request, null);

						/**
						 * Se limpia el contexto de autenticación, se invalida la sesión
						 * 
						 */
						SecurityContextHolder.clearContext();
						if (session != null) {
							session.invalidate();
						}

						/**
						 * CODIGO TEMPORAL
						 ***********************************************************/

						// response.sendRedirect("/resetpwd");

						ObjectMapper mapper = new ObjectMapper();

						if (responseEntity.getBody().getMeta().getCode().equals(AuthUtils.RESET_REJECTED)) {
							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							mapper.writeValue(response.getWriter(), responseEntity.getBody());
						} else {
							response.setStatus(HttpStatus.OK.value());
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							mapper.writeValue(response.getWriter(), responseEntity);
						}

						/***********************************************************
						 * CODIGO TEMPORAL
						 */

					}

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
