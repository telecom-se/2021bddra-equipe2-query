package db.query;

import java.util.Collection;
import java.util.*;
import java.io.*;

import lombok.Data;

@Data
public class QueryParser {

	private Query query;
	private Collection<String> keywords;
	
	public QueryParser(String s) throws IOException {

		StreamTokenizer tok = new StreamTokenizer(new StringReader(s));

		tok.lowerCaseMode(true);

		while (tok.nextToken() != StreamTokenizer.TT_EOF) {

			if (tok.ttype == StreamTokenizer.TT_WORD) {
				System.out.println(tok.sval);
			} else if (tok.ttype == Constants.STAR_CHARACTER) {
				System.out.println("*");
			} else if (tok.ttype == Constants.COMMA_CHARACTER) {
				System.out.println(tok.sval);
				System.out.println(",");
			} else if (tok.ttype == Constants.SEMICOLON_CHARACTER) {
				System.out.println(tok.sval);
				System.out.println(";");
			}

		}
	}
}
