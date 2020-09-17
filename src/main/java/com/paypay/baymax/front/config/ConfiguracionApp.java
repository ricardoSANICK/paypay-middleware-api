package com.paypay.baymax.front.config;

import javax.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.nxn.sau.security.util.JWTEncripter;
import com.paypay.baymax.commons.util.GsonBuild;
import com.paypay.baymax.front.ws.RequestRS;

@Configuration
public class ConfiguracionApp {

	@Bean
	public ModelMapper getModelMapper() {
		return new ModelMapper();
	}
	
	

	@Bean("gsonCreate")
	@Primary
	public Gson gsonCreate() {
		return new GsonBuild().getGson();
	}

	@Bean("gsonCreateDate")
	public Gson gsonCreateDate() {
		return new GsonBuild().getGsonDate();
	}
	
	@Bean
	public RequestRS requestRS() {
		return new RequestRS();
	}
	
	@Bean
	public JWTEncripter jwtEncripter() {
		return new JWTEncripter();
	}
	

	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplateBuilder builder = new RestTemplateBuilder();
		return builder.build();
	}

	@PostConstruct
	public void init() {
		
	}

}
