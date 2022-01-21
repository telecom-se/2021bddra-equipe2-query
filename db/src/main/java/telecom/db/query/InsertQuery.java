package telecom.db.query;

import java.sql.Timestamp;

import lombok.Data;

//Classe définissant une query de type INSERT INTO
@Data
public class InsertQuery {
	
	private String tableName;
	private String dbName;
	private Timestamp date;
	private String value;
}
