package db.query;

import java.sql.Timestamp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Error.QueryException;
import lombok.Data;

@Data
public class QueryParser {

	public String queryType(String query) throws QueryException {

		Pattern typePattern = Pattern.compile("(select|insert|create table|create database|delete|drop)+\\s+.*?", Pattern.CASE_INSENSITIVE);
		Matcher typeMatcher = typePattern.matcher(query);

		if (!typeMatcher.matches()) {
			System.out.println("not good type request");
			throw new QueryException("not good type request");
		}

		String type = typeMatcher.group(1).toLowerCase().toString();

		switch (type) {

		case "select": {
			System.out.println("select");
			return "select";
		}
		case "insert": {
			System.out.println("insert");
			return "insert";
		}
		case "create table": {
			System.out.println("create table");
			return "create table";
		}
		case "create database": {
			System.out.println("create database");
			return "create database";
		}
		case "delete": {
			System.out.println("delete");
			return "delete";
		}
		case "drop": {
			System.out.println("drop");
			return "drop";
		}
		default: {
			System.out.println("other type");
			throw new QueryException("other type");
		}
		}
	}

	public SelectQuery selectParser(String query) throws QueryException {

		Pattern selectPattern = Pattern.compile("select\\s+(.*?)\\s*+from\\s*+(.*?)(?:\\s*+where\\s*+(.*?))?;",
				Pattern.CASE_INSENSITIVE);
		Matcher selectMatcher = selectPattern.matcher(query.toLowerCase());

		if (!selectMatcher.matches()) {
			System.out.println("bad select request");
			throw new QueryException("not good type request");
		}

		if (selectMatcher.group(1).isEmpty()) {
			System.out.println("no field");
			throw new QueryException("no field");
		}

		String[] fields = selectMatcher.group(1).toString().split("\\s*,\\s*");

		if (selectMatcher.group(2).isEmpty() || !selectMatcher.group(2).contains(".")) {
			System.out.println("no table / bad name. Need database.table");
			throw new QueryException("no table / bad name. Need database.table");
		}

		String table = selectMatcher.group(2).toString();

		SelectQuery mySelect = new SelectQuery();

		String conditions = null;

		if (selectMatcher.group(3) != null && !selectMatcher.group(3).trim().isEmpty()) {
			conditions = selectMatcher.group(3).toString();
			Object resultCondition = andOrParser(conditions);

			if (resultCondition instanceof AndCondition) {
				mySelect.setAndConditions((AndCondition)resultCondition);
			} else if (resultCondition instanceof OrCondition) {
				mySelect.setOrConditions((OrCondition)resultCondition);
			} else if (resultCondition instanceof Condition) {
				mySelect.setCondition((Condition)resultCondition);
			} else {
				return null;
			}
		}

		for (int i = 0; i < fields.length; i++) {
			mySelect.addField(fields[i]);
		}

		mySelect.setDbName(table.split("\\.")[0]);
		mySelect.setTableName(table.split("\\.")[1]);

		return mySelect;

	}

	public InsertQuery insertParser(String query) {

		Pattern insertPattern = Pattern.compile("insert\\s*+into\\s*+(.*?)\\s*+values\\s*+\\((.*?)\\);",
				Pattern.CASE_INSENSITIVE);
		Matcher insertMatcher = insertPattern.matcher(query);

		if (!insertMatcher.matches()) {
			System.out.println("bad insert request");
			return null;
		}

		if (insertMatcher.group(1).isEmpty() || !insertMatcher.group(1).contains(".")) {
			System.out.println("no table / bad name. Need database.table");
			return null;
		}

		String table = insertMatcher.group(1).toString();

		if (insertMatcher.group(2).isEmpty()) {
			System.out.println("no value to add");
			return null;
		}

		String[] values = insertMatcher.group(2).toString().split("\\s*,\\s*");

		if (values[0].isEmpty()) {
			System.out.println("no timestamp");
			return null;
		}
		if (values[1].isEmpty()) {
			System.out.println("no value");
			return null;
		}

		// TODO verifier que la valeur est un int32/int64

		Timestamp timestamp = null;
		String value = values[1];

		try {
			timestamp = Timestamp.valueOf(values[0]);
		} catch (Exception e) {
			System.out.println("bad timestamp");
			return null;
		}

		InsertQuery myInsert = new InsertQuery();
		myInsert.setDbName(table.split("\\.")[0]);
		myInsert.setTableName(table.split("\\.")[1]);

		myInsert.setDate(timestamp);
		myInsert.setValue(value);

		return myInsert;
	}
	
	public CreateQuery createParser(String query) {

		Pattern createPattern = Pattern.compile("create\\s*+table\\s*+(.*?);",
				Pattern.CASE_INSENSITIVE);
		Matcher createMatcher = createPattern.matcher(query.toLowerCase());
		if (!createMatcher.matches()) {
			System.out.println("bad create request");
			return null;
		}
		System.out.println(createMatcher.group(1));
		if (createMatcher.group(1).isEmpty() || !(createMatcher.group(1).contains("."))) {
			System.out.println("no field -- need database.table");
			return null;
		}

		CreateQuery myCreate = new CreateQuery(createMatcher.group(1));
		return myCreate;

	}
	
	public CreateDatabaseQuery createDatabaseParser(String query) {

		Pattern createPattern = Pattern.compile("create\\s*+database\\s*+(.*?);",
				Pattern.CASE_INSENSITIVE);
		Matcher createMatcher = createPattern.matcher(query.toLowerCase());
		if (!createMatcher.matches()) {
			System.out.println("bad create request");
			return null;
		}
		System.out.println(createMatcher.group(1));
		if (createMatcher.group(1).isEmpty()) {
			System.out.println("no database name ...");
			return null;
		}

		CreateDatabaseQuery myCreateDatabase = new CreateDatabaseQuery(createMatcher.group(1));
		return myCreateDatabase;

	}
	
	public DropQuery dropParser(String query) {

		Pattern dropPattern = Pattern.compile("drop\\s*+table\\s*+(.*?);",
				Pattern.CASE_INSENSITIVE);
		Matcher dropMatcher = dropPattern.matcher(query.toLowerCase());
		if (!dropMatcher.matches()) {
			System.out.println("bad drop request");
			return null;
		}

		if (dropMatcher.group(1).isEmpty()) {
			System.out.println("no field");
			return null;
		}

		DropQuery myDrop = new DropQuery(dropMatcher.group(1));
		return myDrop;

	}
	
	public DeleteQuery deleteParser(String query) {

		Pattern deletePattern = Pattern.compile("delete\\s*+from\\s*+(.*?)(?:\\s*+where\\s*+(.*?))?;",
				Pattern.CASE_INSENSITIVE);
		Matcher deleteMatcher = deletePattern.matcher(query);

		if (!deleteMatcher.matches()) {
			System.out.println("bad delete request");
			return null;
		}

		if (deleteMatcher.group(1).isEmpty()) {
			System.out.println("no field");
			return null;
		}


		if (deleteMatcher.group(2).isEmpty()) {
			System.out.println("no table");
			return null;
		}

		String table = deleteMatcher.group(1).toString();

		String condition = null;
		if (!deleteMatcher.group(2).isEmpty()) {
			condition = deleteMatcher.group(2).toString();
		}

		DeleteQuery myDelete = new DeleteQuery(deleteMatcher.group(1), deleteMatcher.group(2));

		return myDelete;

	}

	public Condition conditionParser(String condition) {
		
		//verification du bon nombre d'???l???ments
		String[] conditionElement = condition.toLowerCase().split("\\s*+(<|>|=|!=|<=|>=)+\\s*");
		
		if(conditionElement.length!=2) {
			System.out.println("bad condition request : " + condition);
			return null;
		}
		
		// trouver l'op???ration
		Operator operator = null;
		
		Pattern conditionPattern = Pattern.compile("(.*?)\\s*+(!=|<=|>=|<|>|=)\\s*+(.*?)", Pattern.CASE_INSENSITIVE);
		Matcher conditionMatcher = conditionPattern.matcher(condition);

		if (!conditionMatcher.matches()) {
			System.out.println("bad condition request : " + condition);
			return null;
		}
		
		if(conditionMatcher.group(2)==null) {
			System.out.println("bad operator");
			return null;
		}
		
		switch (conditionMatcher.group(2).toString()) {
		case "=": {
			operator = Operator.OPERATOR_EQUAL;
			break;
		}
		case "!=": {
			operator = Operator.OPERATOR_DIFFERENT;
			break;
		}
		case "<=": {
			operator = Operator.OPERATOR_LOWER_OR_EQUAL;
			break;
		}
		case ">=": {
			operator = Operator.OPERATOR_HIGHER_OR_EQUAL;
			break;
		}
		case "<": {
			operator = Operator.OPERATOR_LOWER;
			break;
		}
		case ">": {
			operator = Operator.OPERATOR_HIGHER;
			break;
		}
		}

		if (operator == null) {
			System.out.println("bad operator");
			return null;
		}
		
		// trouver les champs
		if (conditionElement[0].isEmpty()) {
			System.out.println("bad condition request : " + condition);
			return null;
		}

		String champ1 = conditionElement[0].toString();

		if (conditionElement[1].isEmpty()) {
			System.out.println("bad condition request : " + condition);
			return null;
		}
		
		if(champ1.toString().equals("time")) {
			try {
				Timestamp.valueOf(conditionElement[1]);
				
				Condition returnCondition = new Condition();
				returnCondition.setField1(champ1);
				returnCondition.setField2(conditionElement[1].toString());
				returnCondition.setOperator(operator);
				return returnCondition;
				
			} catch (Exception e) {
				System.out.println("bad timestamp");
				return null;
			}
		}else if(champ1.toString().equals("temperature")) {
			try {
				Float.parseFloat(conditionElement[1]);
				
				Condition returnCondition = new Condition();
				returnCondition.setField1(champ1);
				returnCondition.setField2(conditionElement[1].toString());
				returnCondition.setOperator(operator);
				return returnCondition;
				
			} catch (Exception e) {
				System.out.println("bad temperature");
				return null;
			}
		}else {
			return null;
		}

	}

	public Object andOrParser(String conditions) {

		// Verifier qu'il y a qu'un ou 0 "and" ou "or
		String[] andOrConditions = conditions.toLowerCase().split("\\s*+(and|or)+\\s*");

		if (andOrConditions.length > 2) {
			System.out.println("too many arguments");
			return null;
		}
		if (andOrConditions.length < 1) {
			System.out.println("bad arguments");
			return null;
		}
		
		// trouver si c'est "and" ou "or"
		String andOrCondition = null;
		if (conditions.toLowerCase().contains(" and ")) {
			andOrCondition = "and";
		}
		if (conditions.toLowerCase().contains(" or ")) {
			andOrCondition = "or";
		}

		// Si il y a pas de "and" ou "or" on creer seulement un condition
		if (andOrCondition == null) {
			Condition condition = conditionParser(andOrConditions[0].toString());
			if (condition != null) {
				return condition;
			} else {
				return null;
			}
		}

		//Sinon on enregistre un condition de type "and" ou "or"
		Condition condition1 = conditionParser(andOrConditions[0].toString());
		Condition condition2 = conditionParser(andOrConditions[1].toString());

		if (andOrCondition == "or" && condition1 != null && condition2 != null) {
			return new OrCondition(condition1, condition2);
		} else if (andOrCondition == "and" && condition1 != null && condition2 != null) {
			return new AndCondition(condition1, condition2);
		}

		return null;
	}

	
}
