package com.paypay.baymax.front.service.security;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.nxn.sau.security.DTO.SecurityDTO;
import com.paypay.baymax.commons.DTO.pagination.DataTableRequest;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.front.service.ServiceUtils;

@Service
public class SAUService extends ServiceUtils<SecurityDTO, DataTableRequest<Object>> {

	private final static Logger log = LoggerFactory.getLogger(SAUService.class);

	private final String mainURI = "/api/v1/paypay/core/sau";

	// Obtener las politicas de seguridad asignadas al usuario seleccionado
	// ........................................

	/**
	 * 
	 * @param params
	 * @param request
	 * @param accion
	 * @param principal
	 * @param entity
	 * @return
	 */
	public ResponseDTO securityPoliciesUser(String params, HttpServletRequest request, String accion, String principal,
			Class<?> entity) {
		try {
			return GET(mainURI + "/securityPoliciesUser/" + params, request, accion, principal, entity, "").getBody();
		} catch (Exception e) {
			log.error("Tipo de dato no coinicide: " + e.getMessage());
			return null;
		}
	}

	/**
	 * 
	 * @param securityDTO
	 * @param request
	 * @param accion
	 * @param principal
	 * @param entity
	 * @return
	 */
	public ResponseEntity<ResponseDTO> changePassword(SecurityDTO securityDTO, HttpServletRequest request,
			String accion, String principal, Class<?> entity) {
		try {
			return POST(mainURI + "/changePassword", request, securityDTO, accion, principal, "");
		} catch (Exception e) {
			log.error("Tipo de dato no coinicide: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @param securityDTO
	 * @param request
	 * @param accion
	 * @param principal
	 * @param entity
	 * @return
	 */
	public ResponseEntity<ResponseDTO> resetPassword(SecurityDTO securityDTO, HttpServletRequest request, String accion,
			String principal, Class<?> entity) {
		try {
			return POST(mainURI + "/resetPassword", request, securityDTO, accion, principal, "");
		} catch (Exception e) {
			log.error("Tipo de dato no coinicide: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @param securityDTO
	 * @param request
	 * @param accion
	 * @param principal
	 * @param entity
	 * @return
	 */
	public ResponseEntity<ResponseDTO> requestResetPassword(SecurityDTO securityDTO, HttpServletRequest request,
			String accion, String principal, Class<?> entity) {
		try {
			securityDTO.setUsuariomodificacion(principal);
			return POST(mainURI + "/requestResetPassword", request, securityDTO, accion, principal, "");
		} catch (Exception e) {
			log.error("Tipo de dato no coinicide: " + e.getMessage());
			return null;
		}

	}

}
