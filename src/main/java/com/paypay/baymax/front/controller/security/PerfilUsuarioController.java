package com.paypay.baymax.front.controller.security;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.paypay.baymax.commons.DTO.pagination.DataTableRequest;
import com.paypay.baymax.commons.DTO.security.PerfilUsuarioDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.meta.ErrorCodes;
import com.paypay.baymax.front.service.CommonMethodsService;

@RestController
@RequestMapping(EndPoints.PERFIL_USUARIO)
@PreAuthorize("hasRole('ROLE_ACCESO')")
public class PerfilUsuarioController {

	public final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CommonMethodsService<PerfilUsuarioDTO, DataTableRequest<Object>> service;

	private final String mainURI = EndPoints.USERS;
	private final String errorCode = ErrorCodes.PERFIL_USUARIO;

	@Value("${com.paypay.baymax.web.front.uri}")
	private String urlResetPwd;

	//@Autowired
	//private ImageUtils imageUtils;

	
	@GetMapping(value = "/{id:.+}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ResponseDTO> get(@PathVariable("id") String id, Principal principal,
			HttpServletRequest request) {
		return service.getPOST(service.pathBuilder(mainURI, "get"), new PerfilUsuarioDTO(id), request, principal,
				this.errorCode, true);
	}
	
	@PutMapping(value = "")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ResponseDTO> update(RequestEntity<PerfilUsuarioDTO> requestEntity, Principal principal,
			HttpServletRequest request) {
		requestEntity.getBody().setUpdateUsername(principal.getName());
		//requestEntity.getBody().setAvatar(imageUtils.proccessBase64Img(requestEntity.getBody().getAvatar()));
		return service.update(mainURI + "/updatePerfil", requestEntity.getBody(), request, principal, this.errorCode);
	}

}
