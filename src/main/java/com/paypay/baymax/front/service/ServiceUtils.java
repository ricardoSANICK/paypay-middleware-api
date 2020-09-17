package com.paypay.baymax.front.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.google.gson.JsonSyntaxException;
import com.paypay.baymax.commons.DTO.BitacoraDTO;
import com.paypay.baymax.commons.DTO.mail.CorreoDTO;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.commons.util.GsonBuild;
import com.paypay.baymax.commons.util.Modulos;
import com.paypay.baymax.commons.util.RemoteHostInfo;

@Component
public class ServiceUtils<E, SS> extends RequestUtils {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * GET
	 * 
	 * @param request
	 * @param path
	 * @return
	 */
	public ResponseEntity<ResponseDTO> GET(String path, HttpServletRequest request, String accion, String principal,
			Class<?> entity, String errorcode) {

		ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(), null);
		String jsonResponse = null;

		try {
			jsonResponse = requestRS.GETRequest(String.class, path);
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		} catch (Exception e) {
			log.error("Error al procesar la peticion");
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		}

		if (StringUtils.isNotBlank(accion) && entity != null)
			this.buildBitacora(entity, request, path, principal, accion);

		return super.buildResponseEntity(responseDTO);
	}

	/**
	 * GET
	 *
	 * @param request
	 * @param path
	 * @return
	 */
	public ResponseEntity<ResponseDTO> GET(String path, HttpServletRequest request, String accion, String principal,
			Object entity, String errorcode) {

		ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(), null);
		String jsonResponse = null;

		try {
			jsonResponse = requestRS.GETRequest(String.class, path);
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		} catch (Exception e) {
			log.error("Error al procesar la peticion");
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		}

		if (StringUtils.isNotBlank(accion) && entity != null)
			this.buildBitacora(entity, request, path, principal, accion);

		return super.buildResponseEntity(responseDTO);
	}

	/**
	 * 
	 * @param path
	 * @param request
	 * @param entity
	 * @param accion
	 * @param principal
	 * @param errorcode
	 * @return
	 */
	public ResponseEntity<ResponseDTO> POST(String path, HttpServletRequest request, Object entity, String accion,
			String principal, String errorcode) {

		ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(), null);
		String jsonResponse = null;

		try {
			jsonResponse = requestRS.POSTRequest(jwt.toJWT(entity, new GsonBuild().getGsonDate()), String.class, path);
			responseDTO = this.handleResponse(jsonResponse, request != null ? request.hashCode() : 0, path, errorcode);
		} catch (Exception e) {
			log.error("Error al procesar la peticion");
			responseDTO = this.handleResponse(jsonResponse, request != null ? request.hashCode() : 0, path, errorcode);
		}

		if (StringUtils.isNotBlank(accion) && entity != null)
			this.buildBitacora(entity, request, path, principal, accion);

		return super.buildResponseEntity(responseDTO);
	}

	/**
	 * 
	 * @param path
	 * @param request
	 * @param entity
	 * @param accion
	 * @param principal
	 * @param errorcode
	 * @return
	 */
	@Async
	public void POSTAsync(String path, HttpServletRequest request, Object entity, String accion, String principal,
			String errorcode) {

		// ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(), null);
		// String jsonResponse = null;

		try {
			requestRS.POSTRequestAsync(jwt.toJWT(entity, new GsonBuild().getGsonDate()), String.class, path);

			// responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path,
			// errorcode);
		} catch (Exception e) {
			log.error("Error al procesar la peticion");
			// responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path,
			// errorcode);
		}

		if (StringUtils.isNotBlank(accion) && entity != null)
			this.buildBitacora(entity, request, path, principal, accion);

		// return super.buildResponseEntity(responseDTO);
	}

	/**
	 * POST
	 * 
	 * @param request
	 * @param path
	 * @param entity
	 * @return
	 */
	public ResponseEntity<ResponseDTO> POSTSS(String path, HttpServletRequest request, SS entity, String accion,
			String principal, String errorcode) {
		return this.POST(path, request, entity, accion, principal, errorcode);
	}

	/**
	 * PUT
	 * 
	 * @param path
	 * @param request
	 * @param entity
	 * @param accion
	 * @param principal
	 * @param errorcode
	 * @return
	 */
	public ResponseEntity<ResponseDTO> PUT(String path, HttpServletRequest request, E entity, String accion,
			String principal, String errorcode) {

		ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(), null);
		String jsonResponse = null;

		try {
			jsonResponse = requestRS.PATCHRequest(jwt.toJWT(entity, new GsonBuild().getGsonDate()), String.class, path);
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		} catch (Exception e) {
			log.error("Error al procesar la peticion");
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		}

		if (StringUtils.isNotBlank(accion) && entity != null)
			this.buildBitacora(entity, request, path, principal, accion);

		return super.buildResponseEntity(responseDTO);
	}

	/**
	 * DELETE
	 * 
	 * @param path
	 * @param request
	 * @param entity
	 * @param accion
	 * @param principal
	 * @param errorcode
	 * @return
	 */
	public ResponseEntity<ResponseDTO> DELETE(String path, HttpServletRequest request, E entity, String accion,
			String principal, String errorcode) {
		ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(), null);
		String jsonResponse = null;

		try {
			jsonResponse = requestRS.GETRequest(String.class, path);
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		} catch (Exception e) {
			log.error("Error al procesar la peticion");
			responseDTO = this.handleResponse(jsonResponse, request.hashCode(), path, errorcode);
		}

		if (StringUtils.isNotBlank(accion) && entity != null)
			this.buildBitacora(entity, request, path, principal, accion);

		return super.buildResponseEntity(responseDTO);
	}

	/**
	 * handleResponse
	 * 
	 * @param id
	 * @param errorcode
	 * @return
	 */
	private ResponseDTO handleResponse(String json, int id, String path, String errorcode) {

		ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(), null);

		try {
			responseDTO = jwt.fromJWT(json, new GsonBuild().getGsonDate(), ResponseDTO.class);

			if (responseDTO == null) {
				responseDTO = new ResponseDTO();
				responseDTO.setMeta(new MetaDTO(id, "CORE_ERROR", "CoreErrorException",
						"Ha ocurrido un error en el CORE", "Ha ocurrido un error en el CORE"));
			} else if (responseDTO.getMeta() == null) {
				responseDTO.setMeta(new MetaDTO(id, "CORE_ERROR", "CoreErrorException",
						"Ha ocurrido un error en el CORE", "Ha ocurrido un error en el CORE"));
			} else {
				String codigo = responseDTO.getMeta().getCode();

				if (StringUtils.isNotBlank(codigo)) {
					if (codigo.compareTo(DefinicionesComunes.CODIGO_OK) != 0) {
						responseDTO.getMeta().setCode(codigo.contains("UK") ? codigo : codigo + errorcode);
					}
				} else {
					responseDTO.getMeta().setCode("CORE_ERROR");
					responseDTO.getMeta()
							.setType(StringUtils.isNotBlank(responseDTO.getMeta().getType())
									? responseDTO.getMeta().getType()
									: "CoreErrorException");
					responseDTO.getMeta()
							.setMessage(StringUtils.isNotBlank(responseDTO.getMeta().getMessage())
									? responseDTO.getMeta().getMessage()
									: "Ha ocurrido un error en el CORE");
				}
			}
		} catch (ProcessingException e) {
			log.error("Error al obtener registro. " + e.getMessage());
			responseDTO.getMeta().setType(e.getClass().getSimpleName());
			responseDTO.getMeta().setMessage(e.getMessage());
			responseDTO.getMeta().setCode("WS" + errorcode);
		} catch (JsonSyntaxException e) {
			log.error("Error al obtener registro. " + e.getMessage());
			responseDTO.getMeta().setType(e.getClass().getSimpleName());
			responseDTO.getMeta().setMessage(e.getMessage());
			responseDTO.getMeta().setCode("JS" + errorcode);
		} catch (Exception e) {
			log.error("Error al obtener registro. " + e.getMessage());
			responseDTO.getMeta().setType(e.getClass().getSimpleName());
			responseDTO.getMeta().setMessage(e.getMessage());
			responseDTO.getMeta().setCode("CR" + errorcode);
		}

		return responseDTO;
	}

	public ResponseEntity<ResponseDTO> sendCorreo(CorreoDTO correoDTO, HttpServletRequest request) {
		return this.POST(super.correoURI, request, correoDTO, null, "reset", "3200");
	}

	private void buildBitacora(Object entity, HttpServletRequest request, String path, String principal,
			String accion) {

		try {
			String modulo = "";

			if (accion.contains("Acceso de usuario")) {
				modulo = "Login";
			} else if (accion.contains("Solicitud de reseteo de password")) {
				modulo = "Usuarios";
			} else if (accion.contains("Último Intento Fallido") || accion.contains("Bloqueo de usuario")) {
				modulo = "Seguridad | Login";
			} else if (path.contains("/bitacora/getByParameter")) {
				return;
			}

			else {
				modulo = entity.getClass().getSimpleName();
				modulo = Modulos.ENTIDADES.get(modulo);
				if (modulo == null) {
					Class<?> class_ = (Class<?>) entity;
					modulo = class_.getName();

					if (modulo.contains("Group_members") || modulo.toLowerCase().contains("members")) {
						modulo = Modulos.ENTIDADES.get("GroupMembers");
					} else if (modulo.contains("Group_authorities") || modulo.toLowerCase().contains("authorities")) {
						modulo = Modulos.ENTIDADES.get("GroupAuthorities");
					} else if (modulo.contains("Groups") || modulo.contains("GroupsDTO")) {
						modulo = Modulos.ENTIDADES.get("GroupsDTO");
					}
				}

			}

			BitacoraDTO biDTO = new BitacoraDTO();
			biDTO.setModulo(modulo);
			biDTO.setRequestAddress(RemoteHostInfo.getRemoteAddr(request));
			biDTO.setUrl(path);
			biDTO.setUsuario_username(principal);
			biDTO.setAccion(accion);

			generateBitacora(biDTO);
		} catch (Exception e) {
			log.error("No se puede generar un registro de bitácora.");
		}
	}

}