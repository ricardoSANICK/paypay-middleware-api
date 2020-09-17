package com.paypay.baymax.front.config.security.handlers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;

public class CustomAccessDeniedHanderImpl implements AccessDeniedHandler {

	protected static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHanderImpl.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		if (!response.isCommitted()) {

			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(request.hashCode(), "AccessDenied",
					"AccessDeniedException",
					accessDeniedException != null ? accessDeniedException.getMessage() : "AccessDeniedException", null),
					null);

			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(response.getWriter(), responseDTO);

		}
	}

}
