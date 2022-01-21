package telecom.db.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import telecom.db.error.QueryException;
import telecom.db.error.StorageException;
import telecom.db.query.QueryParser;
import telecom.db.storage.Storage;

/**
 * Service utilisé par le controller 
 * 
 * Prend en argument la query en string 
 *
 * @return le résultat de la query
 */
@Service
public class QueryService {

	public Object computeQuery(String query) {
		
		QueryParser queryParser = new QueryParser();
		Storage storage = new Storage();
		
		try {
			Object parseQuery = queryParser.parseQuery(query);
			Object result = storage.readDatas(parseQuery);
			return result;
		} catch (QueryException e) {
			return e;
		} 
		catch (StorageException e) {
			return e;
		} catch (IOException e) {
			return e;
		}
	}
}
