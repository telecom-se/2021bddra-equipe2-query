package telecom.db.query;

import lombok.Data;

//Classe d�finissant une query de type CREATE DATABASE
@Data
public class CreateDatabaseQuery {
	
	private String dbName;
	
	public CreateDatabaseQuery(String dbname) {
		this.dbName=dbname;
	}
}
