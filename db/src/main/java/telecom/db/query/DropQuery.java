package telecom.db.query;

import lombok.Data;

//Classe définissant une query de type DROP TABLE
@Data
public class DropQuery {
	
	private String tableName;
	
	public DropQuery(String tablename) {
		this.tableName=tablename;
	}
}
