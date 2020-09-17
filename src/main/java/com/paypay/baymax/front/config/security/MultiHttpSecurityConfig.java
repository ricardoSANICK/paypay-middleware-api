package com.paypay.baymax.front.config.security;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import com.google.gson.Gson;
import com.paypay.baymax.commons.DTO.pagination.DataTableRequest;
import com.paypay.baymax.commons.DTO.security.UsersDTO;
import com.paypay.baymax.commons.meta.EndPoints;
import com.paypay.baymax.commons.security.CustomGrantedAuthorities;
import com.paypay.baymax.front.config.security.filters.CustomConcurrentSessionFilter;
import com.paypay.baymax.front.config.security.filters.JWTAuthenticationFilter;
import com.paypay.baymax.front.config.security.filters.JWTLoginFilter;
import com.paypay.baymax.front.config.security.handlers.CustomAccessDeniedHanderImpl;
import com.paypay.baymax.front.config.security.handlers.CustomLogoutSuccessHandler;
import com.paypay.baymax.front.config.security.jwt.JWTSettings;
import com.paypay.baymax.front.config.security.jwt.JWTSettingsResetPassword;
import com.paypay.baymax.front.config.security.jwt.JWTTokenUtils;
import com.paypay.baymax.front.config.security.jwt.TokenAuthenticationService;
import com.paypay.baymax.front.config.security.sau.SAUUtils;
import com.paypay.baymax.front.controller.VueEndPoints;
import com.paypay.baymax.front.service.BitacoraService;
import com.paypay.baymax.front.service.CommonMethodsService;
import com.paypay.baymax.front.service.security.SAUService;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true, jsr250Enabled = true, securedEnabled = true)
public class MultiHttpSecurityConfig {

	@Configuration
	@Order(1)
	public class ApiSecurityAdapter extends WebSecurityConfigurerAdapter {

		@Autowired
		private JWTSettings jwtSettings;

		@Autowired
		private JWTSettingsResetPassword jwtSettingsRPwd;

		@Autowired
		private UserDetailsService userDetailsService;

		@Autowired
		private SAUService sauService;

		@Autowired
		private BitacoraService bitacoraService;

		@Autowired
		private CommonMethodsService<UsersDTO, DataTableRequest<Object>> userService;

		@Autowired
		private Gson gson;

		@Value("${server.servlet.session.cookie.name}")
		private String COOKIE_NAME;

		@Autowired
		public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordencoder());
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			TokenAuthenticationService.jwtTokensUtils = new JWTTokenUtils(jwtSettings, jwtSettingsRPwd);
			TokenAuthenticationService.sauUtils = new SAUUtils(sauService, gson);
			TokenAuthenticationService.userDetailsService = userDetailsService;

			http.cors().and().csrf().disable().authorizeRequests()
					/** Inicio - Interceptores de URI **/

					// API login & logout - backend login
					.antMatchers(HttpMethod.GET, "/", EndPoints.LOGOUT).permitAll()

					// Políticas de seguridad - Políticas de sesión
					.antMatchers(HttpMethod.POST, EndPoints.LOGIN).permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.CHANGE_PASSWORD).permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.REQUEST_RESET_PASSWORD).permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.API_RESET_PASSWORD).permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.RESET_PASSWORD).permitAll()
					.antMatchers(HttpMethod.GET, "/resetSessions", "/resetSessions/**").permitAll()

					// Vue login & logout - frontend login
					.antMatchers(HttpMethod.GET, "/", "/login", "/error", "/logout", "/img", "/css", "/js", "/fonts",
							"/**.js", "/**.html", "/**.json")
					.permitAll()

					// Políticas de seguridad - Políticas de sesión Vuejs
					.antMatchers(HttpMethod.GET, "/error", VueEndPoints.AUTH, VueEndPoints.HOME,
							VueEndPoints.HOME + "/**", VueEndPoints.EXPIRED_PWD, VueEndPoints.RESET_PWD,
							VueEndPoints.FORGET_PWD, VueEndPoints.SESSION_EXPIRED_MSG, VueEndPoints.AUTH_EXPIRED_PWD,
							VueEndPoints.AUTH_RESET_PWD, VueEndPoints.AUTH_RESET_PWD + "/**",
							VueEndPoints.AUTH_RESET_PWD + "/**/**", VueEndPoints.AUTH_FORGET_PWD,
							VueEndPoints.AUTH_SESSION_EXPIRED_MSG)
					.permitAll()
					//------Start main ------//
					.antMatchers(HttpMethod.GET, EndPoints.COMBOS).permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.COMBOS + "/**").permitAll()
					
					.antMatchers(HttpMethod.GET, EndPoints.EMPLOYEES).permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.EMPLOYEES + "/**").permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.EMPLOYEES).permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.EMPLOYEES + "/**").permitAll()
					.antMatchers(HttpMethod.PUT, EndPoints.EMPLOYEES).permitAll()
					.antMatchers(HttpMethod.PUT, EndPoints.EMPLOYEES + "/**").permitAll()
					.antMatchers(HttpMethod.DELETE, EndPoints.EMPLOYEES).permitAll()
					.antMatchers(HttpMethod.DELETE, EndPoints.EMPLOYEES + "/**").permitAll()
					
					.antMatchers(HttpMethod.GET, EndPoints.REVIEWS).permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.REVIEWS + "/**").permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.REVIEWS).permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.REVIEWS + "/**").permitAll()
					.antMatchers(HttpMethod.PUT, EndPoints.REVIEWS).permitAll()
					.antMatchers(HttpMethod.PUT, EndPoints.REVIEWS + "/**").permitAll()
					.antMatchers(HttpMethod.DELETE, EndPoints.REVIEWS).permitAll()
					.antMatchers(HttpMethod.DELETE, EndPoints.REVIEWS + "/**").permitAll()
					
					.antMatchers(HttpMethod.GET, EndPoints.FEEDBACKS).permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.FEEDBACKS + "/**").permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.FEEDBACKS).permitAll()
					.antMatchers(HttpMethod.POST, EndPoints.FEEDBACKS + "/**").permitAll()
					.antMatchers(HttpMethod.PUT, EndPoints.FEEDBACKS).permitAll()
					.antMatchers(HttpMethod.PUT, EndPoints.FEEDBACKS + "/**").permitAll()
					.antMatchers(HttpMethod.DELETE, EndPoints.FEEDBACKS).permitAll()
					.antMatchers(HttpMethod.DELETE, EndPoints.FEEDBACKS + "/**").permitAll()
					
					//------End main------//
					// Resources
					.antMatchers(HttpMethod.GET, "public/static/**/**", "public/**/**").permitAll()
					.antMatchers(HttpMethod.GET, "/public/static/**/**", "/public/**/**").permitAll()
					.antMatchers(HttpMethod.GET, "/static/**", "static/**", "static/**/**", "/static/**/**").permitAll()
					.antMatchers(HttpMethod.GET, "/fonts/**").permitAll().antMatchers(HttpMethod.GET, "/img/**")
					.permitAll().antMatchers(HttpMethod.GET, "/css/**").permitAll()
					.antMatchers(HttpMethod.GET, "/js/**").permitAll().antMatchers(HttpMethod.GET, "/toastr.js.map")
					.permitAll().antMatchers(HttpMethod.GET, "/favicon.ico").permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.IMAGE_RESOURCES).permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.IMAGE_RESOURCES + "/**").permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.IMAGE_RESOURCES + "/**/**").permitAll()
					.antMatchers(HttpMethod.GET, EndPoints.IMAGE_RESOURCES + "/**/**/**").permitAll()

					// Combos
					//.antMatchers(EndPoints.COMBOS, EndPoints.COMBOS + "/**")
					//.hasRole(CustomGrantedAuthorities.ROLE_ACCESO.getShortAuthority())

					// Catalogos Seguridad
					.antMatchers(EndPoints.GROUPS, EndPoints.GROUPS + "/**", EndPoints.USERS, EndPoints.USERS + "/**",
							EndPoints.GROUP_MEMBERS, EndPoints.GROUP_MEMBERS + "/**", EndPoints.GROUP_AUTHORITIES,
							EndPoints.GROUP_AUTHORITIES + "/**")
					.hasRole(CustomGrantedAuthorities.ROLE_ACCESO_SEGURIDAD.getShortAuthority())
					// Elimincación de perfiles
					.antMatchers(HttpMethod.DELETE, EndPoints.GROUPS)
					.hasRole(CustomGrantedAuthorities.ROLE_ACCESO_ELIMINAR_PERFILES.getShortAuthority())

					// Perfil (Modificación de foto de perfil por el usuario)
					.antMatchers(EndPoints.PERFIL_USUARIO, EndPoints.PERFIL_USUARIO + "/**")
					.hasRole(CustomGrantedAuthorities.ROLE_ACCESO.getShortAuthority())

					// Catalogos Generales
					.antMatchers(EndPoints.PLANTILLA_CORREO, EndPoints.PLANTILLA_CORREO + "/**")
					.hasRole(CustomGrantedAuthorities.ROLE_ACCESO_PLANTILLA_CORREO.getShortAuthority())

					// Catalogos Operativos
					.antMatchers(EndPoints.PARAMETROS, EndPoints.PARAMETROS + "/**")
					.hasAnyRole(CustomGrantedAuthorities.ROLE_ACCESO_CATALOGOS.getShortAuthority(),
							CustomGrantedAuthorities.ROLE_ACCESO.getShortAuthority())

					.anyRequest().authenticated()
					/** Fin - Interceptores de URI **/
					.and().exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHanderImpl()).and()
					.addFilterBefore(concurrentSessionFilter(), UsernamePasswordAuthenticationFilter.class)
					.addFilterBefore(new JWTLoginFilter(EndPoints.LOGIN, authenticationManager(),
							sessionAuthenticationStrategy(), TokenAuthenticationService.sauUtils, userDetailsService),
							UsernamePasswordAuthenticationFilter.class)
					.addFilterBefore(new JWTAuthenticationFilter(jwtSettings, jwtSettingsRPwd),
							UsernamePasswordAuthenticationFilter.class)
			// .addFilter(concurrentSessionFilter())
			;

			http.logout().deleteCookies(COOKIE_NAME).invalidateHttpSession(true).clearAuthentication(true)
					.logoutUrl(EndPoints.LOGOUT).logoutSuccessHandler(new CustomLogoutSuccessHandler(bitacoraService))
					.deleteCookies(COOKIE_NAME);

		}

		@Bean(name = "passwordEncoder")
		public PasswordEncoder passwordencoder() {
			return new BCryptPasswordEncoder();
		}

		@Bean
		public SessionRegistry sessionRegistry() {
			return new SessionRegistryImpl();
		}

		@Bean
		public HttpSessionEventPublisher httpSessionEventPublisher() {
			return new HttpSessionEventPublisher();
		}

		@Bean
		public AccessDeniedHandler accessDeniedHandler() {
			return new CustomAccessDeniedHanderImpl();
		}

		@Bean
		public CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy() {
			ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(
					sessionRegistry());

			concurrentSessionControlAuthenticationStrategy.setMaximumSessions(jwtSettings.getMaxSessionByUser());
			concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(true);

			SessionFixationProtectionStrategy sessionFixationProtectionStrategy = new SessionFixationProtectionStrategy();

			RegisterSessionAuthenticationStrategy registerSessionStrategy = new RegisterSessionAuthenticationStrategy(
					sessionRegistry());

			CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy = new CompositeSessionAuthenticationStrategy(
					Arrays.asList(concurrentSessionControlAuthenticationStrategy, sessionFixationProtectionStrategy,
							registerSessionStrategy));

			return sessionAuthenticationStrategy;
		}

		@Bean
		public ConcurrentSessionFilter concurrentSessionFilter() {
			return new CustomConcurrentSessionFilter(sessionRegistry(), "/logout");
		}

	}

}
