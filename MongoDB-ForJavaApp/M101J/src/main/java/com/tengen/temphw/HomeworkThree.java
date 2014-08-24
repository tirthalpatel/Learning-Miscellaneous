package com.tengen.temphw;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class HomeworkThree {

	public static void main(String[] args) throws UnknownHostException {

		MongoClient client = new MongoClient();
		DB db = client.getDB("school");
		DBCollection collection = db.getCollection("students");

		// Sort by student_id and then by score
		DBCursor cursor = collection.find().sort(new BasicDBObject("_id", 1));

		try {
			while (cursor.hasNext()) {
				DBObject studentDoc = cursor.next();
				System.out.println(studentDoc);

				// DBObject scoresDoc = (DBObject) studentDoc.get("scores");
				// System.out.println("------> " + scoresDoc);

				List<DBObject> scores = (List<DBObject>) JSON.parse(studentDoc.get("scores").toString());				
				
				Collections.sort(scores, new Comparator<DBObject>() {
				    public int compare(DBObject o1, DBObject o2) {				  
				        return new Double(o1.get("score").toString()).compareTo(new Double(o2.get("score").toString())); 
				    } 
				});								

				System.out.println("--->" + scores);

				for(DBObject score : scores) {
					if("homework".equals(score.get("type"))) {						
						scores.remove(score);
						break;
					}					
				}
				
				System.out.println("--->" + scores);
				
				studentDoc.removeField("scores");
				studentDoc.put("scores", scores);
				
				System.out.println("--->" + studentDoc);
				
				// collection.update(new BasicDBObject("_id", studentDoc.get("_id")), studentDoc);
			}
		} finally {
			cursor.close();
		}
	}
}
