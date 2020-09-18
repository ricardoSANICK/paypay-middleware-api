package com.paypay.baymax.front.controller.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.api.ReviewApi;
import com.paypay.baymax.commons.type.OperReviewAllType;
import com.paypay.baymax.commons.type.OperReviewListAllType;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-09-13T17:05:15.456-05:00[America/Mexico_City]")
@Controller
public class ReviewApiController implements ReviewApi {

    private static final Logger log = LoggerFactory.getLogger(ReviewApiController.class);
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    private static RestTemplate restTemplate = new RestTemplate();
    
    @Value("${com.paypay.baymax.core.uri}")
    private String uri;
    private String COREENDPOINT = "/api/v1/paypay/core/review";

    @org.springframework.beans.factory.annotation.Autowired
    public ReviewApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

	@Override
	public ResponseEntity<Void> disable(
			@ApiParam(value = "Review ID",required=true) @PathVariable("id") Long id) {
		restTemplate.delete(uri + COREENDPOINT + "/" + id);
        return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<OperReviewListAllType> getAll() {
		HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity <String> ("parameters", headers);
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(uri + COREENDPOINT);
        
        ResponseEntity<OperReviewListAllType> result = restTemplate
        		.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, OperReviewListAllType.class);
        return ResponseEntity.ok().body(result.getBody());
	}

	@Override
	public ResponseEntity<OperReviewAllType> getReviewById(
			@ApiParam(value = "Review ID",required=true) @PathVariable("id") Long id) {
		Map <String, String> params = new HashMap < String, String > ();
        params.put("id", id.toString());
        OperReviewAllType result = restTemplate.getForObject(uri + COREENDPOINT + "/{id}", OperReviewAllType.class, params);
        return ResponseEntity.ok().body(result);
	}

	@Override
	public ResponseEntity<OperReviewAllType> save(
			@ApiParam(value = "Insert a review object." ,required=true )  
			@Valid @RequestBody OperReviewAllType body) {
		OperReviewAllType result = restTemplate.postForObject(uri + COREENDPOINT, body, OperReviewAllType.class);
    	return ResponseEntity.ok().body(result);
	}

	@Override
	public ResponseEntity<OperReviewAllType> update(
			@ApiParam(value = "Update a review object." ,required=true )  
			@Valid @RequestBody OperReviewAllType body) {
		Map <String, String> params = new HashMap < String, String > ();
    	restTemplate.put(uri + COREENDPOINT, body, params);
        return new ResponseEntity<OperReviewAllType>(HttpStatus.OK);
	}
}
