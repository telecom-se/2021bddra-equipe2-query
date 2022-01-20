package telecom.db.query;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class SelectQuery {

	private Collection<String> fields;
	private String tableName;
	private String dbName;

	private Condition condition;
	private AndCondition andConditions;
	private OrCondition orConditions;
	
	public SelectQuery() {
		this.fields = new ArrayList<String>();
	}

	public void addField(String field) {
		this.fields.add(field);
	}
}
