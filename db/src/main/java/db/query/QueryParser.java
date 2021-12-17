package db.query;

import java.util.Collection;
import java.util.*;
import java.io.*;
import lombok.Data;

@Data
public class QueryParser {
	
	private Query query;
	private Collection<String> keywords;
	
	private void initKeywords() {
	      keywords = Arrays.asList("select", "from", "where", "and",
	                               "insert", "into", "values", "delete", "update", "set", 
	                               "create", "table", "int", "varchar", "view", "as", "index", "on");
	   }
	
}
