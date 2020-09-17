package com.paypay.baymax.front.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxn.sau.security.DTO.SecurityDTO;
import com.paypay.baymax.commons.DTO.mail.CorreoDTO;
import com.paypay.baymax.commons.DTO.mail.PlantillaCorreoDTO;
import com.paypay.baymax.commons.DTO.security.UsersDTO;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.util.CommonUtils;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.commons.util.mail.CorreosUtils;
import com.paypay.baymax.front.config.security.jwt.JWTTokenUtils;
import com.paypay.baymax.front.config.security.sau.AuthUtils;
import com.paypay.baymax.front.service.security.SAUService;

import io.jsonwebtoken.Claims;

public class ResetPasswordService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private CommonMethodsService<?, ?> service;

	public ResetPasswordService(CommonMethodsService<?, ?> service) {
		this.service = service;
	}

	public ResponseEntity<ResponseDTO> sendResetPassword(String username, String urlResetPwd,
			JWTTokenUtils jwtTokenUtils, SAUService sauService, HttpServletRequest request, String updateUsername) {
		ResponseDTO responseDTO = null;

		try {

			Gson gson = service.getGson();

			HashMap<String, ?> result = new HashMap<>();
			List<String> authorities = new ArrayList<String>();
			authorities.add("ROLE_ACCESO_PAYPAY");

			/**
			 * 1. Generar Claims para Token
			 */

			Claims claims = jwtTokenUtils.generateClaims(username, authorities);

			/**
			 * 2. Generar Token de reseteo de password
			 */

			LocalDateTime currentTime = LocalDateTime.now();

			Date expiration = Date
					.from(currentTime.plusMinutes(jwtTokenUtils.getJwtSettingsRPwd().getTokenExpirationTime())
							.atZone(ZoneId.systemDefault()).toInstant());

			String jwtToken = jwtTokenUtils.generateJWTTokenResetPwd(claims, expiration);

			String url = urlResetPwd + "auth/resetpwd?token=" + jwtToken;

			/**
			 * 3. Obtener información adicional del usuario: correoElectronico
			 */
			responseDTO = sauService.securityPoliciesUser("userdetails/" + username + "/" + username, request, null,
					username, UsersDTO.class);

			result = gson.fromJson(gson.toJson(responseDTO.getResults()), new TypeToken<HashMap<String, Object>>() {
			}.getType());
			UsersDTO usersDTO = gson.fromJson(gson.toJson(result.get("userDetails")), new TypeToken<UsersDTO>() {
			}.getType());

			// SAUDTO user = gson.fromJson(gson.toJson(result.get("userDetails")), new
			// TypeToken<SAUDTO>() {}.getType());

			ResponseEntity<ResponseDTO> responseRsPwd = null;

			/**
			 * 3.1 Evalua si el usuario esta activo y puede solicitar reset de password
			 */
			if (usersDTO.getLocked()) {
				/**
				 * 3.2 Generar registro de solicitud de reseto de password
				 */

				SecurityDTO secDTORsPwd = new SecurityDTO();
				secDTORsPwd.setUsername(usersDTO.getUsername());
				secDTORsPwd.setTokenAnterior(jwtTokenUtils.getJwtSettingsRPwd().getTokenPreFix() + " " + jwtToken);
				secDTORsPwd.setTokenVencimiento(expiration);

				log.info("updateUsername: " + updateUsername);

				responseRsPwd = sauService.requestResetPassword(secDTORsPwd, request,
						"Solicitud de reseteo de password para: " + username,
						StringUtils.isNotBlank(updateUsername) ? updateUsername : username, SecurityDTO.class);

				/**
				 * 3.3 asigna bandera para validar continuación de proceso según respuesta de
				 * registro de solicitud de reset de password
				 */
				boolean isContinue = false;

				if (responseRsPwd != null) {
					if (responseRsPwd.getStatusCode().compareTo(HttpStatus.OK) == 0) {
						if (responseRsPwd.getBody() != null) {
							if (responseRsPwd.getBody().getMeta() != null) {
								if (StringUtils.isNotBlank(responseRsPwd.getBody().getMeta().getCode())) {
									isContinue = responseRsPwd.getBody().getMeta().getCode()
											.compareTo(DefinicionesComunes.CODIGO_OK) == 0;
								}
							}
						}
					}
				}

				if (isContinue) {
					/**
					 * 4. Iniciar proceso de correo
					 */

					PlantillaCorreoDTO plantilla = null;
					result.clear();

					CorreosUtils correoUtils = new CorreosUtils();
					CorreoDTO correoDTO = new CorreoDTO();

					/**
					 * 4.1. Obtener plantilla correo
					 */
					responseDTO = service.GET("/plantillaCorreo/getForSend/RESETPWDU", request, null, username,
							PlantillaCorreoDTO.class, "3002").getBody();

					result = gson.fromJson(gson.toJson(responseDTO.getResults()),
							new TypeToken<HashMap<String, Object>>() {
							}.getType());

					plantilla = gson.fromJson(gson.toJson(result.get("plantillaCorreo")),
							new TypeToken<PlantillaCorreoDTO>() {
							}.getType());
					plantilla.setCc("");
					plantilla.setCco("");
					plantilla.setMultiListPerfilesDestinatarios("");
					plantilla.setDestinatariosOpcionales(usersDTO.getEmail());

					/**
					 * 4.2. Setear mapa de valores para el cuerpo de la plantilla
					 */

					HashMap<String, String> mapValues = new HashMap<>();
					mapValues.put("\\$\\{NOMBRECOMPLETO}", usersDTO.getFirstName());
					mapValues.put("\\$\\{ENLACE}", url);
					mapValues.put("\\$\\{FECHA}", DateTime.now().toString("dd/MM/YYYY"));

					plantilla.setMapValues(mapValues);

					/**
					 * 4.3. Procesar plantilla
					 */
					plantilla = correoUtils.procesarPlantilla(plantilla, null);

					correoDTO.setAsunto(plantilla.getAsunto());
					correoDTO.setCuerpo(plantilla.getCuerpo());
					correoDTO.setDe(plantilla.getRemitente());
					correoDTO.setEstatus("pendiente");
					correoDTO.setOrigen("paypay");
					correoDTO.setPara(plantilla.getDestinatariosOpcionales());

					/**
					 * 4.4. Generar registro de correo
					 */

					ResponseEntity<ResponseDTO> responseDTOCorreo = service.sendCorreo(correoDTO, request);

					HashMap<String, Object> responseToMessage = new HashMap<String, Object>();
					responseToMessage.put("nombre", usersDTO.getFirstName());
					responseToMessage.put("fullName",
							CommonUtils.getFullName(usersDTO.getLastName(), usersDTO.getFirstName()));
					responseDTOCorreo.getBody().setResults(responseToMessage);
					return responseDTOCorreo;
				} else {
					return responseRsPwd;
				}
			} else {
				log.info(AuthUtils.RESET_REJECTED);
				/**
				 * Usuario con campo "activo" en false, se rechaza el reset de password
				 */
				return responseRsPwd = ResponseEntity.ok().body(new ResponseDTO(new MetaDTO(request.hashCode(),
						AuthUtils.RESET_REJECTED, "Su usuario está bloqueado, consulte al administrador"), null));
			}

		} catch (Exception e) {
			log.error("Error al intentar enviar correo electrónico");
			return service.buildErrorResponse(request.hashCode(), "ERROR", e.getClass().getSimpleName(), e.getMessage(),
					new HashMap<String, Object>());
		}

	}
}
