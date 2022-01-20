package telecom.db.query;

import java.sql.Timestamp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.Data;
import telecom.db.error.QueryException;

@Data
@Service
public class QueryParser {
	

	public Object parseQuery(String query) throws QueryException {

		Pattern typePattern = Pattern.compile("(select|insert|create table|create database|delete|drop)+\\s+.*?", Pattern.CASE_INSENSITIVE);
		Matcher typeMatcher = typePattern.matcher(query.toLowerCase());

		System.out.println(query);
		
		if (!typeMatcher.matches()) {
			System.out.println("not good type request");
			throw new QueryException("not good type request");
		}

		String type = typeMatcher.group(1).toLowerCase().toString();

		switch (type) {

		case "select": {
			System.out.println("select");
			return selectParser(query);
		}
		case "insert": {
			System.out.println("insert");
			return insertParser(query);
		}
		case "create table": {
			System.out.println("create table");
			return createParser(query);
		}
		case "create database": {
			System.out.println("create database");
			return createDatabaseParser(query);
		}
		case "delete": {
			System.out.println("delete");
			return deleteParser(query);
		}
		case "drop": {
			System.out.println("drop");
			return dropParser(query);
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
				System.out.println("ici");
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

	public InsertQuery insertParser(String query) throws QueryException {

		Pattern insertPattern = Pattern.compile("insert\\s*+into\\s*+(.*?)\\s*+values\\s*+\\((.*?)\\);",
				Pattern.CASE_INSENSITIVE);
		Matcher insertMatcher = insertPattern.matcher(query);

		if (!insertMatcher.matches()) {
			System.out.println("bad insert request");
			throw new QueryException("bad insert request");
		}

		if (insertMatcher.group(1).isEmpty() || !insertMatcher.group(1).contains(".")) {
			System.out.println("no table / bad name. Need database.table");
			throw new QueryException("no table / bad name. Need database.table");
		}

		String table = insertMatcher.group(1).toString();

		if (insertMatcher.group(2).isEmpty()) {
			System.out.println("no value to add");
			throw new QueryException("no value to add");
		}

		String[] values = insertMatcher.group(2).toString().split("\\s*,\\s*");

		if(values.length<=1) {
			throw new QueryException("bad insert");
		}
		
		if (values[0].isEmpty()) {
			System.out.println("no timestamp");
			throw new QueryException("no timestamp");
		}
		if (values[1].isEmpty()) {
			System.out.println("no value");
			throw new QueryException("no value");
		}

		Timestamp timestamp = null;
		String value = values[1];

		try {
			timestamp = Timestamp.valueOf(values[0]);
		} catch (Exception e) {
			System.out.println("bad timestamp");
			throw new QueryException("bad timestamp");
		}

		InsertQuery myInsert = new InsertQuery();
		myInsert.setDbName(table.split("\\.")[0]);
		myInsert.setTableName(table.split("\\.")[1]);

		myInsert.setDate(timestamp);
		myInsert.setValue(value);

		return myInsert;
	}
	
	public CreateQuery createParser(String query) throws QueryException {

		Pattern createPattern = Pattern.compile("create\\s*+table\\s*+(.*?);",
				Pattern.CASE_INSENSITIVE);
		Matcher createMatcher = createPattern.matcher(query.toLowerCase());
		if (!createMatcher.matches()) {
			System.out.println("bad create request");
			throw new QueryException("bad create request");
		}
		System.out.println(createMatcher.group(1));
		if (createMatcher.group(1).isEmpty() || !(createMatcher.group(1).contains("."))) {
			System.out.println("no field -- need database.table");
			throw new QueryException("no field -- need database.table");
		}

		CreateQuery myCreate = new CreateQuery(createMatcher.group(1));
		return myCreate;

	}
	
	public CreateDatabaseQuery createDatabaseParser(String query) throws QueryException {

		Pattern createPattern = Pattern.compile("create\\s*+database\\s*+(.*?);",
				Pattern.CASE_INSENSITIVE);
		Matcher createMatcher = createPattern.matcher(query.toLowerCase());
		if (!createMatcher.matches()) {
			System.out.println("bad create request");
			throw new QueryException("bad create request");
		}
		System.out.println(createMatcher.group(1));
		if (createMatcher.group(1).isEmpty()) {
			System.out.println("no database name ...");
			throw new QueryException("no database name ...");
		}

		CreateDatabaseQuery myCreateDatabase = new CreateDatabaseQuery(createMatcher.group(1));
		return myCreateDatabase;

	}
	
	public DropQuery dropParser(String query) throws QueryException {

		Pattern dropPattern = Pattern.compile("drop\\s*+table\\s*+(.*?);",
				Pattern.CASE_INSENSITIVE);
		Matcher dropMatcher = dropPattern.matcher(query.toLowerCase());
		if (!dropMatcher.matches()) {
			System.out.println("bad drop request");
			throw new QueryException("bad drop request");
		}

		if (dropMatcher.group(1).isEmpty()) {
			System.out.println("no field");
			throw new QueryException("bad drop request");
		}

		DropQuery myDrop = new DropQuery(dropMatcher.group(1));
		return myDrop;

	}
	
	public DeleteQuery deleteParser(String query) throws QueryException {

		Pattern deletePattern = Pattern.compile("delete\\s*+from\\s*+(.*?)(?:\\s*+where\\s*+(.*?))?;",
				Pattern.CASE_INSENSITIVE);
		Matcher deleteMatcher = deletePattern.matcher(query);

		if (!deleteMatcher.matches()) {
			System.out.println("bad delete request");
			throw new QueryException("bad delete request");
		}

		if (deleteMatcher.group(1).isEmpty()) {
			System.out.println("no field");
			throw new QueryException("no field");
		}


		if (deleteMatcher.group(2).isEmpty()) {
			System.out.println("no table");
			throw new QueryException("no table");
		}

		String table = deleteMatcher.group(1).toString();

		String condition = null;
		if (!deleteMatcher.group(2).isEmpty()) {
			condition = deleteMatcher.group(2).toString();
		}

		DeleteQuery myDelete = new DeleteQuery(deleteMatcher.group(1), deleteMatcher.group(2));

		return myDelete;

	}

	public Condition conditionParser(String condition) throws QueryException {
		
		//verification du bon nombre d'elements
		String[] conditionElement = condition.toLowerCase().split("\\s*+(<|>|=|!=|<=|>=)+\\s*");
		
		if(conditionElement.length!=2) {
			System.out.println("bad condition request : " + condition);
			throw new QueryException("no table");
		}
		
		// trouver l'op�ration
		Operator operator = null;
		
		Pattern conditionPattern = Pattern.compile("(.*?)\\s*+(!=|<=|>=|<|>|=)\\s*+(.*?)", Pattern.CASE_INSENSITIVE);
		Matcher conditionMatcher = conditionPattern.matcher(condition);

		if (!conditionMatcher.matches()) {
			System.out.println("bad condition request : " + condition);
			throw new QueryException("bad condition request : " + condition);
		}
		
		if(conditionMatcher.group(2)==null) {
			System.out.println("bad operator");
			throw new QueryException("bad operator");
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
			throw new QueryException("bad operator");
		}
		
		// trouver les champs
		if (conditionElement[0].isEmpty()) {
			System.out.println("bad condition request : " + condition);
			throw new QueryException("bad condition request : " + condition);
		}

		String champ1 = conditionElement[0].toString();

		if (conditionElement[1].isEmpty()) {
			System.out.println("bad condition request : " + condition);
			throw new QueryException("bad condition request : " + condition);
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
				throw new QueryException("bad timestamp");
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
				throw new QueryException("bad temperature");
			}
		}else {
			throw new QueryException("bad fields");
		}

	}

	public Object andOrParser(String conditions) throws QueryException {

		// Verifier qu'il y a qu'un ou 0 "and" ou "or
		String[] andOrConditions = conditions.toLowerCase().split("\\s*+(and|or)+\\s*");

		if (andOrConditions.length > 2) {
			System.out.println("too many arguments");
			throw new QueryException("too many arguments");
		}
		if (andOrConditions.length < 1) {
			System.out.println("bad arguments");
			throw new QueryException("bad arguments");
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
				throw new QueryException("bad condition");
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

		throw new QueryException("bad condition with " + andOrCondition);
	}

	
}