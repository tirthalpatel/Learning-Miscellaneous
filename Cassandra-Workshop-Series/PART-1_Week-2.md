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
	- The **Clustering Column(s)** : Used to ensure uniqueness and sorting order (e.g. added_date, userid) : Optional : Rows are always retrieved using the clustering order or its reverse : While the default order for each column is ascendant or ASC, it can be customized on a per column basis using the CLUSTERING ORDER BY clause
	- A table with **single-row partitions** : is a table where there is exactly one row per partition : Defines a primary key to be equivalent to a partition key
	- A table with **multi-row partitions** is a table where there can be one or more rows per partition : Defines a primary key to be a combination of both partition and clustering keys : Rows in the same partition have the same partition key values and are ordered based on their clustering key values using the default ascendant order
	- **Static columns** - Some non-key columns in a table with multi-row partitions can be declared as static columns : Is shared by all rows belonging to the same partition : Can be used to optimize the storage and simplify column updates
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
	- The INSERT query goes to the nodes storing this partition
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
	- Possible to define collection data types that contain nested collections, such as list of maps or set of sets of sets : Use **Frozen** keyword in collection to nest datatypes : **Nested Collection** definition has to be designated as **FROZEN** : Using a Frozen will serialize multiple components into single value : Values in Frozen collection are treated like Blobs : When an individual element of a frozen collection needs to be updated, the entire collection must be overwritten : Nested collections are generally less efficient unless they hold immutable or rarely changing data : Non-frozen types allow updates to individual fields
	- Collections may only be used in primary keys, if they are frozen
* **User Defined Types** (UDTs) - Attach multiple data fields to a column : Can be any datatype including collections and other UDTs : Allows embedding more complex data within a single column

#### Advanced Options

* Creating **User Defined Functions** (UDFs) - Write custom functions using Java or JavaScript : Use in SELECT, INSERT, and UPDATE statements : Function are only available within the Keyspace where it is defined : Need to enable in cassandra.yml file 
* Creating **User Defined Aggregates** (UDAs) - DataStax Enterprise allows users to define aggregate functions : Functions are applied on data stored in table as part of query result : The aggregate function must be created prior to its use in SELECT statement : Query must only include the aggregate function itself, and no additional columns
	
### Homework 

#### [DS220 - DataStax Enterprise 6 Practical Application Data Modeling with Apache Cassandra™](https://academy.datastax.com/#/online-courses/ca2e1209-510b-44a6-97de-d5219d835319)

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
* **Optimization Tuning Phase**
	- `Analysis and Validation` : Reasons to change data model (requirements change in the domain - the data model is no longer efficient - data is becoming imbalanced - unforeseen load on particular nodes leading to hotspotting) : Important considerations for changing data model or handling new requirements (natural or surrogate keys? write conflicts (overwrites) possible? what data types to use? how large are partitions? how much data duplication is required? are client-side joins required and at what cost? are data consistency anomalies possible? how to enable transactions and data aggregation?)
	- `Write Techniques:` **Linearizable Consistency** (solution to problems that involve race conditions, e.g. two users trying to register new accounts using the same username, or multiple auction participants placing bids on the same item and potentially overwriting each other's bids) is supported by **Lightweight Transactions** (LWTs) : LWT compare and set operations with ACID properties : Does a read to check a condition, and performs the INSERT / UPDATE / DELETE if the condition is true : Each lightweight transaction is atomic and always works on a single partition : Essentially ACID transaction at partition level : More expensive than regular reads and writes : Use `IF NOT EXISTS` keyword with INSERT : Use `IF` keyword with UPDATE
	- `Write Techniques:` **Data consistency with Atomic Batches** : Schema data consistency refers to the correctness of data copies : With data duplication, need to handle consistency : All copies of same data in schema should have the same values : Adding, updating and deleting data may require multiple INSERTs, UPDATEs and DELETEs : Logged batches were built for maintaining consistency : Batch is not intended for bulk data loading : No ordering for operations in batch rather all writes are executed with the same timestamp : Single-partition batches can even contain lightweight transactions, but multi-partition batches cannot	
	- `Read Techniques:` **Secondary Indexes** : Used to query a table using a column that is not normally queryable : Two types of secondary indexes (i) **Regular secondary index (2i)** that uses hash tables to index data and supports equality predicates (ii) **STable-attached secondary index (SASI)** that is an experimental and more efficient secondary index that uses B+ trees to index data and can support equality, inequality and even LIKE text pattern matching : Can be created on any column including collections except counter and static collumns : Indexes on multiple columns are not supported : Secondary index creates additional data structures on nodes holding table partitions : Use when (i) low cardinality columns (ii) prototyping or small datasets (iii) for search on a both a partition key and an indexed column in large partition : Not to use when (i) high cardinality columns (ii) with tables that use a counter column (iii) frequently updated or deleted columns
	- `Read Techniques:` **Storage Attached Indexes (SAI)** : New secondary index implementation available in Datastax Astra and Datastax Enterprise : Allow to query columns outside the Cassandra partition key without using the ALLOW FILTERING keyword or creating custom tables for each query pattern : Use `CREATE CUSTOM INDEX ... USING 'StorageAttachedIndex'` DDL commands to define one or more SAI indexes on the table : SAI provides more functionality compared to Cassandra secondary indexes, is faster at writes compared to any Cassandra or DSE Search index, and uses significantly less disk space : See [SAI Workshop PPT](https://github.com/DataStax-Academy/workshop-storage-attached-indexes/blob/main/slides/Presentation.pdf)
	- `Read Techniques:` **Materialized Views** : A read-only table that automatically duplicates, persists and maintains a subset of data from a base table : A database object that stores query results : Builds a table from another table's data which has a new primary key and new properties : The columns of the source table's primary key must be part of the materialized view's primary key : Only one new column can be added to the materialized view's primary key : All view primary key columns must be restricted to not allow nulls : Are suited for high cardinality data : Data can be only written to source tables not materialized views : In terms of performance, even though writes to base tables and views are asynchronous, each materialized view slows down writes to its base table by approximately 10%, so recommended to not create more than two materialized views per table
	- `Read Techniques:` **Data Aggregation** : Built-in functions provide some summary form of data : DES 6.0 supports CQL aggregates including SUM, AVG, COUNT, MIN, MAX
	- **How do Data Aggregation for all rows of table** : Possible solutions (i) Update data aggregation on-the-fly in Cassandra using lightweight transactions or counter type (ii) Implement data aggregation on client side (iii) Near real-time batch aggregation using Apache Spark (iv) Stats components using Apache Solr
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
	- What Cassandra structure aids with consistency between duplicate data copies? : `Logged Batch`
	- How do lightweight transaction differ from normal inserts/updates? : They compare (`validate a condition`) before setting (`read before write`)
	- Which data replication strategy is recommended for production? : `NetworkTopologyStrategy`
	- A table can have many rows per partition if ... : `It has a primary key consisting of partition and clustering keys`
	- Clustering order defines how rows are sorted within : `A partition`
	- Static columns are used to store values that are : `The same for all rows in a partition`
	- A table with a composite partition key can be queried using ... : `all columns of the partition key`
	- Inequality predicates are allowed on ... : `clustering key columns`
	- Row ordering is only possible ... : `within each partition`
	- In a cluster with two datacenters and replication factors 5 and 3, how many replicas must respond to satisfy CL QUORUM? : `5`
	- Which of the following consistency levels are guaranteed to provide strong consistency in any cluster? : `QUORUM for writes and QUORUM for reads`
	- Lightweight transactions ensure ... : `linearizable consistency`
	- Under what circumstances is the use of lightweight transactions justified? : `race conditions and low data contention`
	- The main reason to use a batch is ... : `atomicity`
	- When creating a batch you should ... : `not rely on statement ordering for a correct result`
	- What is the main real-time transactional use case for a secondary index? : `retrieving rows from a large multi-row partition`
	
#### [Hands-on Learning Series - Cassandra Fundamentals using CQL](https://www.datastax.com/learn/cassandra-fundamentals)

#### [Advanced Data Modeling Workshop](https://github.com/datastaxdevs/advanced-data-modeling-workshop) & [Hands-on Learning Series - Data Modeling by Example](https://www.datastax.com/learn/data-modeling-by-example)

------