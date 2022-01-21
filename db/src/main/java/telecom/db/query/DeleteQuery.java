package telecom.db.query;

import lombok.Data;

//Classe définissant une query de type DELETE
@Data
public class DeleteQuery {
	private String tableName;
	private String conditions;
	 
	public DeleteQuery(String tablename,String conditions) {
		this.tableName=tablename;
		this.conditions=conditions;
	}
}
