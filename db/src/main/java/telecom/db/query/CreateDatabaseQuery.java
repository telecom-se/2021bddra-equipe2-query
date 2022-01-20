package telecom.db.query;

import lombok.Data;

@Data
public class CreateDatabaseQuery {
	
	private String dbName;
	
	public CreateDatabaseQuery(String dbname) {
		this.dbName=dbname;
	}
}
