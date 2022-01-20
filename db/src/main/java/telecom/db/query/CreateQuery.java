package telecom.db.query;

import lombok.Data;

@Data
public class CreateQuery {
	
	private String tableName;
	private String dbName;
	
	public CreateQuery(String tablename) {
		this.dbName=tablename.split("\\.")[0];
		this.tableName=tablename.split("\\.")[1];
	}
}
