package db.query;

import java.util.Collection;

import lombok.Data;

@Data
public class AndCondition {
	private Condition condition1;
	private Condition condition2;
	
	public AndCondition(Condition condition1,Condition condition2) {
		this.condition1=condition1;
		this.condition2=condition2;
	}

}
