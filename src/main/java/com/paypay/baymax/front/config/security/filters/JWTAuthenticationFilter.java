package com.paypay.baymax.front.config.security.filters;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.GenericFilterBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.front.config.security.jwt.JWTSettings;
import com.paypay.baymax.front.config.security.jwt.JWTSettingsResetPassword;
import com.paypay.baymax.front.config.security.jwt.TokenAuthenticationService;
import com.paypay.baymax.front.config.security.sau.AuthUtils;
import com.paypay.baymax.front.controller.VueEndPoints;

public class JWTAuthenticationFilter extends GenericFilterBean {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private JWTSettings jwtSettings;
	private JWTSettingsResetPassword jwtSettingsResetPassword;

	public JWTAuthenticationFilter(JWTSettings jwtSettings, JWTSettingsResetPassword jwtSettingsResetPassword) {
		this.jwtSettings = jwtSettings;
		this.jwtSettingsResetPassword = jwtSettingsResetPassword;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		String requestURI = ((HttpServletRequest) request).getRequestURI().toString();
		if (requestURI.compareTo("/") == 0 || requestURI.compareTo("/login") == 0 || requestURI.contains("/static")
				|| requestURI.contains("/img") || requestURI.contains("/css") || requestURI.contains("/js")
				|| requestURI.contains("/fonts") || requestURI.contains("/favicon.ico")
				|| requestURI.compareTo("/toastr.js.map") == 0 || requestURI.contains("/resetSessions")
				|| requestURI.contains(".js") || requestURI.contains("index.html")
				|| requestURI.contains("manifest.json") || requestURI.contains(VueEndPoints.RESET_PWD)
				|| requestURI.contains(VueEndPoints.FORGET_PWD) || requestURI.contains(VueEndPoints.SESSION_EXPIRED_MSG)
				|| requestURI.contains(VueEndPoints.EXPIRED_PWD) || requestURI.contains(VueEndPoints.AUTH_RESET_PWD)
				|| requestURI.contains(VueEndPoints.AUTH_FORGET_PWD)
				|| requestURI.contains(EndPoints.LOGIN)
				//------Start main ------//
				|| requestURI.contains(EndPoints.COMBOS)
				|| requestURI.contains(EndPoints.EMPLOYEES)
				|| requestURI.contains(EndPoints.REVIEWS)
				|| requestURI.contains(EndPoints.FEEDBACKS)
				//------End main ------//
				|| requestURI.contains(VueEndPoints.AUTH_SESSION_EXPIRED_MSG)
				|| requestURI.contains(VueEndPoints.AUTH_EXPIRED_PWD) || requestURI.contains(VueEndPoints.AUTH)
				|| requestURI.contains(VueEndPoints.HOME) || requestURI.contains(VueEndPoints.API_COMBOS)
				|| requestURI.contains(EndPoints.IMAGE_RESOURCES) || requestURI.contains(EndPoints.FILE_RESOURCES)
				|| requestURI.compareTo(EndPoints.API_RESET_PASSWORD) == 0
				|| requestURI.compareTo(EndPoints.CHANGE_PASSWORD) == 0) {
			filterChain.doFilter(request, response);
		} else {
			Authentication authentication = TokenAuthenticationService.getAuthentication((HttpServletRequest) request,
					jwtSettings, jwtSettingsResetPassword);

			HttpSession session = ((HttpServletRequest) request).getSession(false);

			if (session != null && authentication != null && authentication.isAuthenticated()) {
				filterChain.doFilter(request, response);
			} else if (authentication != null && authentication.isAuthenticated()
					&& requestURI.compareTo(EndPoints.API_RESET_PASSWORD) == 0) {
				// filterChain.doFilter(request,response);
			} else {
				boolean sauUris = false;

				requestURI = ((HttpServletRequest) request).getRequestURI().toString();

				if (StringUtils.isNotBlank(requestURI)) {

					log.info("uri: " + requestURI);
					if (sauUris = (requestURI.compareTo(EndPoints.CHANGE_PASSWORD) == 0
							|| requestURI.compareTo(EndPoints.REQUEST_RESET_PASSWORD) == 0
							|| requestURI.compareTo(EndPoints.API_RESET_PASSWORD) == 0
							|| requestURI.compareTo(EndPoints.RESET_PASSWORD) == 0 || requestURI.contains(".js")
							|| requestURI.contains("index.html") || requestURI.contains("manifest.json")
							|| requestURI.compareTo("/") == 0 || requestURI.compareTo("") == 0
							|| requestURI.compareTo("/error") == 0 || requestURI.contains("/static")
							|| requestURI.contains("/img") || requestURI.contains("/css") || requestURI.contains("/js")
							|| requestURI.contains("/fonts") || requestURI.contains("/favicon.ico")
							|| requestURI.contains(VueEndPoints.RESET_PWD)
							|| requestURI.contains(VueEndPoints.FORGET_PWD)
							|| requestURI.contains(VueEndPoints.SESSION_EXPIRED_MSG)
							|| requestURI.contains(VueEndPoints.EXPIRED_PWD)
							|| requestURI.contains(VueEndPoints.AUTH_RESET_PWD)
							|| requestURI.contains(VueEndPoints.AUTH_FORGET_PWD)
							|| requestURI.contains(VueEndPoints.AUTH_SESSION_EXPIRED_MSG)
							|| requestURI.contains(VueEndPoints.AUTH_EXPIRED_PWD)
							|| requestURI.contains(VueEndPoints.AUTH) || requestURI.contains(VueEndPoints.HOME)
							|| requestURI.contains(VueEndPoints.API_COMBOS) || requestURI.contains(EndPoints.BITACORA)
							|| requestURI.contains(EndPoints.IMAGE_RESOURCES)))
						filterChain.doFilter(request, response);

				}

				sauUris = requestURI.compareTo(EndPoints.LOGIN) == 0;

				if (sauUris == false) {
					log.info("No autorizado");
					try {
						HttpServletResponse resp = (HttpServletResponse) response;

						log.info("status: " + resp.getStatus());

						resp.setStatus(HttpStatus.UNAUTHORIZED.value());
						resp.setContentType(MediaType.APPLICATION_JSON_VALUE);

						ResponseDTO responseDTO = new ResponseDTO(new MetaDTO(request.hashCode(),
								AuthUtils.ERROR_NOT_PERMITTED, "AccessDenied", "Acceso no permitido", null), null);

						ObjectMapper mapper = new ObjectMapper();
						mapper.writeValue(response.getWriter(), responseDTO);

					} catch (AuthenticationException failed) {
						log.info(failed.getMessage());
					} catch (Exception e) {
						log.info(e.getMessage());
					}

				}
			}
		}
	}

}
