package db.query;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class QueryParser {

	public String queryType(String query) {

		Pattern typePattern = Pattern.compile("(select|insert)+\\s+.*?", Pattern.CASE_INSENSITIVE);
		Matcher typeMatcher = typePattern.matcher(query);

		if (!typeMatcher.matches()) {
			System.out.println("not good type request");
			return null;
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
		default: {
			System.out.println("other type");
		}
		}

		return null;
	}

	public SelectQuery selectParser(String query) {

		Pattern selectPattern = Pattern.compile("select\\s*+(.*?)\\s*+from\\s*+(.*?)(?:\\s*+where\\s*+(.*?))?;",
				Pattern.CASE_INSENSITIVE);
		Matcher selectMatcher = selectPattern.matcher(query);

		if (!selectMatcher.matches()) {
			System.out.println("bad select request");
			return null;
		}

		if (selectMatcher.group(1).isEmpty()) {
			System.out.println("no field");
			return null;
		}

		String[] fields = selectMatcher.group(1).toString().split("\\s*,\\s*");

		if (selectMatcher.group(2).isEmpty()) {
			System.out.println("no table");
			return null;
		}

		String table = selectMatcher.group(2).toString();

		String condition = null;
		if (!selectMatcher.group(3).isEmpty()) {
			condition = selectMatcher.group(3).toString();
		}

		SelectQuery mySelect = new SelectQuery();
		for (int i = 0; i < fields.length; i++) {
			mySelect.addField(fields[i]);
		}
		mySelect.setTableName(table);
		mySelect.setConditions(condition);

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

		if (insertMatcher.group(1).isEmpty()) {
			System.out.println("no table");
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

		//TODO verifier que la valeur est un int32/int64
		
		Timestamp timestamp = null;
		String value = values[1];

		try {
			timestamp = Timestamp.valueOf(values[0]);
		} catch (Exception e) {
			System.out.println("bad timestamp");
			return null;
		}

		InsertQuery myInsert = new InsertQuery();
		myInsert.setTableName(table);

		myInsert.setDate(timestamp);
		myInsert.setValue(value);

		return myInsert;
	}

	public Condition conditionParser(String condition){
		
	}
	
}

