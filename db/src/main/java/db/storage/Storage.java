package db.storage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.Timestamp;
import Error.StorageException;
import db.query.*;
import lombok.Data;

@Data
public class Storage {
	public Object readDatas(Object query) throws StorageException, IOException {
		if(query instanceof InsertQuery)
		{
			storeInsertQuery((InsertQuery)query);
			return "Insert query successful";
		}

		else if(query instanceof SelectQuery)
		{
			return storeSelectQuery((SelectQuery)query);
		}

		/*
		 * else if(query instanceof CreateDatabaseQuery)
		{
			storeCreateDatabaseQuery((CreateDatabaseQuery)query);
			return "Create Database query successful";
		}



		else if(query instanceof CreateSeriesQuery)
		{
			storeCreateSeriesQuery((CreateSeriesQuery)query);
			return "Create Series query successful";
		}
		*/

		else
		{
			return "Query type not found.";
		}
	}


	public void storeInsertQuery(InsertQuery query) throws StorageException, IOException{

		/*
		 *  String dbName;
			String tableName;
			Timestamp date;
			String value;
		*/

        File file = new File("./db/" + query.getDbName() + query.getTableName() + ".txt");
        List<String> lines = new ArrayList<String>(); 

        if (file.length() == 0) {
            // File is empty so we need to add timestamp and value first :
        	lines.add("timestamp,value");
        }
        lines.add(query.getDate().toString() + "," + query.getValue());

        Path file2 = Paths.get("./db/" + query.getDbName() + query.getTableName() + ".txt");
        Files.write(file2, lines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
	}

	public Object storeSelectQuery(SelectQuery query) throws StorageException, FileNotFoundException, IOException{
		/* 
			Collection<String> fields;
			String dbName;
			String tableName;
			String conditions; 
		*/ 

		// Good way to read datas from a txt, line by line. check : https://stackoverflow.com/questions/5868369/how-can-i-read-a-large-text-file-line-by-line-using-java
		SelectValues selectvalues = new SelectValues();
		try (BufferedReader br = new BufferedReader(new FileReader("./db/" + query.getDbName() + query.getTableName() + ".txt"))) {
		    String line;
		    String [] splittedLine;
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	splittedLine = line.split(",");
		    	selectvalues.getTimestamps().add(splittedLine[0]);
		    	selectvalues.getValues().add(splittedLine[1]);
		    }
		}


		String firstElement = query.getFields().iterator().next();
        if(firstElement == "*" || query.getFields().size() == 2) {
        	return selectvalues;
        }

        else if (firstElement == "timestamp")
        {
        	return selectvalues.getTimestamps();
        }

        else if (firstElement == "value")
        {
        	return selectvalues.getValues();
        }
        
        // means it failed
        return null;

	}
/*
	public void storeCreateDatabaseQuery(CreateDatabaseQuery query) throws StorageException{
		// Create a directory at ./db/dbName
		if (!(new File("./db/" + query.getDbName()).mkdirs())) {
			System.out.println("Can't create directory. Already exist ?");
			throw new QueryException("Can't create directory. Already exist ?");
		}
	}

	public void storeCreateSeriesQuery(CreateSeriesQuery query) throws StorageException{
		// Create a TXT file at ./db/dbName/seriesName.txt
		try {
			File myObj = new File("./db/" + query.getDbName() + query.getSeriesName() + ".txt");
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
			} 
			else {
				System.out.println("File already exists.");
			}
		} 
		catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
*/
}
