package com.paypay.baymax.front.controller.security;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import com.paypay.baymax.commons.DTO.security.UsersFormDTO;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.meta.ErrorCodes;
import com.paypay.baymax.front.config.security.jwt.JWTSettings;
import com.paypay.baymax.front.config.security.jwt.JWTSettingsResetPassword;
import com.paypay.baymax.front.config.security.jwt.JWTTokenUtils;
import com.paypay.baymax.front.config.security.sau.AuthUtils;
import com.paypay.baymax.front.service.CommonMethodsService;
import com.paypay.baymax.front.service.ResetPasswordService;
import com.paypay.baymax.front.service.security.SAUService;

@RestController
@RequestMapping(EndPoints.USERS)
@PreAuthorize("hasAnyRole('ROLE_ACCESO','ROLE_ACCESO_SEGURIDAD')")
public class UsersController {

	private final CommonMethodsService<UsersFormDTO, DataTableRequest<Object>> service;

	private final String mainURI = EndPoints.USERS;
	private final String errorCode = ErrorCodes.USERS;

	@Value("${com.paypay.baymax.web.front.urlResetPwd}")
	private String urlResetPwd;

	//private final ImageUtils imageUtils;

	private final JWTSettings jwtSettings;

	private final JWTSettingsResetPassword jwtSettingsResetPWD;

	private final SAUService sauService;

	private final UserDetailsService userDetailsService;

	@Autowired
	public UsersController(CommonMethodsService<UsersFormDTO, DataTableRequest<Object>> service, 
			//ImageUtils imageUtils,
			JWTSettings jwtSettings, JWTSettingsResetPassword jwtSettingsResetPWD, SAUService sauService,
			UserDetailsService userDetailsService) {
		this.service = service;
		//this.imageUtils = imageUtils;
		this.jwtSettings = jwtSettings;
		this.jwtSettingsResetPWD = jwtSettingsResetPWD;
		this.sauService = sauService;
		this.userDetailsService = userDetailsService;
	}
	
	
	@GetMapping(value = "/{username:.+}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ResponseDTO> get(@PathVariable("username") String username, Principal principal,
			HttpServletRequest request) {
		return service.getPOST(service.pathBuilder(mainURI, "get"), new UsersFormDTO(username), request, principal,
				this.errorCode, true);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ResponseDTO> getAll(Principal principal, HttpServletRequest request) {
		return service.get(service.pathBuilder(mainURI), request, principal, null, this.errorCode);
	}
	
	@PostMapping(value = "/list")
	@ResponseStatus(HttpStatus.OK)
	public String serverSideList(Principal principal, HttpServletRequest request) {
		return service.serverSideList(mainURI + "/getServerSideList", new DataTableRequest<Object>(request), request,
				principal, this.errorCode);
	}
	
	@PostMapping(value = "/treeList")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ResponseDTO> treeList(Principal principal, HttpServletRequest request) {
		return service.getPOST(service.pathBuilder(mainURI, "treeList"), null, request, principal, this.errorCode,
				true);
	}
	
	
	@PostMapping(value = "")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> save(RequestEntity<UsersFormDTO> requestEntity, Principal principal,
			HttpServletRequest request) {
		requestEntity.getBody().setRecordUsername(principal.getName());
		//requestEntity.getBody().setAvatar(imageUtils.proccessBase64Img(requestEntity.getBody().getAvatar()));
		return service.save(mainURI + "/save", requestEntity.getBody(), request, principal, this.errorCode);
	}
	
	@PutMapping(value = "")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> update(RequestEntity<UsersFormDTO> requestEntity, Principal principal,
			HttpServletRequest request) {
		requestEntity.getBody().setUpdateUsername(principal.getName());
		//requestEntity.getBody().setAvatar(imageUtils.proccessBase64Img(requestEntity.getBody().getAvatar()));
		return service.update(mainURI + "/update", requestEntity.getBody(), request, principal, this.errorCode);
	}
	
	@PutMapping(value = "/{username:.+}/{enabled}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> updateStatus(@PathVariable("username") String username,
			@PathVariable("enabled") boolean enabled, Principal principal, HttpServletRequest request) {

		UsersFormDTO usersFormDTO = new UsersFormDTO();
		usersFormDTO.setUsername(username);
		usersFormDTO.setLocked(enabled);
		usersFormDTO.setEnabled(enabled);
		usersFormDTO.setUpdateUsername(principal.getName());

		return service.update(mainURI + "/updateStatus", usersFormDTO, request, principal, this.errorCode);
	}
	
	@DeleteMapping(value = "")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> disable(@RequestParam("username") String username, Principal principal,
			HttpServletRequest request) {

		return service.disablePOST(mainURI + "/disable", new UsersFormDTO(username, principal.getName()), request,
				principal, this.errorCode);

	}
	
	@GetMapping(value = "/resetPassword/{username:.+}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_MODIFICAR_SEGURIDAD')")
	public ResponseEntity<ResponseDTO> resetPassword(@PathVariable("username") String username, Principal principal,
			HttpServletRequest request) {
		ResetPasswordService resetPasswordService = new ResetPasswordService(service);

		try {
			userDetailsService.loadUserByUsername(username);
			return resetPasswordService.sendResetPassword(username, urlResetPwd,
					new JWTTokenUtils(jwtSettings, jwtSettingsResetPWD), sauService, request, principal.getName());
		} catch (UsernameNotFoundException unfe) {
			return ResponseEntity.ok().body(new ResponseDTO(new MetaDTO(request.hashCode(),
					AuthUtils.ERROR_USER_NOT_ASSIGNED, AuthUtils.ERROR_USER_NOT_ASSIGNED), null));
		}

	}

}
