package telecom.db.query;

import lombok.Data;

@Data
public class Condition {

	private String field1;
	private String field2;
	private Operator operator;

	public Condition(String field1, String field2, Operator operator) {
		this.field1 = field1;
		this.field2 = field2;
		this.operator = operator;
	}

	public Condition() {

	}
}
