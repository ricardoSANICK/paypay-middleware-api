package com.paypay.baymax.front.controller.combo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypay.baymax.commons.api.ComboApi;
import com.paypay.baymax.commons.type.OperComboListAllType;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-09-04T16:06:01.444-05:00[America/Mexico_City]")
@Controller
public class ComboApiController implements ComboApi {

    private static final Logger log = LoggerFactory.getLogger(ComboApiController.class);
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    private static RestTemplate restTemplate = new RestTemplate();
    
    @Value("${com.paypay.baymax.core.uri}")
    private String uri;
    
    private String COREENDPOINT = "/api/v1/paypay/core/combo";

    @org.springframework.beans.factory.annotation.Autowired
    public ComboApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<OperComboListAllType> getCombo(
    		@ApiParam(value = "Combo",required=true) @PathVariable("option") String option) {
    	
    	Map <String, String> params = new HashMap < String, String > ();
        params.put("option", option);
        
        OperComboListAllType result = restTemplate.getForObject(uri + COREENDPOINT + "/{option}", OperComboListAllType.class, params);
        return ResponseEntity.ok().body(result);
    	
    }

	@Override
	public ResponseEntity<OperComboListAllType> getComboByParameter(
			@ApiParam(value = "Combo",required=true) @PathVariable("option") String option,
			@ApiParam(value = "Combo",required=true) @PathVariable("parameter") String parameter) {
		
		Map <String, String> params = new HashMap < String, String > ();
        params.put("option", option);
        params.put("parameter", parameter);
        
        OperComboListAllType result = restTemplate.getForObject(uri + COREENDPOINT + "/{option}" + "parameter" + "/{parameter}", OperComboListAllType.class, params);
        return ResponseEntity.ok().body(result);
	}
    

}
