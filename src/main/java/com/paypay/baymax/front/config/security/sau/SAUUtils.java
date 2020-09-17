package com.paypay.baymax.front.config.security.sau;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nxn.sau.security.DTO.SecurityDTO;
import com.paypay.baymax.commons.DTO.security.SAUDTO;
import com.paypay.baymax.commons.DTO.security.UsersDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.util.CommonUtils;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.front.config.security.dto.ChangePasswordCredentials;
import com.paypay.baymax.front.service.security.SAUService;
import com.nxn.sau.security.ErrorCodes;
import com.nxn.sau.security.SecurityPolicies;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ProcessingException;
import java.util.Date;
import java.util.HashMap;

public class SAUUtils {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final SAUService sauService;
	private final Gson gson;

	/**
	 * 
	 * @param sauService
	 */
	public SAUUtils(SAUService sauService, Gson gson) {
		super();
		this.sauService = sauService;
		this.gson = gson;
	}

	public int unsuccessfulAuthentication(HttpServletRequest request, AuthenticationException failed,
			String principal) {

		int subErrorCode = 0;
		String errormsj = "";

		if (failed instanceof SessionAuthenticationException) {
			subErrorCode = AuthUtils.ACCOUNT_IS_USING;
		} else if (failed instanceof BadCredentialsException) {
			subErrorCode = AuthUtils.INVALID_PASSWORD;
			subErrorCode = this.updateUserAttemps(principal, subErrorCode, request);
		} else if (failed instanceof DisabledException) {
			subErrorCode = AuthUtils.ACCOUNT_LOCKED;
		} else {

			subErrorCode = this.updateUserAttemps(principal, subErrorCode, request);

			if (errormsj.compareTo(AuthUtils.ERROR_ACCOUNT_LOCKED) != 0) {

				try {
					subErrorCode = AuthUtils.parseSubErrorCode(failed.getCause().getMessage());
				} catch (Exception e) {
					log.info("Error de usuario y/o contraseña");
					subErrorCode = AuthUtils.INVALID_PASSWORD;
				}
				if (subErrorCode <= 0) {
					log.debug("Failed to locate AD-specific sub-error code in message");
					log.info("Error de usuario y/o contraseña");
					subErrorCode = AuthUtils.INVALID_PASSWORD;
				}

			} else {
				subErrorCode = AuthUtils.ACCOUNT_LOCKED;
			}
		}

		log.info(AuthUtils.subCodeToLogMessage(subErrorCode));

		return subErrorCode;
	}

	/**
	 * 
	 * @param principal
	 */
	public int updateUserAttemps(String principal, int suberrorcode, HttpServletRequest request) {

		try {
			SecurityPolicies sp = new SecurityPolicies();
			ResponseDTO responseDTO = null;

			if (StringUtils.isNotBlank(principal)) {
				responseDTO = sauService.securityPoliciesUser("userdetails/" + principal + "/" + principal, request,
						null, principal, UsersDTO.class);

				HashMap<String, Object> result = gson.fromJson(gson.toJson(responseDTO.getResults()),
						new TypeToken<HashMap<String, Object>>() {
						}.getType());
				SAUDTO user = gson.fromJson(gson.toJson(result.get("userDetails")), new TypeToken<SAUDTO>() {
				}.getType());

				result.clear();

				if (user != null) {

					if (user.getLockDate() != null) {
						// errormsj = AuthUtils.ERROR_ACCOUNT_LOCKED;
						suberrorcode = AuthUtils.ACCOUNT_LOCKED;
					} else if (user.getLockDate() != null && user.getEnabled() == false) {
						// errormsj = AuthUtils.ERROR_ACCOUNT_LOCKED;
						suberrorcode = AuthUtils.ACCOUNT_LOCKED;
					} else if (user.getGroups() == null) {
						// errormsj = AuthUtils.ERROR_USER_NOT_ASSIGNED;
						suberrorcode = AuthUtils.USER_NOT_ASSIGNED;
					} else if (user.getGroups().isEmpty()) {
						// errormsj =AuthUtils.ERROR_USER_NOT_ASSIGNED;
						suberrorcode = AuthUtils.USER_NOT_ASSIGNED;
					} else {
						sauService.securityPoliciesUser("lastfailedattempt/" + principal + "/" + principal, request,
								"Último Intento Fallido", principal, SecurityDTO.class);

						SecurityDTO securityDTO = new SecurityDTO();
						securityDTO.setNumintentosfallidos(user.getFailedAttemps() + 1);

						if (sp.validateUserPolicies(securityDTO) == ErrorCodes.ACCOUNT_LOCKED) {

							sauService.securityPoliciesUser("bloquearusuario/" + principal + "/" + principal, request,
									"Bloqueo de usuario", principal, SecurityDTO.class);

							// errormsj = AuthUtils.ERROR_ACCOUNT_LOCKED;
							suberrorcode = AuthUtils.ACCOUNT_LOCKED;
						} else if (sp.validateUserPolicies(securityDTO, 1) == ErrorCodes.INVALID_PASSWORD_CAPTCHA) {
							// errormsj = AuthUtils.ERROR_INVALID_PASSWORD_CAPTCHA;
							suberrorcode = AuthUtils.INVALID_PASSWORD_CAPTCHA;
						} else {
							// errormsj = AuthUtils.ERROR_INVALID_PASSWORD;
							suberrorcode = AuthUtils.INVALID_PASSWORD;
						}
					}
				} else {
					// errormsj = AuthUtils.ERROR_USERNAME_NOT_FOUND;
					suberrorcode = AuthUtils.USERNAME_NOT_FOUND;
				}

			} else {
				// errormsj = AuthUtils.ERROR_USERNAME_NOT_FOUND;
				suberrorcode = AuthUtils.USERNAME_NOT_FOUND;
			}

		} catch (JsonSyntaxException e1) {
			// errormsj = AuthUtils.ERROR_CONNECTION_REFUSED;
			suberrorcode = AuthUtils.CONNECTION_REFUSED;
		} catch (ProcessingException e1) {
			// errormsj = AuthUtils.ERROR_CONNECTION_REFUSED;
			suberrorcode = AuthUtils.CONNECTION_REFUSED;
		} catch (Exception e1) {
			// errormsj = AuthUtils.ERROR_CONNECTION_REFUSED;
			suberrorcode = AuthUtils.CONNECTION_REFUSED;
		}

		log.error("ErrorMSJ: " + suberrorcode);

		return suberrorcode;
	}

	/**
	 * 
	 * @param principal
	 * @param request
	 * @return
	 */
	public HashMap<String, Object> applySessionPolicies(String principal, HttpServletRequest request) {

		try {
			HashMap<String, Object> respMap = new HashMap<String, Object>();
			ResponseDTO responseDTO = null;

			String code = DefinicionesComunes.CODIGO_ERROR;
			String fullName = "Usuario";

			SecurityPolicies sp = new SecurityPolicies();

			SecurityDTO securityDTO = new SecurityDTO();

			responseDTO = sauService.securityPoliciesUser("userdetails/" + principal + "/" + principal, request, null,
					principal, UsersDTO.class);

			HashMap<String, ?> result = gson.fromJson(gson.toJson(responseDTO.getResults()),
					new TypeToken<HashMap<String, Object>>() {
					}.getType());

			SAUDTO user = gson.fromJson(gson.toJson(result.get("userDetails")), new TypeToken<SAUDTO>() {
			}.getType());
			fullName = CommonUtils.getFullName(user.getLastName(), user.getFirstName());

			DateTime dateLastAccessDT = DateTime.now();
			if (user.getLastAccess() != null)
				dateLastAccessDT = new DateTime(user.getLastAccess());

			DateTime dateLastPasswordChangeDT = new DateTime(1900, 1, 1, 1, 1);
			if (user.getLastPasswordChange() != null)
				dateLastPasswordChangeDT = new DateTime(user.getLastPasswordChange());

			DateTime expirationDate = dateLastPasswordChangeDT.plusDays(sp.getProp_dias_vencimiento_password_interno());

			int lastAccess = Days.daysBetween(DateTime.now(), dateLastAccessDT).getDays();
			int lastPasswordChange = Math.abs(Days.daysBetween(expirationDate, DateTime.now()).getDays());

			securityDTO.setNumintentosfallidos(user.getFailedAttemps());
			securityDTO.setTipoUsuario("interno");
			securityDTO.setUltimo_acceso_interno(lastAccess);
			securityDTO.setUltimo_cambio_password_interno(lastPasswordChange);
			securityDTO.setTipoUsuario("interno");

			int errorcode = sp.validateUserPolicies(securityDTO);

			result.clear();

			switch (errorcode) {
			case ErrorCodes.USER_OK:
				sauService.securityPoliciesUser("lastaccess/" + principal + "/" + principal, request,
						"Acceso de usuario: " + principal, principal, UsersDTO.class);
				code = DefinicionesComunes.CODIGO_OK;
				break;
			case ErrorCodes.ACCOUNT_LOCKED:
				log.info("CUENTA BLOQUEADA");

				SecurityContextHolder.clearContext();
				HttpSession session = request.getSession(false);
				if (session != null) {
					session.invalidate();
				}
				code = AuthUtils.ERROR_ACCOUNT_LOCKED;
				break;
			case ErrorCodes.ACCOUNT_EXPIRED:
				sauService.securityPoliciesUser("bloquearusuario/" + principal + "/" + principal, request,
						"Bloqueando usuario [" + principal + "] por expiración de cuenta", principal, UsersDTO.class);
				code = AuthUtils.ERROR_ACCOUNT_EXPIRED;
				break;
			case ErrorCodes.PASSWORD_EXPIRED:
				log.info("PASSWORD EXPIRADO");
				code = AuthUtils.ERROR_PASSWORD_EXPIRED;
				break;
			}

			respMap.put("code", code);
			respMap.put("user", user);
			respMap.put("nombre", fullName);
			respMap.put("diasvencimiento", securityDTO.getUltimo_cambio_password_interno());
			respMap.put("fechavencimiento", expirationDate);

			return respMap;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 * @param cpc
	 * @param request
	 * @return
	 */
	public ResponseEntity<ResponseDTO> applyChangePassword(ChangePasswordCredentials cpc, String tokenAnterior,
			Date tokenVencimiento, HttpServletRequest request) {

		SecurityDTO securityDTO = new SecurityDTO();

		securityDTO.setUsername(cpc.getUsername());
		securityDTO.setPassword(cpc.getPassword());
		securityDTO.setNewpassword(cpc.getNewpassword());
		securityDTO.setConfirmpassword(cpc.getConfirmpassword());
		securityDTO.setTokenAnterior(tokenAnterior);
		securityDTO.setTokenVencimiento(tokenVencimiento);

		ResponseEntity<ResponseDTO> rp = (cpc.getPassword() != null)
				? sauService.changePassword(securityDTO, request, null, cpc.getUsername(), securityDTO.getClass())
				: sauService.resetPassword(securityDTO, request, null, cpc.getUsername(), securityDTO.getClass());

		if (rp != null) {

			if (rp.getStatusCode().compareTo(HttpStatus.OK) == 0) {
				if (rp.getBody() != null) {
					if (rp.getBody().getMeta() != null) {
						if (StringUtils.isNotBlank(rp.getBody().getMeta().getCode())) {
							if (rp.getBody().getMeta().getCode().compareTo(DefinicionesComunes.CODIGO_OK) == 0) {
								return null;
							}
						}
					}
				}
			}
		}

		return rp;

	}

	/**
	 * 
	 * @param request
	 * @param principal
	 * @return
	 * @throws Exception
	 */
	public int getFailedAttemps(HttpServletRequest request, String principal, int subErrorCode) {
		ResponseDTO responseDTO = null;
		try {
			if (subErrorCode == AuthUtils.INVALID_PASSWORD || subErrorCode == AuthUtils.INVALID_PASSWORD_CAPTCHA
					|| subErrorCode == AuthUtils.INVALID_PASSWORD_CAPTCHAx) {
				responseDTO = sauService.securityPoliciesUser("userdetails/" + principal + "/" + principal, request,
						null, principal, UsersDTO.class);

				HashMap<String, Object> result = gson.fromJson(gson.toJson(responseDTO.getResults()),
						new TypeToken<HashMap<String, Object>>() {
						}.getType());

				SAUDTO user = gson.fromJson(gson.toJson(result.get("userDetails")), new TypeToken<SAUDTO>() {
				}.getType());

				return user.getFailedAttemps() != null ? user.getFailedAttemps() : 0;
			}

			return 0;
		} catch (Exception e) {
			return 0;
		}
	}

	public SAUService getSauService() {
		return sauService;
	}

	public Gson getGson() {
		return gson;
	}

}
