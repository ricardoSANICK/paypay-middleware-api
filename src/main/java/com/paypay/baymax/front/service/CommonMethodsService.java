package com.paypay.baymax.front.service;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.google.gson.reflect.TypeToken;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.util.GsonBuild;

@Service
public class CommonMethodsService<E, SS> extends ServiceUtils<E, SS> {

	public CommonMethodsService() {
	}

	public ResponseEntity<ResponseDTO> get(String path, HttpServletRequest request, Principal principal,
			Class<?> entity, String errorcode) {
		return GET(path, request, entity != null ? bitGET : null, getName(principal), entity, errorcode);
	}

	public ResponseEntity<ResponseDTO> getPOST(String path, E entity, HttpServletRequest request, Principal principal,
			String errorcode) {
		return POST(path, request, entity, bitGET, getName(principal), errorcode);
	}

	public ResponseEntity<ResponseDTO> getPOST(String path, E entity, HttpServletRequest request, Principal principal,
			String errorcode, boolean noAccion) {
		return POST(path, request, entity, noAccion ? null : bitGET, getName(principal), errorcode);
	}

	public String serverSideList(String path, SS entity, HttpServletRequest request, Principal principal,
			String errorcode) {

		ResponseDTO responseDTO = POSTSS(path, request, entity, null, getName(principal), errorcode).getBody();

		HashMap<String, Object> mapResult = gson.fromJson(gson.toJson(responseDTO.getResults()),
				new TypeToken<HashMap<String, Object>>() {
				}.getType());

		return new GsonBuild().getGson().toJson(mapResult.get("dtr"));

	}

	public ResponseEntity<ResponseDTO> save(String path, E entity, HttpServletRequest request, Principal principal,
			String errorcode) {
		return POST(path, request, entity, bitSAVE, getName(principal), errorcode);
	}

	public ResponseEntity<ResponseDTO> update(String path, E entity, HttpServletRequest request, Principal principal,
			String errorcode) {
		return POST(path, request, entity, bitUPDATE, getName(principal), errorcode);
	}

	public ResponseEntity<ResponseDTO> updatePUT(String path, E entity, HttpServletRequest request, Principal principal,
			String errorcode) {
		return PUT(path, request, entity, bitUPDATE, getName(principal), errorcode);
	}

	public ResponseEntity<ResponseDTO> delete(String path, HttpServletRequest request, Principal principal,
			Class<?> entity, String errorcode) {
		return GET(path, request, bitDELETE, getName(principal), entity, errorcode);
	}

	public ResponseEntity<ResponseDTO> deletePOST(String path, E entity, HttpServletRequest request,
			Principal principal, String errorcode) {
		return POST(path, request, entity, bitDELETE, getName(principal), errorcode);
	}

	public ResponseEntity<ResponseDTO> disable(String path, String accion, HttpServletRequest request,
			Principal principal, Class<?> entity, String errorcode) {
		return GET(path, request, accion, getName(principal), entity, errorcode);
	}

	public ResponseEntity<ResponseDTO> disable(String path, String accion, HttpServletRequest request,
			Principal principal, Object entity, String errorcode) {
		return GET(path, request, accion, getName(principal), entity, errorcode);
	}

	public ResponseEntity<ResponseDTO> disablePOST(String path, E entity, HttpServletRequest request,
			Principal principal, String errorcode) {
		return POST(path, request, entity, bitDISABLE, getName(principal), errorcode);
	}

	public String pathBuilder(Object... pathElements) {
		return Arrays.asList(pathElements).stream().map(Object::toString).collect(Collectors.joining("/")).toString();
	}

}
