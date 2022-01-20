package telecom.db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import telecom.db.service.QueryService;

@RestController
public class QueryController {

	@Autowired
	QueryService queryService;
	
	@PostMapping("/query")
	public Object parseComputeQuery(@RequestBody String query) {
		
		return this.queryService.computeQuery(query);
		
	}
}