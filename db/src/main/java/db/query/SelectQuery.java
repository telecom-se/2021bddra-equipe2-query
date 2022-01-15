package db.query;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class SelectQuery {

	private Collection<String> fields;
	private String tableName;
	private String conditions;
	 
	public SelectQuery(Collection<String> fields,String tablename,String conditions) {
		this.fields=fields;
		this.tableName=tablename;
		this.conditions=conditions;
	}
	
	public SelectQuery() {
		this.fields = new ArrayList<String>();
	}
	
	public void addField(String field) {
		this.fields.add(field);
	}
}
