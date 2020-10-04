# PART 1: How to build cloud-native applications with Cassandra

Theory + Hands-on exercises -> Key takeaways

------

## Week 2: Data Modeling for Apache Cassandra™ - [video](https://www.youtube.com/watch?v=V7dnCeJrtD4) and [material](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week2-DataModel)

* Learn the fundamentals of a good data model, a data modeling methodology as well as some data modeling common issues which should be avoided
* Learn practical techniques to properly design and implement your data model

### [Creating an efficient Data Model for highly-loaded applications with Apache Cassandra](https://cassandra.apache.org/doc/latest/data_modeling/index.html)

#### Data structure: Keyspace, Tables, Partition

![Cassandra Data Structure](images/02-Cassadra-DataStructure.png?raw=true)

* Data is stored in **tables**, whose schema defines the layout of said data in the table, and those tables are grouped in **keyspace**
* A **Keyspace** 	
	- Top-level namespace/container : Contains tables and sets replication
	- A group of tables sharing replication strategy, replication factor and other properties
	- Defines a number of options that applies to all the tables it contains including the **replication strategy** (tells Cassandra how to distribute the replicated data across the database machines) and the **replication factor** (tells Cassandra how many copies of the data to create)
	- The **SimpleStrategy** defines a replication factor for data to be spread across the entire cluster 
	- The **NetworkTopologyStrategy** is a production ready replication strategy that allows to set the replication factor independently for each data-center
	- How to create Keyspace?
		```
		CREATE KEYSPACE my_first_ks
		  WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1' : 1, 'DC2' : 3}
		  AND durable_writes = false;
		```
* A 'Table' 
	- A group of columns and rows storing partitions 
	- **Primary Key** : An unique identifier for a row : all table must define the primary key (and only one) : Must ensure uniqueness + May define sorting : The primary key is composed of two parts namely Partition key and clustering columns
	- The **Partition Key** : A value of a column(s) to calculate a token : An identifier for a partition (e.g. msgid) : Consists of at least one column, may have more if needed (so-called composite partition) : Partitions rows
	- The **Clustering Column(s)** : Used to ensure uniqueness and sorting order (e.g. added_date, userid) : Optional
	- How to create table?
		```
		CREATE TABLE my_first_ks.my_messages_table (
		  msgid uuid,
		  added_date timestamp,
		  userid uuid,
		  message text,	
		  PRIMARY KEY ((msgid), added_date, userid));
		```  
* Casssandra **hashes the full partition key to retrieve a partition**, and this has implied consequences: 
	- Retrieving a single partition is very fast
	- Retrieving multiple partitions will be slower
	- Partition keys have no inherent order, so cannot perform "greater than" or "less than" types of operations on partition keys
	- Since Cassandra orders the rows within a partition based on clustering columns, you can perform "greater than" or "less than" operations on clustering columns after specifying the partition key
* A **Partition** 
	- A group of rows having the same partition token, a based unit of access in Cassandra : stored together, all the rows are guaranteed to be neighbours
	- Every node is responsible for a range of tokens (e.g. 0-100500, 100501-201000...)
	- INSERT a new row, it must include the value of its Partition Key (can’t be null!) : Hash this value using [MurMur3 hasher](http://murmurhash.shorelabs.com/) : O(1) constant time algorithm : E.g. “Seattle” becomes "2466717130", if "Partition Key = Seattle" and "Partition Token = 2466717130"
	- This partition belongs to the node[s] responsible for this token
	- The INSERT query goes to the nodes storing this partition (Notice Replication Factor)
* Rules of Good Partition
	- **Store together what you retrieve together** : For example, To open a video? Get the comments in a single query! The **PRIMARY KEY ((video_id), created_at, comment_id)** shall keep all relevant data on the same partition, so no need to scan through different partitions during query
	- **Avoid big and constantly growing partitions** : No technical limitations, but… for better query performance... try to keep... Up to ~100k rows in a partition / Up to ~100MB in a Partition : Leverage **Bucketing** (i.e. **Composite partition key**) : For example, a huge IoT infrastructure, hardware all over the world, different sensors reporting their state every 10 seconds. Every sensor reports its UUID, timestamp of the report, sensor’s value. The **PRIMARY KEY ((sensor_id), reported_at)** shall result to big partition in few months. So, go for bucketing in such cases like **PRIMARY KEY ((sensor_id, month_year), reported_at)** shall have different partition every month for each sensor_id 
	- **Avoid hot partitions** : For example, the **PRIMARY KEY ((country), user_id)** shall have less loaded partitions for most European countries, however super overloaded partitions for few Asian contries like China or India : Results to inconsistent performance
	- **Always specify the partition key in the query!** : If there is no partition key in a query, Cassandra can't understand which node you will ask? : For example, when **PRIMARY KEY ((city), last_name, first_name, email))**, the **SELECT address FROM users_by_city WHERE city = “Otterberg” AND last_name = “Koshkina”** query is correct one over the **SELECT address FROM users_by_city WHERE first_name = “Anna”**

### Homework: [DS220 - DataStax Enterprise 6 Practical Application Data Modeling with Apache Cassandra™](https://academy.datastax.com/#/online-courses/ca2e1209-510b-44a6-97de-d5219d835319)

------