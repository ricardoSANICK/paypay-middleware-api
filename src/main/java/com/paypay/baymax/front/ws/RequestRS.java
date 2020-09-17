package com.paypay.baymax.front.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

public class RequestRS {

	private final static Logger log = LoggerFactory.getLogger(RequestRS.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${com.paypay.baymax.core.uri}")
	private String BASE_URI;

	public RequestRS() {
	}

	public <T> T GETRequest(Class<T> responseType, String path) throws Exception {
		log.debug("Consumo RestTemplate GET [" + path + "]");
		return restTemplate.getForObject(BASE_URI + "/" + path, responseType);
	}

	public <T> T POSTRequest(Object requestEntity, Class<T> responseType, String path) throws Exception {
		log.debug("Consumo RestTemplate POST [" + path + "]");
		return restTemplate.postForObject(BASE_URI + "/" + path, requestEntity, responseType);
	}

	@Async
	public <T> void POSTRequestAsync(Object requestEntity, Class<T> responseType, String path) throws Exception {
		log.debug("Consumo RestTemplate POST [" + path + "]");
		restTemplate.postForObject(BASE_URI + "/" + path, requestEntity, responseType);
	}

	public void PUTRequest(Object requestEntity, String path) throws Exception {
		log.debug("Consumo RestTemplate PUT [" + path + "]");
		restTemplate.put(BASE_URI + "/" + path, requestEntity);
	}

	public <T> T PATCHRequest(Object requestEntity, Class<T> responseType, String path) throws Exception {
		log.debug("Consumo RestTemplate PATCH [" + path + "]");
		return restTemplate.patchForObject(BASE_URI + "/" + path, requestEntity, responseType);
	}

	public void DELETERequest(String path) {
		log.debug("Consumo RestTemplate DELETE [" + path + "]");
		restTemplate.delete(BASE_URI + "/" + path);
	}

}