package com.paypay.baymax.front.service;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.nxn.sau.security.util.JWTEncripter;
import com.paypay.baymax.commons.DTO.BitacoraDTO;
import com.paypay.baymax.commons.DTO.mail.CorreoDTO;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.enums.BitacoraAccionesEnum;
import com.paypay.baymax.front.ws.RequestRS;

@Component
public class RequestUtils {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public RequestRS requestRS;

	@Autowired
	public Gson gson;

	@Autowired
	public JWTEncripter jwt;

	public final String bitURI = "bitacora/save";
	public final String correoURI = "correo/save";

	public final String bitGET = BitacoraAccionesEnum.GET.getDescripcion();
	public final String bitSAVE = BitacoraAccionesEnum.SAVE.getDescripcion();
	public final String bitUPDATE = BitacoraAccionesEnum.UPDATE.getDescripcion();
	public final String bitENABLE = BitacoraAccionesEnum.ENABLE.getDescripcion();
	public final String bitDISABLE = BitacoraAccionesEnum.DISABLE.getDescripcion();
	public final String bitDELETE = BitacoraAccionesEnum.DELETE.getDescripcion();

	/**
	 * buildResponseEntity
	 * 
	 * @param responseDTO
	 * @return
	 */
	public ResponseEntity<ResponseDTO> buildResponseEntity(ResponseDTO responseDTO) {
		return ResponseEntity.ok().body(responseDTO);
	}

	/**
	 * 
	 * @param id
	 * @param code
	 * @param status
	 * @param message
	 * @param result
	 * @return
	 */
	public ResponseEntity<ResponseDTO> buildOKResponse(int id, String code, String message, Object result) {
		return ResponseEntity.ok().body(new ResponseDTO(new MetaDTO(id, code, message), result));
	}

	public ResponseEntity<ResponseDTO> buildErrorResponse(int id, String code, String type, String message,
			Object result) {
		return ResponseEntity.ok().body(new ResponseDTO(new MetaDTO(id, code, type, message, null), result));
	}

	public void generateBitacora(BitacoraDTO DTO) {
		try {
			requestRS.POSTRequest(jwt.toJWT(DTO, gson), String.class, bitURI);
		} catch (Exception e) {
			log.info("Error al generar registro en bitacora. " + e.getMessage());
		}
	}

	public void getPlantillaCorreo(CorreoDTO correoDTO) {
		try {
			requestRS.GETRequest(String.class, "plantillaCorreo/get");
		} catch (Exception e) {
			log.info("Error al generar registro de correo. " + e.getMessage());
		}
	}

	public void generateCorreo(CorreoDTO correoDTO) {
		try {
			requestRS.POSTRequest(jwt.toJWT(correoDTO, gson), String.class, correoURI);
		} catch (Exception e) {
			log.info("Error al generar registro de correo. " + e.getMessage());
		}
	}

	public String getName(Principal principal) {
		return principal != null ? principal.getName() : null;
	}

	public Gson getGson() {
		return gson;
	}

}
