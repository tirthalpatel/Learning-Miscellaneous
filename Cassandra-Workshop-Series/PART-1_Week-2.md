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
* A **Table** 
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

#### The Art of Data Modeling

* **Relational Data Modelling**:
	1. Analyze raw data 
	2. Identify entities, their properties and relations
	3. Design tables, using normalization and foreign keys
	4. Use JOIN when doing queries to join denormalized data from multiple tables
* **NoSQL Data Modelling**:
	1. Analyze user behaviour (customer first!)
	2. Identify workflows, their dependencies and needs
	3. Define Queries to fulfill these workflows
	4. Knowing the queries, design tables, using denormalization
	5. Use BATCH when inserting or updating denormalized data of multiple tables
* **Cassandra Data Modeling** starts with the queries in mind, and here are the steps to the process:
	- Enumerate all use-cases and their interdependencies
	- Use the use cases to identify all queries the app will perform
	- Use the queries to drive the table definitions
* If you want your **Cassandra queries** to be fast:
	- Create tables where the full partition key is how you will query the table
	- The clustering columns are how you want to order the results : Can perform either equality (=) or range queries (<, >) on clustering columns : Since data is sorted on disk, range searches are a binary search followed by a linear read
	- Clustering columns default ascending order : Change ordering direction via **WITH CLUSTERING ORDER BY**
	- Do not require more than one partition key per query
	- **Allow Filtering** : Relaxes quering on partition key constraint and allow query on just clustering columns : Causes Apache Cassandra to scan all partitions in the table : Don't use it
* **Cassandra does not do a read before writing** because that would cripple Cassandra's performance:
	- Instead, Cassandra assumes that if you are inserting a row, the row doesn't exist and Cassandra proceeds to quickly write the row.
	- Likewise, Cassandra does not read anything before writing an update. So, if the record we are updating does not exist, Cassandra merely creates it.
	- If you absolutely must perform a read before writing due to a race condition, Cassandra provides lightweight transactions. **Lightweight transactions** force Cassandra to read before writing, but they have serious performance implications and should be **avoided if possible**.
	
#### Data Types

* CQL is a typed language and [supports a rich set of data types](https://cassandra.apache.org/doc/latest/cql/types.html), including **native types**, **collection types**, **user-defined types**, **tuple types** and **custom types**
* **UUID** and **TIMEUUID** - Used in place of integer IDs as Cassandra is a distributed database
	- UUID : Universal Unique Identifier : Generate via uuid()
	- TIMEUUID embeds a TIMESTAMP value : Sortable : Generate via now()
* **Blob** - Arbitrary bytes (no validation), expressed as Hexadecimal in Cassandra
* **Boolean** - Stored internally as true or false
* **Inet** - IP address string in IPv4 or IPv6 format
* **Counter** - Imprecise values use case such as likes, views, etc. : Cannot be part of a primary key : Counters not mixed with other types in table : Need specially dedicated table which can have primary key and one or more counter columns : Incrementing or decrementing counters is not idempotent : Must use UPDATE command to update counter columns : Counter columns can not be indexed or deleted : Incrementing or decrementing a counter is not always guaranteed to work, i.e., under high traffic situations, it is possible for one of these operations to get dropped : Cassandra counters are always NOT 100% accurate
* **Collections**
	- Supports 3 kind of collections: **Maps** (Typed collectio of key-value pairs; Ordered by unique keys), **Sets** (Typed collection of unique values; Stored unordered, but retrieved in sorted ordered) and **Lists** (Do not need to be unique and can be duplicated; Stored in particular order)
	- To avoid performance problems, only use collections for small-ish numbers of elements
	- Sets and maps do not incur the read-before-write penalty, but some list operations do. Therefore, when possible, prefer sets to lists
	- List prepend and append operations are not idempotent, so retrying after a timeout may result in duplicate elements
	- Use **Frozen** keyword in collection to nest datatypes : Using a Frozen will serialize multiple components into single value : Values in Frozen collection are treated like Blobs : Non-frozen types allow updates to individual fields
	- Collections may only be used in primary keys, if they are frozen
* **User Defined Types** (UDTs) - Attach multiple data fields to a column : Can be any datatype including collections and other UDTs : Allows embedding more complex data within a single column
	
### Homework: [DS220 - DataStax Enterprise 6 Practical Application Data Modeling with Apache Cassandra™](https://academy.datastax.com/#/online-courses/ca2e1209-510b-44a6-97de-d5219d835319) & [Hands-on Learning Series - Cassandra Fundamentals using CQL](https://www.datastax.com/learn/cassandra-fundamentals)

* Relational vs. Cassandra
	![Relational vs. Cassandra Modeling](images/02.01-Relational-vs-Cassandra-DataModeling.png?raw=true)
* **Cassandra Data Modeling Methodology**
	![Chebotko Diagram Notions](images/02.02-Chebotko-Diagram-Notions.png?raw=true)
* **Cassandra Data Modeling Principles** 
	- `Know your data` : Key constraints affect schema design : Primary key uniquely identifies a row / entity / relationship
	- `Know your queries` : Queries directly affect schema design : Queries captured by application workflow model : Table schema design changes if queries change : Single Partition Per Query is Ideal (most efficient access pattern, query accesses only one partition to retrieve results, partition can be single-row or multi-row) : Partition+ Per Query is Acceptable (less efficient access pattern but not necessarily bad, query needs to access multiple partitions to retrieve results) : Table Scan / Multi-Table Scan is Anti-Pattern (least efficient type of query but may be needed in some cases, query needs to access all partitions in the table(s) to retrieve results
	- `Nest data` : Nesting organizes multiple entities into a single partition : Supports partition per query data access : Three data nesting mechanisms are clustering columns (multi-row partitions), collection columns, and user-defined type columns
	- `Duplicate data` : Better to duplicate than to a join : Data duplication can scale, but joins cannot : Partition per query and data nesting may result in data duplication : Query results are pre-computed and materialized : Data can be duplicated across tables, partitions, and / or rows
* **Mapping Rules** of Conceptual Data Model and Data Access Patterns to create a Logical Data Table : (1) Entities and Relationships (2) Equality search attributes (3) Inequility search attributes (4) Ordering attributes (5) Key attributes
* **User Defined Functions** (UDFs) and **User Defined Aggregates** (UDAs)
	- Creating UDFs : Write custom functions using Java or JavaScript : Use in SELECT, INSERT, and UPDATE statements : Function are only available within the Keyspace where it is defined : Need to enable in cassandra.yml file
	- Creating UDAs : DataStax Enterprise allows users to define aggregate functions : Functions are applied on data stored in table as part of query result : The aggregate function must be created prior to its use in SELECT statement : Query must only include the aggregate function itself, and no additional columns
* Quiz
	- Which command drops all records from an existing table? : `TRUNCATE`
	- What command executes a file of CQL statements? : `SOURCE`
	- What command adds/removes columns to/from a table? : `ALTER`
	- What command bulk load data files? : `COPY`
	- What is smallest atomic unit of storage in Cassandra? : `Cell`
	- What is cell? : `Key-Value pair`
	- What is table's main purpose in Cassandra database? : `Serve a query`
	- Why do we nest data in Cassandra? : `Support a partition per query access pattern`
	- What methods are available for loading data into Cassandra? : `COPY command` (import/export CSV), `SSTable loader` (load pre-existing external SSTables into a cluster), `DataStax Enterprise Bulk loader` (used for loading large amounts of data fast using both CSV or JSON format)
	
------