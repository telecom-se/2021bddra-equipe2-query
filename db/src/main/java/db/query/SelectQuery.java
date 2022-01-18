package db.query;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class SelectQuery {

	private Collection<String> fields;
	private String tableName;
	private Condition condition;
	private AndCondition andConditions;
	private OrCondition orConditions;

	public SelectQuery() {
		this.fields = new ArrayList<String>();
	}

	public void addField(String field) {
		this.fields.add(field);
	}

	public SelectQuery(Collection<String> fields, String tableName, Condition condition, AndCondition andConditions,
			OrCondition orConditions) {
		super();
		this.fields = fields;
		this.tableName = tableName;
		this.condition = condition;
		this.orConditions = orConditions;
		this.andConditions = andConditions;
	}
}
