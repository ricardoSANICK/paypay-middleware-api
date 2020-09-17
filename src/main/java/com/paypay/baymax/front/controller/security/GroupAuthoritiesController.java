package com.paypay.baymax.front.controller.security;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.paypay.baymax.commons.DTO.pagination.DataTableRequest;
import com.paypay.baymax.commons.DTO.security.Group_authoritiesDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.meta.ErrorCodes;
import com.paypay.baymax.front.service.CommonMethodsService;

@RestController
@RequestMapping(EndPoints.GROUP_AUTHORITIES)
@PreAuthorize("hasAnyRole('ROLE_ACCESO','ROLE_ACCESO_SEGURIDAD')")
public class GroupAuthoritiesController {

	private final CommonMethodsService<Group_authoritiesDTO, DataTableRequest<Object>> service;

	private final String mainURI = EndPoints.GROUP_AUTHORITIES;
	private final Class<Group_authoritiesDTO> DTOClass = Group_authoritiesDTO.class;
	private final String errorCode = ErrorCodes.GROUP_AUTHORITIES;

	@Autowired
	public GroupAuthoritiesController(CommonMethodsService<Group_authoritiesDTO, DataTableRequest<Object>> service) {
		this.service = service;
	}

	@GetMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ResponseDTO> get(@PathVariable("id") String id, Principal principal,
			HttpServletRequest request) {

		return service.get(service.pathBuilder(mainURI, id, "authorities"), request, principal, DTOClass,
				this.errorCode);
	}

	@PostMapping(value = "")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> save(RequestEntity<Group_authoritiesDTO> requestEntity, Principal principal,
			HttpServletRequest request) {
		requestEntity.getBody().setRecordUsername(principal.getName());
		return service.save(mainURI + "/save", requestEntity.getBody(), request, principal, this.errorCode);
	}

	@PutMapping(value = "")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> update(RequestEntity<Group_authoritiesDTO> requestEntity, Principal principal,
			HttpServletRequest request) {
		requestEntity.getBody().setUpdateUsername(principal.getName());
		return service.update(mainURI + "/update", requestEntity.getBody(), request, principal, this.errorCode);
	}

	@DeleteMapping(value = "")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> delete(@RequestParam("group_id") String group_id,
			@RequestParam("authority") String authority, Principal principal, HttpServletRequest request) {
		return service.delete(service.pathBuilder(mainURI, "delete", group_id, authority, principal.getName()), request,
				principal, DTOClass, this.errorCode);
	}

}
