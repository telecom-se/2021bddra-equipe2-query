package db.query;

import lombok.Data;

@Data
public class CreateQuery {
	
	private String tableName;
	
	public CreateQuery(String tablename) {
		this.tableName=tablename;
	}
}
