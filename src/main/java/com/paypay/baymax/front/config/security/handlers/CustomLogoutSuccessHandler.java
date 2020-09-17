package com.paypay.baymax.front.config.security.handlers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.DTO.BitacoraDTO;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.commons.util.RemoteHostInfo;
import com.paypay.baymax.front.service.BitacoraService;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

	private final Logger log = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

	private final BitacoraService bitacoraService;

	public CustomLogoutSuccessHandler(BitacoraService bitacoraService) {
		this.bitacoraService = bitacoraService;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		String username = (authentication != null ? authentication.getName() : "");

		log.info("Logout: " + username);

		if (StringUtils.isNotBlank(username)) {
			BitacoraDTO biDTO = new BitacoraDTO();
			biDTO.setModulo("Logout");
			biDTO.setRequestAddress(RemoteHostInfo.getRemoteAddr(request));
			biDTO.setUrl("/logout");
			biDTO.setUsuario_username(username);
			biDTO.setAccion("Logout");

			try {
				if (bitacoraService != null) {
					bitacoraService.saveBitacora(biDTO);
				} else {
					log.error("Service is null");
				}
			} catch (Exception e) {
				log.error("Error: " + e.getMessage());
			}
		}

		String requestURI = request.getRequestURI().toString();
		log.info("Logout correcto de: " + requestURI);

		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getWriter(), new ResponseDTO(
				new MetaDTO(request.hashCode(), DefinicionesComunes.CODIGO_OK, "Logout correcto"), null));

	}

}
