package telecom.db.query;

import lombok.Data;

//Classe définissant une condition avec un or
@Data
public class OrCondition {
	private Condition condition1;
	private Condition condition2;
	
	public OrCondition(Condition condition1,Condition condition2) {
		this.condition1=condition1;
		this.condition2=condition2;
	}
}
