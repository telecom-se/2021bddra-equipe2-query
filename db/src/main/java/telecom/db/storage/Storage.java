package telecom.db.storage;

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
import java.util.List;
import java.sql.Timestamp;

import telecom.db.error.StorageException;
import telecom.db.query.*;
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
		
		checkForPaths(query.getDbName(), query.getTableName());
		
		// For delta delta compression we need to read the files and get the two firsts rows, if exist.
		SelectValues selectvalues = new SelectValues();
	    int i = 0;
		try (BufferedReader br = new BufferedReader(new FileReader("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt"))) {
		    String line;
		    String [] splittedLine;

		    // We only want to get the first 3 rows.
		    while ((line = br.readLine()) != null && i != 3) {
		    	splittedLine = line.split(",");
		    	selectvalues.getTimestamps().add(splittedLine[0]);
		    	selectvalues.getValues().add(splittedLine[1]);
		    	i=i+1;
		    }
		}
		
        Path file2 = Paths.get("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt");
        List<String> lines = new ArrayList<String>(); 

        if (i == 0) {
            // File is empty so we need to add timestamp and value first :
        	lines.add("timestamp,value");
        	lines.add(String.valueOf(query.getDate().getTime()) + "," + query.getValue());
        }
        else if (i == 2){
        	// Means we have only one data row 
        	lines.add(String.valueOf(query.getDate().getTime() - Long.parseLong(selectvalues.getTimestamps().get(1)) ) + "," + query.getValue());
        }
        
        else if (i==3) {
        	// Now we have at least 2 datas rows so we can remove the 2 first values to our timestamp ! youhou
        	lines.add((query.getDate().getTime() - Long.parseLong(selectvalues.getTimestamps().get(1)) - Long.parseLong(selectvalues.getTimestamps().get(2))) + "," + query.getValue());
        }
        
        Files.write(file2, lines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);        
	}

	public Object storeSelectQuery(SelectQuery query) throws StorageException, FileNotFoundException, IOException{

		checkForPaths(query.getDbName(), query.getTableName());
		// Good way to read datas from a txt, line by line. check : https://stackoverflow.com/questions/5868369/how-can-i-read-a-large-text-file-line-by-line-using-java
		SelectValues selectvalues = new SelectValues();
		String col1 = "", col2 = "";
		try (BufferedReader br = new BufferedReader(new FileReader("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt"))) {
		    String line;
		    String [] splittedLine;
		    Timestamp l_Timestamp;
		    Long firstDataRow = (long) 0, secondDataRow = (long) 0, dataRow = (long) 0 ;
		    int i = 0;
		    while ((line = br.readLine()) != null) {
		    	splittedLine = line.split(",");
	    		// Here we need to process all Timestamps since they are delta delta compressed and then convert them from Long to Timestamps
		    	if (i>2) {
		    		// Here we have already at least 2 datas rows. We need to add the first data row and the second
		    		dataRow = Long.parseLong(splittedLine[0]) + firstDataRow + secondDataRow;
		    		l_Timestamp = new Timestamp(dataRow);
			    	selectvalues.getTimestamps().add(l_Timestamp.toString());
			    	selectvalues.getValues().add(splittedLine[1]);
		    	}
		    	else if (i==1){
		    		// first data row :
		    		firstDataRow = Long.parseLong(splittedLine[0]);
		    		l_Timestamp = new Timestamp(firstDataRow);
			    	selectvalues.getTimestamps().add(l_Timestamp.toString());
			    	selectvalues.getValues().add(splittedLine[1]);
		    	}
		    	else if (i==2) {
		    		// Second data row. Means we have to add the first data row :
		    		secondDataRow = Long.parseLong(splittedLine[0]);
		    		l_Timestamp = new Timestamp(secondDataRow + firstDataRow);
			    	selectvalues.getTimestamps().add(l_Timestamp.toString());
			    	selectvalues.getValues().add(splittedLine[1]);
		    	}
		    	else if (i==0){
		    		// The first line is containing the columns name
		    		col1 = splittedLine[0];
		    		col2 = splittedLine[1];
		    	}
	    		i=i+1;
		    }
		}
		
		// Avoid to read empty file
		if (col1.equals("") || col2.equals("")) {
			System.out.println("Selecting datas from an empty file. Returning 'File is empty'");
			return "File is empty";
		}

		// Condition checker

		if (query.getCondition() != null)
		{
			System.out.println("Only one condition detected");
			List<String> newTimes = new ArrayList<String>();
			List<String> newValues = new ArrayList<String>();
			doCalculate(selectvalues, query.getCondition(), newTimes, newValues);
	    	selectvalues.setTimestamps(newTimes);
	    	selectvalues.setValues(newValues);
		}
		
		else if (query.getAndConditions() != null)
		{
			// L'idée ici c'est de d'abord traiter la condition 1 puis on update les tableaux qui stock le résultat
			//  ensuite on traite la conditon 2 sur le tableau précédemment rempli par les valeurs qui respectent la condition1. 
			// en sortie on aura bien un tableau qui stock les valeurs qui respectent la condition 1 ET 2.
			// Ca marche bien puisque c'est une condition ET donc il faut que les 2 soient vraies.
			
			System.out.println("And condition detected");
			List<String> newTimes = new ArrayList<String>();
			List<String> newValues = new ArrayList<String>();
			
			doCalculate(selectvalues, query.getAndConditions().getCondition1(), newTimes, newValues);
			
			// Let's update our Timestamps and Values with the new values we get from the first condition.
			selectvalues.getTimestamps().clear();
			selectvalues.getValues().clear();
			
	    	selectvalues.getTimestamps().addAll(newTimes); // !! don't use setTimestamps(newTimes) as the next line newTimes.clear() will also clear Timestamps ... 
	    	selectvalues.getValues().addAll(newValues);

	    	newTimes.clear();
	    	newValues.clear();
	    	
	    	// Now we gonna do the second condition with the new values
	    	doCalculate(selectvalues, query.getAndConditions().getCondition2(), newTimes, newValues);
	    	
	    	//update values
			selectvalues.setTimestamps(newTimes);
			selectvalues.setValues(newValues);
		}
		
		else if (query.getOrConditions() != null)
		{
			// Alors la la galère c'est que le OR peut prendre si au moins une condition est valide.
			// Le truc c'est que si les deux conditions sont vraies, on va add deux fois la même valeurs ........
			
			System.out.println("OR condition detected");
			List<String> newTimes = new ArrayList<String>();
			List<String> newValues = new ArrayList<String>();
			List<Integer> indexToRemove = new ArrayList<Integer>();
			
			doCalculateOr(selectvalues, query.getOrConditions().getCondition1(), newTimes, newValues, indexToRemove);
			
			for(int j = 0; j < indexToRemove.size()-1 ; j++){
				selectvalues.getTimestamps().remove(indexToRemove.get(j));
				selectvalues.getValues().remove(indexToRemove.get(j));
			}
	    	
			doCalculate(selectvalues, query.getOrConditions().getCondition2(), newTimes, newValues);
			
			selectvalues.setTimestamps(newTimes);
			selectvalues.setValues(newValues);
		}
		
		
		// Returning the right element according to what the user has selected (eg time or temperature)
		String firstElement = query.getFields().iterator().next();
		//System.out.println(firstElement);
        if(firstElement.equals("*") || query.getFields().size() == 2) {
        	return selectvalues;
        }

        else if (firstElement.equals("time"))
        {
        	return selectvalues.getTimestamps();
        }

        else if (firstElement.equals("temperature"))
        {
        	return selectvalues.getValues();
        }
        
        // means it failed
        return null;

	}
	
	public void storeCreateSeriesQuery(CreateQuery query) throws StorageException{
		// Create a TXT file at ./storeDB/dbName/seriesName.txt
		checkForPaths(query.getDbName());
		try {
			File myObj = new File("./src/main/java/db/storeDB/" + query.getDbName()  + "/" + query.getTableName() + ".txt");
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
			} 
			else {
				System.out.println("File already exists.");
				throw new StorageException("table already exist.");
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
			throw new StorageException("Database already exist.");
		}
	}
	
	public List<Integer> selectCondEqual(SelectValues selectvalues, Condition cond, List<String> newTimes, List<String> newValues){
	int size = selectvalues.getTimestamps().size();
	List<Integer> index=new ArrayList<Integer>();
	for(int j = 0; j < size ; j++){
			if(cond.getField1().equals("time")) {
				
				if (selectvalues.getTimestamps().get(j).equals(cond.getField2())) {
   					newTimes.add(selectvalues.getTimestamps().get(j));
   					newValues.add(selectvalues.getValues().get(j));
   					index.add(j);
				}
			}
			
			else if(cond.getField1().equals("temperature")) {
   				if (selectvalues.getValues().get(j).equals(cond.getField2())) {
   					newTimes.add(selectvalues.getTimestamps().get(j));
   					newValues.add(selectvalues.getValues().get(j));
   					index.add(j);
   				}
	   		}
		}
	return index;
	}
	
	public List<Integer> selectCondDiff(SelectValues selectvalues, Condition cond, List<String> newTimes, List<String> newValues){
	int size = selectvalues.getTimestamps().size();
	List<Integer> index=new ArrayList<Integer>();
	for(int j = 0; j < size ; j++){
			if(cond.getField1().equals("time")) {
				
				if (!selectvalues.getTimestamps().get(j).equals(cond.getField2())) {
   					newTimes.add(selectvalues.getTimestamps().get(j));
   					newValues.add(selectvalues.getValues().get(j));
   					index.add(j);
				}
			}
			
			else if(cond.getField1().equals("temperature")) {
   				if (!selectvalues.getValues().get(j).equals(cond.getField2())) {
   					newTimes.add(selectvalues.getTimestamps().get(j));
   					newValues.add(selectvalues.getValues().get(j));
   					index.add(j);
   				}
	   		}
		}
	return index;
	}
	
	public List<Integer> selectCondLow(SelectValues selectvalues, Condition cond, List<String> newTimes, List<String> newValues){
	int size = selectvalues.getTimestamps().size();
	List<Integer> index=new ArrayList<Integer>();
	for(int j = 0; j < size ; j++){
			if(cond.getField1().equals("time")) {
				
				if (Timestamp.valueOf(selectvalues.getTimestamps().get(j)).before(Timestamp.valueOf(cond.getField2()))) { // == Timestamp stocké < timestamp conditionnel
   					newTimes.add(selectvalues.getTimestamps().get(j));
   					newValues.add(selectvalues.getValues().get(j));
   					index.add(j);
				}
			}
			else if(cond.getField1().equals("temperature")) {
				if (Float.parseFloat(selectvalues.getValues().get(j)) < Float.parseFloat(cond.getField2())) {
   					newTimes.add(selectvalues.getTimestamps().get(j));
   					newValues.add(selectvalues.getValues().get(j));
   					index.add(j);
   				}
	   		}
		}
	return index;
	}
	
	public List<Integer> selectCondHigh(SelectValues selectvalues, Condition cond, List<String> newTimes, List<String> newValues){
	int size = selectvalues.getTimestamps().size();
	List<Integer> index=new ArrayList<Integer>();
	for(int j = 0; j < size ; j++){
		if(cond.getField1().equals("time")) {
			if (Timestamp.valueOf(selectvalues.getTimestamps().get(j)).after(Timestamp.valueOf(cond.getField2()))) {
				newTimes.add(selectvalues.getTimestamps().get(j));
				newValues.add(selectvalues.getValues().get(j));
				index.add(j);
			}
		}
		
		else if(cond.getField1().equals("temperature")) {
			if (Float.parseFloat(selectvalues.getValues().get(j)) > Float.parseFloat(cond.getField2())) {
				newTimes.add(selectvalues.getTimestamps().get(j));
				newValues.add(selectvalues.getValues().get(j));
				index.add(j);
			}
   		}
	}
	return index;
	}
	
	
	/* Checking if paths exists functions */
	public void checkForPaths(String dbName, String tableName) throws StorageException
	{
		Path dbPath = Paths.get("./src/main/java/db/storeDB/" + dbName);
		if (Files.notExists(dbPath)) {
			throw new StorageException("DB doesn't exist.");
		}
		
		Path tablePath = Paths.get("./src/main/java/db/storeDB/" + dbName  + "/" + tableName + ".txt");
		if (Files.notExists(tablePath)) {
			throw new StorageException("TABLE doesn't exist.");
		}
	}
	
	public void checkForPaths(String dbName) throws StorageException
	{
		Path dbPath = Paths.get("./src/main/java/db/storeDB/" + dbName);
		if (Files.notExists(dbPath)) {
			throw new StorageException("DB doesn't exist.");
		}
	}
	
	
	/* Functions for using the right function according to the operator. */
	public void doCalculate(SelectValues selectvalues, Condition condition, List<String> newTimes, List<String> newValues){
		switch(condition.getOperator()){				
   		case OPERATOR_EQUAL: 
   			selectCondEqual(selectvalues, condition, newTimes, newValues);
	       break;
       case OPERATOR_DIFFERENT:
    	   selectCondDiff(selectvalues, condition, newTimes, newValues);
           break;
       case OPERATOR_LOWER:
    	   selectCondLow(selectvalues, condition, newTimes, newValues);
           break;
       case OPERATOR_HIGHER:
    	   selectCondHigh(selectvalues, condition, newTimes, newValues);
           break;
       case OPERATOR_LOWER_OR_EQUAL:
    	   selectCondEqual(selectvalues, condition, newTimes, newValues);
    	   selectCondLow(selectvalues, condition, newTimes, newValues);
           break;
       case OPERATOR_HIGHER_OR_EQUAL:
    	   selectCondEqual(selectvalues, condition, newTimes, newValues);
    	   selectCondHigh(selectvalues, condition, newTimes, newValues);
           break; 
       default:
    	   System.out.println("Choix incorrect");
           break;
   }
	}
	
	public void doCalculateOr(SelectValues selectvalues, Condition condition, List<String> newTimes, List<String> newValues, List<Integer> indexToRemove){
		switch(condition.getOperator()){				
   		case OPERATOR_EQUAL: 
   			indexToRemove.addAll(selectCondEqual(selectvalues, condition, newTimes, newValues));
	       break;
       case OPERATOR_DIFFERENT:
    	   indexToRemove.addAll(selectCondDiff(selectvalues, condition, newTimes, newValues));
           break;
       case OPERATOR_LOWER:
    	   indexToRemove.addAll(selectCondLow(selectvalues, condition, newTimes, newValues));
           break;
       case OPERATOR_HIGHER:
    	   indexToRemove.addAll(selectCondHigh(selectvalues, condition, newTimes, newValues));
           break;
       case OPERATOR_LOWER_OR_EQUAL:
    	   indexToRemove.addAll(selectCondEqual(selectvalues, condition, newTimes, newValues));
    	   indexToRemove.addAll(selectCondLow(selectvalues, condition, newTimes, newValues));
           break;
       case OPERATOR_HIGHER_OR_EQUAL:
    	   indexToRemove.addAll(selectCondEqual(selectvalues, condition, newTimes, newValues));
    	   indexToRemove.addAll(selectCondHigh(selectvalues, condition, newTimes, newValues));
           break; 
       default:
    	   System.out.println("Choix incorrect");
           break;
   }
	}
}