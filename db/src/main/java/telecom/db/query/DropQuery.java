package telecom.db.query;

import lombok.Data;

@Data
public class DropQuery {
	
	private String tableName;
	
	public DropQuery(String tablename) {
		this.tableName=tablename;
	}
}
