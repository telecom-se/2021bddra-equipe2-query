package db.query;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class InsertQuery {
	
	private String tableName;
	private Timestamp date;
	private String value;
}
