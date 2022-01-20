package telecom.db.query;

import java.util.Collection;

import lombok.Data;

@Data
public class DeleteQuery {
	private String tableName;
	private String conditions;
	 
	public DeleteQuery(String tablename,String conditions) {
		this.tableName=tablename;
		this.conditions=conditions;
	}
}
