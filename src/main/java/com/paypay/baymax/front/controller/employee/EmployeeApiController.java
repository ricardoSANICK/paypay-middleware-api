package com.paypay.baymax.front.controller.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.api.EmployeeApi;
import com.paypay.baymax.commons.type.OperEmployeeAllType;
import com.paypay.baymax.commons.type.OperEmployeeListAllType;
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
public class EmployeeApiController implements EmployeeApi {

    private static final Logger log = LoggerFactory.getLogger(EmployeeApiController.class);
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    private static RestTemplate restTemplate = new RestTemplate();
    
    @Value("${com.paypay.baymax.core.uri}")
    private String uri;
    private String COREENDPOINT = "/api/v1/paypay/core/employee";

    @org.springframework.beans.factory.annotation.Autowired
    public EmployeeApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

	@Override
	public ResponseEntity<Void> disable(
			@ApiParam(value = "Employee ID",required=true) @PathVariable("id") Long id) {
        restTemplate.delete(uri + COREENDPOINT + "/" + id);
        return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<OperEmployeeListAllType> getAll() {
		HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity <String> ("parameters", headers);
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(uri + COREENDPOINT);
        
        ResponseEntity<OperEmployeeListAllType> result = restTemplate
        		.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, OperEmployeeListAllType.class);
        return ResponseEntity.ok().body(result.getBody());
	}

	@Override
	public ResponseEntity<OperEmployeeAllType> getEmployeeById(
			@ApiParam(value = "Employee ID",required=true) @PathVariable("id") Long id) {
		
		Map <String, String> params = new HashMap < String, String > ();
        params.put("id", id.toString());
        OperEmployeeAllType result = restTemplate.getForObject(uri + COREENDPOINT + "/{id}", OperEmployeeAllType.class, params);
        return ResponseEntity.ok().body(result);
	}

	@Override
	public ResponseEntity<OperEmployeeAllType> save(
			@ApiParam(value = "Insert a employee object." ,required=true )  
			@Valid @RequestBody OperEmployeeAllType body) {
		OperEmployeeAllType result = restTemplate.postForObject(uri + COREENDPOINT, body, OperEmployeeAllType.class);
    	return ResponseEntity.ok().body(result);
	}

	@Override
	public ResponseEntity<OperEmployeeAllType> update(
			@ApiParam(value = "Update a employee object." ,required=true )  
			@Valid @RequestBody OperEmployeeAllType body) {
		
		Map <String, String> params = new HashMap < String, String > ();
    	restTemplate.put(uri + COREENDPOINT, body, params);
        return new ResponseEntity<OperEmployeeAllType>(HttpStatus.OK);
	}

}
