package db.query;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class SelectQuery {

	private Collection<String> fields;
	private String tableName;
	private String dbName;
	private String conditions;
	 
	public SelectQuery(Collection<String> fields,String dbTableName,String conditions) {
		this.fields=fields;
		this.tableName= dbTableName.split(".")[1];
		this.conditions=conditions;
		this.dbName=dbTableName.split(".")[0];;
	}
	
	public SelectQuery() {
		this.fields = new ArrayList<String>();
	}
	
	public void addField(String field) {
		this.fields.add(field);
	}
}
