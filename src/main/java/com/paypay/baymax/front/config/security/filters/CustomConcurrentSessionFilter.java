package com.paypay.baymax.front.config.security.filters;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.DTO.util.MetaDTO;
import com.paypay.baymax.commons.DTO.util.ResponseDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.util.DefinicionesComunes;
import com.paypay.baymax.front.controller.VueEndPoints;

public class CustomConcurrentSessionFilter extends ConcurrentSessionFilter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final SessionRegistry sessionRegistry;
	private String expiredUrl;
	private RedirectStrategy redirectStrategy;
	private LogoutHandler handlers = new CompositeLogoutHandler(new SecurityContextLogoutHandler());
	private SessionInformationExpiredStrategy sessionInformationExpiredStrategy;

	public CustomConcurrentSessionFilter(SessionRegistry sessionRegistry, String expiredURL) {
		super(sessionRegistry, expiredURL);
		this.sessionRegistry = sessionRegistry;
		this.expiredUrl = expiredURL;
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		String requestURI = request.getRequestURI().toString();

		if (EndPoints.LOGIN.compareTo(requestURI) == 0 || requestURI.compareTo(EndPoints.CHANGE_PASSWORD) == 0
				|| requestURI.compareTo(EndPoints.REQUEST_RESET_PASSWORD) == 0 
				|| requestURI.contains("/img")
				|| requestURI.contains(EndPoints.LOGIN)
				|| requestURI.contains("/css") || requestURI.contains("/js") || requestURI.contains("/fonts")
				|| requestURI.compareTo(EndPoints.API_RESET_PASSWORD) == 0
				|| requestURI.compareTo(EndPoints.RESET_PASSWORD) == 0 || requestURI.compareTo("/") == 0
				|| requestURI.compareTo("") == 0 || requestURI.compareTo("/error") == 0
				|| requestURI.contains("/static") || requestURI.contains("/favicon.ico") || requestURI.contains(".js")
				|| requestURI.contains("index.html") || requestURI.contains("manifest.json")
				|| requestURI.contains(VueEndPoints.RESET_PWD) || requestURI.contains(VueEndPoints.FORGET_PWD)
				|| requestURI.contains(VueEndPoints.SESSION_EXPIRED_MSG)
				|| requestURI.contains(VueEndPoints.EXPIRED_PWD) || requestURI.contains(VueEndPoints.AUTH_RESET_PWD)
				|| requestURI.contains(VueEndPoints.AUTH_FORGET_PWD)
				|| requestURI.contains(VueEndPoints.AUTH_SESSION_EXPIRED_MSG)
				|| requestURI.contains(VueEndPoints.AUTH_EXPIRED_PWD) || requestURI.contains(VueEndPoints.AUTH)
				|| requestURI.contains(VueEndPoints.HOME) || requestURI.contains(VueEndPoints.API_COMBOS)
				|| requestURI.contains(EndPoints.IMAGE_RESOURCES)
				//------Start main ------//
				|| requestURI.contains(EndPoints.COMBOS)
				|| requestURI.contains(EndPoints.EMPLOYEES)
				|| requestURI.contains(EndPoints.REVIEWS)
				|| requestURI.contains(EndPoints.FEEDBACKS)
				//------End main------//
				) {
			chain.doFilter(request, response);
		} else {
			HttpSession session = request.getSession(false);

			if (session != null) {
				SessionInformation info = sessionRegistry.getSessionInformation(session.getId());

				if (info != null) {
					if (info.isExpired()) {
						// Expired - abort processing
						if (logger.isDebugEnabled()) {
							logger.debug("Requested session ID " + request.getRequestedSessionId() + " has expired.");
						}
						// doLogout(request, response);

						this.sessionInformationExpiredStrategy
								.onExpiredSessionDetected(new SessionInformationExpiredEvent(info, request, response));
						return;
					} else {
						// Non-expired - update last request date/time
						sessionRegistry.refreshLastRequest(info.getSessionId());
					}
				}

				chain.doFilter(request, response);
			} else {
				log.debug("request.getContextPath(): " + request.getContextPath());
				log.debug("request.getPathInfo(): " + request.getPathInfo());

				log.info("Se ha vencido la sesión del usuario de: " + requestURI);

				response.setStatus(HttpStatus.ACCEPTED.value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);

				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(response.getWriter(), new ResponseDTO(
						new MetaDTO(request.hashCode(), DefinicionesComunes.CODIGO_LOGOUT, "Logout correcto"), null));
			}
		}
	}

}
