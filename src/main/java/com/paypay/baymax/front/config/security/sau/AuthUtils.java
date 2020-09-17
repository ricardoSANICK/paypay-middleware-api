package com.paypay.baymax.front.config.security.sau;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthUtils {

	public static final int USERNAME_NOT_FOUND = 0x525;
	public static final int INVALID_PASSWORD = 0x52e;
	public static final int NOT_PERMITTED = 0x530;
	public static final int PASSWORD_EXPIRED = 0x532;
	public static final int ACCOUNT_DISABLED = 0x533;
	public static final int ACCOUNT_EXPIRED = 0x701;
	public static final int PASSWORD_NEEDS_RESET = 0x773;
	public static final int ACCOUNT_LOCKED = 0x775;
	public static final int ACCOUNT_IS_USING = 902;
	public static final int CONNECTION_REFUSED = 903;
	public static final int USER_NOT_ASSIGNED = 999;
	public static final int INVALID_PASSWORD_CAPTCHA = 1919;
	public static final int INVALID_PASSWORD_CAPTCHAx = 0x52c;

	/** Imprime: Usuario incorrecto o inexistente **/
	public static final String ERROR_USERNAME_NOT_FOUND = "SC0001";
	/** Imprime: Contraseña incorrecta **/
	public static final String ERROR_INVALID_PASSWORD = "SC0002";
	/** Imprime: Usuario bloqueado, contacte al Administrador **/
	public static final String ERROR_ACCOUNT_LOCKED = "SC0003";
	/** Imprime: Usuario no asignado a un grupo, contacte al Administrador **/
	public static final String ERROR_USER_NOT_ASSIGNED = "SC0004";
	/** Imprime: Error de conexión **/
	public static final String ERROR_CONNECTION_REFUSED = "SC0005";
	/** Imprime: Usuario no permitido **/
	public static final String ERROR_NOT_PERMITTED = "SC0006";
	/** Imprime: Password expirada, contacte al Administrador **/
	public static final String ERROR_PASSWORD_EXPIRED = "SC0007";
	/** Imprime: Usuario deshabilitado **/
	public static final String ERROR_ACCOUNT_DISABLED = "SC0008";
	/** Imprime: Cuenta expirada, contacte al Administrador **/
	public static final String ERROR_ACCOUNT_EXPIRED = "SC0009";
	/** Redirige a pantalla de renovación de password **/
	public static final String ERROR_PASSWORD_NEEDS_RESET = "SC0010";
	/** Imprime: Ya existe una sesión abierta con este usuario **/
	public static final String ERROR_ACCOUNT_IS_USING = "SC0011";
	/** Habilita captcha **/
	public static final String ERROR_INVALID_PASSWORD_CAPTCHA = "SC0012";
	/** Imprime: Su usuario está bloqueado, consulte al administrador **/
	public static final String RESET_REJECTED = "SC0013";

	private static final Pattern SUB_ERROR_CODE = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");

	public static String subCodeToLogMessage(int code) {

		switch (code) {
		case 0:
			return ERROR_INVALID_PASSWORD;
		case USERNAME_NOT_FOUND:
			return ERROR_USERNAME_NOT_FOUND;
		case INVALID_PASSWORD:
			return ERROR_INVALID_PASSWORD;
		case NOT_PERMITTED:
			return ERROR_NOT_PERMITTED;
		case PASSWORD_EXPIRED:
			return ERROR_PASSWORD_EXPIRED;
		case ACCOUNT_DISABLED:
			return ERROR_ACCOUNT_DISABLED;
		case ACCOUNT_EXPIRED:
			return ERROR_ACCOUNT_EXPIRED;
		case PASSWORD_NEEDS_RESET:
			return ERROR_PASSWORD_NEEDS_RESET;
		case ACCOUNT_LOCKED:
			return ERROR_ACCOUNT_LOCKED;
		case ACCOUNT_IS_USING:
			return ERROR_ACCOUNT_IS_USING;
		case CONNECTION_REFUSED:
			return ERROR_CONNECTION_REFUSED;
		case INVALID_PASSWORD_CAPTCHA:
		case INVALID_PASSWORD_CAPTCHAx:
			return ERROR_INVALID_PASSWORD_CAPTCHA;
		case USER_NOT_ASSIGNED:
			return ERROR_USER_NOT_ASSIGNED;
		}

		return "Unknown (error code " + Integer.toHexString(code) + ")";
	}

	/**
	 * parseSubErrorCode
	 * 
	 * @author rflores
	 * 
	 * @param message
	 * @return
	 */
	public static int parseSubErrorCode(String message) {
		if (message != null && !message.isEmpty()) {
			Matcher m = SUB_ERROR_CODE.matcher(message);

			if (message.contains("CommunicationException")) {
				return AuthUtils.CONNECTION_REFUSED;
			}

			if (m.matches()) {
				return Integer.parseInt(m.group(1), 16);
			} else if (message.contains("ERR_229")) {
				return AuthUtils.INVALID_PASSWORD;
			} else {
				return AuthUtils.USERNAME_NOT_FOUND;
			}
		} else {
			return AuthUtils.INVALID_PASSWORD;
		}
	}

}
