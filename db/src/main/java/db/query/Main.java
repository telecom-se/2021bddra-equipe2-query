package db.query;

import java.io.IOException;

import Error.QueryException;
import Error.StorageException;
import db.storage.*;

public class Main {

	public static void main(String[] args) throws QueryException, StorageException, IOException {

		QueryParser queryparser = new QueryParser();
		Storage S = new Storage();
/*
		String querySelect = "SELECT * FROM db1.table2 where date=3;";
		String querySelectFail = "SELECTinsert hy  ,  ur FROM table  where date=3;";

		String queryInsert = "INSERT INTO table1 VALUES (2012-12-13 13:56:12,32);";
		String queryInsert2 = "INSERT INTO table1 VALUES (null,32);";
		String queryInsertFail = "INSERT INTO  VALUES 12/12/12 13:56:12,32;";

		String queryCreate = "CREATE TABLE db1.table2;";
		String queryCreateFail = "CREATE hy  ,  ur FROM table  where date=3;";

		String queryDrop = "DROP TABLE db1.table2;";
		String queryDropFail = "DROP hy  ,  ur FROM table  where date=3;";

		String queryDelete = "DELETE FROM db1.table2 where date=3;";
		String queryDeleteFail = "DELETE hy  ,  ur FROM table  where date=3;";

		String condition = "time = 2016-12-03 12:59:36";

		String conditions = "time != 1223-12-03 12:59:36 and time <= 1223-12-03 12:59:59";
		String conditionsFail = "time < 1223-12-03 12:59:36 and times = 1223-12-03 12:59:59 ";

		String queryFail = "GRT INTO table1 VALUES (12/12/12,32)";


		System.out.println(queryparser.queryType(querySelect));
		System.out.println(queryparser.queryType(querySelectFail));
		System.out.println(queryparser.selectParser(querySelect));
		System.out.println(queryparser.insertParser(queryInsert));
		System.out.println(queryparser.conditionParser(condition));
		System.out.println(queryparser.andOrParser(conditions));
		System.out.println(queryparser.andOrParser(conditionsFail));
		System.out.println(queryparser.queryType(queryCreate));
		System.out.println(queryparser.createParser(queryCreateFail));
		System.out.println(queryparser.createParser(queryCreate));
		System.out.println(queryparser.queryType(queryDelete));
		System.out.println(queryparser.deleteParser(queryDeleteFail));
		System.out.println(queryparser.deleteParser(queryDelete));
		System.out.println(queryparser.queryType(queryDrop));
		System.out.println(queryparser.dropParser(queryDropFail));
		System.out.println(queryparser.dropParser(queryDrop));
*/

		/*

		*/
//EXEMPLE OF SEQUENCE : (create database --> create table --> insert --> select

		// Exemple of create database :
		String queryCreateDatabase = "CREATE DATABASE brichieeee;";
		System.out.println(queryparser.queryType(queryCreateDatabase));
		System.out.println(queryparser.createDatabaseParser(queryCreateDatabase));
		S.readDatas(queryparser.createDatabaseParser(queryCreateDatabase));
		
		//EXEMPLE of create table :
		String queryCreateTable = "CREATE TABLE brichieeee.table2;";
		System.out.println(queryparser.createParser(queryCreateTable));
		S.readDatas(queryparser.createParser(queryCreateTable));

		// Exemple of INSERT INTO :
		String queryInsertTest = "INSERT INTO brichieeee.table2 VALUES (2013-12-12 22:0:0,34);";
		System.out.println(queryparser.insertParser(queryInsertTest));	
		S.readDatas(queryparser.insertParser(queryInsertTest));

		// Exemple of SELECT : (where not implemented rn)
		String querySelectTest = "SELECT * FROM brichieeee.table2;";
		System.out.println(queryparser.selectParser(querySelectTest));	
		Object a = S.readDatas(queryparser.selectParser(querySelectTest));
		if (a instanceof SelectValues)
		{
			System.out.println(((SelectValues) a).getValues());
			System.out.println(((SelectValues) a).getTimestamps());
		}

		String querySelectTest2 = "SELECT temperature FROM brichieeee.table2;";
		System.out.println(queryparser.selectParser(querySelectTest2));
		System.out.println(S.readDatas(queryparser.selectParser(querySelectTest2)));
		
		String querySelectTest3 = "SELECT * FROM brichieeee.table2 WHERE temperature>33 AND time=2013-12-12 22:00:00.0;";
		System.out.println(queryparser.selectParser(querySelectTest3));	
		System.out.println(S.readDatas(queryparser.selectParser(querySelectTest3)));
	}

}
