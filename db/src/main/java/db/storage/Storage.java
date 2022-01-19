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
		
		else if(query instanceof CreateQuery)
		{
			storeCreateSeriesQuery((CreateQuery)query);
			return "Create Series query successful";
		}

		else if(query instanceof CreateDatabaseQuery)
		{
			storeCreateDatabaseQuery((CreateDatabaseQuery)query);
			return "Create Database query successful";
		}


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
        File file = new File("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt");
        
        List<String> lines = new ArrayList<String>(); 

        if (file.length() == 0) {
            // File is empty so we need to add timestamp and value first :
        	lines.add("timestamp,value");
        }
        lines.add(query.getDate().toString() + "," + query.getValue());

        Path file2 = Paths.get("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt");
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
		String col1 = "", col2 = "";
		try (BufferedReader br = new BufferedReader(new FileReader("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt"))) {
		    String line;
		    String [] splittedLine;
		    int i = 0;
		    while ((line = br.readLine()) != null) {
		    	splittedLine = line.split(",");
		    	if (i!=0){
			    	selectvalues.getTimestamps().add(splittedLine[0]);
			    	selectvalues.getValues().add(splittedLine[1]);
		    	}
		    	else{
		    		// The first line is containing the columns name
		    		col1 = splittedLine[0];
		    		col2 = splittedLine[1];
		    		i=i+1;
		    	}
		    }
		}
		
		// Avoid to read empty file
		if (col1.equals("") || col2.equals("")) {
			System.out.println("Selecting datas from an empty file. Returning 'File is empty'");
			return "File is empty";
		}

		//Check if conditions name really exist by comparing condition.name with col1 or col2 and conditon.name2 with col1 or col2 for exemple
		
		String firstElement = query.getFields().iterator().next();
		//System.out.println(firstElement);
        if(firstElement.equals("*") || query.getFields().size() == 2) {
        	return selectvalues;
        }

        else if (firstElement.equals("timestamp"))
        {
        	return selectvalues.getTimestamps();
        }

        else if (firstElement.equals("value"))
        {
        	return selectvalues.getValues();
        }
        
        // means it failed
        return null;

	}
	
	public void storeCreateSeriesQuery(CreateQuery query) throws StorageException{
		// Create a TXT file at ./storeDB/dbName/seriesName.txt
		try {
			File myObj = new File("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt");
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

	public void storeCreateDatabaseQuery(CreateDatabaseQuery query) throws StorageException{
		// Create a directory at ./storeDB/dbName
		if (!(new File("./src/main/java/db/storeDB/" + query.getDbName()).mkdirs())) {
			System.out.println("Can't create directory. Already exist ?");
		}
	}

	
}
