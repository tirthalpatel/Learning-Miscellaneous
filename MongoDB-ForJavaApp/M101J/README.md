MongoDB for Java Developers - M101J Course
============================================

The sample code snippet of M101J course @ https://university.mongodb.com/courses. Read related blog post [here] ().


Setup this Maven project directly in Eclipse for quick start 
-------------------------------------------------------------
(1) Download / Clone this project to your local machine.

(2) Just import "M101J" project in Eclipse. Fix build path errors, such as JRE configuration. That's it.


Course Syllabous
-----------------

* Week 1: [Setup MongoDB and Getting Started with it] (http://docs.mongodb.org/manual/tutorial/getting-started/) and [Hello World Programs] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/helloworld)

* Week 2: [CRUD in MongoDB] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/crud)

* Week 3: [Handling blobs using GridFS] (https://github.com/tirthalpatel/Learning-Miscellaneous/blob/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/GridFSTest.java) and [Import Tweets Example] (https://github.com/tirthalpatel/Learning-Miscellaneous/blob/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/ImportTweets.java) 

* week 5: [Aggregation Framework] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/scripts/aggregration_framework)

* week 6: [Creating Replica Set] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/scripts/creating_a_replica_set), [Connecting to a Replica Set from the Java Driver] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/replication), [Building Sharded Environment] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/scripts/building_a_sharded_environment)

* Final Tutorial of Blogging Application:
	* How to run it? 
		- Ensure MongoDB instance "mongodb://localhost" is up and running, as default configuration is in [BlogController.java] (https://github.com/tirthalpatel/Learning-Miscellaneous/blob/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/blog/BlogController.java).
		- Run BlogController.java [as java application](https://github.com/tirthalpatel/Learning-Miscellaneous/blob/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/blog/BlogController.java) or [using maven command](https://github.com/tirthalpatel/Learning-Miscellaneous/blob/master/MongoDB-ForJavaApp/M101J/scripts/blog_app/run.sh). This would run it on HTTP 8082 port using Spark framework.
		- Now you should be able to access it in browser using this URL [http://localhost:8082/] (http://localhost:8082/) and can play with the blogging application functionality.
	* Executive summary of code structure?
		- [UI components are designed using Freemarker template] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/src/main/resources/blog), which are mapped for URL routing using Spark framework in [BlogController.java] (https://github.com/tirthalpatel/Learning-Miscellaneous/blob/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/blog/BlogController.java).
		- [DAO components] (https://github.com/tirthalpatel/Learning-Miscellaneous/tree/master/MongoDB-ForJavaApp/M101J/src/main/java/com/tengen/blog) has data access layer code implemented using MongoDB driver.
		- MongoDB database name is "blog", which contains three collections (1) users (2) sessions (3) posts.	 