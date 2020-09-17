package com.paypay.baymax.front.controller.security.login;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxn.sau.security.ErrorCodes;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.front.config.security.dto.ChangePasswordCredentials;
import com.paypay.baymax.front.config.security.jwt.TokenAuthenticationService;
import com.paypay.baymax.front.config.security.sau.AuthUtils;

@RestController
public class ChangePwdController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping(value = "/changePassword")
	@ResponseStatus(HttpStatus.OK)
	public void changePassword(Principal principal, HttpServletRequest request, HttpServletResponse response) {

		log.info("Usuario intentando cambiar password");

		String code = DefinicionesComunes.CODIGO_OK;
		String type = null;
		String message = null;
		String detail = null;
		boolean hasError = false;

		try {

			ChangePasswordCredentials cpc = (ChangePasswordCredentials) new ObjectMapper()
					.readValue(request.getInputStream(), ChangePasswordCredentials.class);

			if (cpc != null) {

				if (StringUtils.isNotBlank(cpc.getNewpassword()) && StringUtils.isNotBlank(cpc.getConfirmpassword())) {
					if (cpc.getNewpassword().compareTo(cpc.getConfirmpassword()) == 0) {
						UserDetails userDetails = userDetailsService.loadUserByUsername(cpc.getUsername());

						if (userDetails != null) {
							if (passwordEncoder.matches(cpc.getPassword(), userDetails.getPassword())) {
								TokenAuthenticationService
										.addAuthentication(request, response,
												new UsernamePasswordAuthenticationToken(cpc.getUsername(),
														cpc.getPassword(), userDetails.getAuthorities()),
												cpc, null, null);
							} else {
								hasError = true;
								code = ErrorCodes.DIFF_OLD_PASSWORD;
								type = "SAUException";
								message = "La contraseña actual es incorrecta";
								detail = null;
							}
						} else {
							hasError = true;
							code = AuthUtils.ERROR_USERNAME_NOT_FOUND;
							type = "SAUException";
							message = "Usuario incorrecto o inexistente";
							detail = null;
						}
					} else {
						hasError = true;
						code = ErrorCodes.SAME_NEW_NEW_PASSWORD;
						type = "SAUException";
						message = "La confirmación de contraseña no coincide";
						detail = null;
					}
				} else {
					hasError = true;
					code = ErrorCodes.SAME_NEW_NEW_PASSWORD;
					type = "SAUException";
					message = "La confirmación de contraseña no coincide";
					detail = null;
				}

			} else {
				hasError = true;
				code = AuthUtils.ERROR_USERNAME_NOT_FOUND;
				type = "SAUException";
				message = " Usuario incorrecto o inexistente";
				detail = null;
			}

		} catch (JsonParseException e) {
			hasError = true;
			code = "CHANGE_PASSWORD_ERROR";
			type = e.getClass().getSimpleName();
			message = e.getMessage();
			detail = e.getMessage();
		} catch (JsonMappingException e) {
			hasError = true;
			code = "CHANGE_PASSWORD_ERROR";
			type = e.getClass().getSimpleName();
			message = e.getMessage();
			detail = e.getMessage();

		} catch (IOException e) {
			hasError = true;
			code = "CHANGE_PASSWORD_ERROR";
			type = e.getClass().getSimpleName();
			message = e.getMessage();
			detail = e.getMessage();
		}

		if (hasError) {

			ObjectMapper mapper = new ObjectMapper();

			try {

				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);

				mapper.writeValue(response.getWriter(),
						new ResponseDTO(new MetaDTO(request.hashCode(), code, type, message, detail), null));

			} catch (IOException e) {
			}
		}
	}

}
