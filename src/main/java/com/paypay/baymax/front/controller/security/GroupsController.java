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
import com.paypay.baymax.commons.DTO.security.GroupsDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.meta.ErrorCodes;
import com.paypay.baymax.front.service.CommonMethodsService;


@RestController
@RequestMapping(EndPoints.GROUPS)
@PreAuthorize("hasAnyRole('ROLE_ACCESO','ROLE_ACCESO_SEGURIDAD')")
public class GroupsController {

	private final CommonMethodsService<GroupsDTO,DataTableRequest<Object>> service;

	private final String mainURI = EndPoints.GROUPS;
	private final Class<GroupsDTO> DTOClass = GroupsDTO.class;
	private final String errorCode = ErrorCodes.GROUPS;
	private static final String ROLE_ACCESO_ELIMINAR_PERFILES = "hasRole(T(com.paypay.baymax.commons.security.CustomGrantedAuthorities).ROLE_ACCESO_ELIMINAR_PERFILES.toString())";

	@Autowired
	public GroupsController(CommonMethodsService<GroupsDTO, DataTableRequest<Object>> service) {
		this.service = service;
	}
	
	@GetMapping(value="/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ResponseDTO> get(@PathVariable("id") String id
					   , Principal principal
					   , HttpServletRequest request) {		
		return service.get(service.pathBuilder(mainURI,id)
						   , request
						   , principal
						   , null
						   , this.errorCode);
	}
	
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResponseDTO> getAll(Principal principal
            , HttpServletRequest request) {
        return service.get(service.pathBuilder(mainURI)
                , request
                , principal
                , null
                , this.errorCode);
    }
    
	@PostMapping(value="")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> save(
						RequestEntity<GroupsDTO>  requestEntity
						, Principal principal
						, HttpServletRequest request) {
		requestEntity.getBody().setRecordUsername(principal.getName());
		return service.save(mainURI + "/save", requestEntity.getBody(), request, principal,this.errorCode);
	}
	
	@PutMapping(value="")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> update(
			RequestEntity<GroupsDTO>  requestEntity
			, Principal principal
			, HttpServletRequest request) {
		requestEntity.getBody().setUpdateUsername(principal.getName());
		return service.update(mainURI + "/update", requestEntity.getBody(), request, principal,this.errorCode);
	}
	
	@DeleteMapping(value="")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ROLE_ACCESO_ELIMINAR_PERFILES)
	public ResponseEntity<ResponseDTO> delete(
				 @RequestParam("id") String id		  
			   , Principal principal
			   , HttpServletRequest request) {
		return service.delete(service.pathBuilder(mainURI,"delete",id)
				   , request
				   , principal
				   , DTOClass
				   , this.errorCode);
	}
}
