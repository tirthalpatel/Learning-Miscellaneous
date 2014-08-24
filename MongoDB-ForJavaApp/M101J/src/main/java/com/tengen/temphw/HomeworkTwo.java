package com.tengen.temphw;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;

public class HomeworkTwo {

	public static void main(String[] args) throws UnknownHostException {

		MongoClient client = new MongoClient();
		DB db = client.getDB("students");
		DBCollection collection = db.getCollection("grades");

		// Get documents where type is homework
		QueryBuilder builder = QueryBuilder.start("type").is("homework");
		
		// Sort by student_id and then by score
		DBCursor cursor = collection.find(builder.get())
                .sort(new BasicDBObject("student_id", 1).append("score", 1));                

        try {
            while (cursor.hasNext()) {
            	DBObject lowSocre = cursor.next(); 
                //DBObject highScore = cursor.next();                                
                                
                System.out.println(lowSocre);
                //System.out.println(highScore);
                
                //collection.remove(lowSocre);                
            }
        } finally {
            cursor.close();
        }
	}

}
