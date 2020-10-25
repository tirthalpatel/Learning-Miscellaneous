# PART 1: How to build cloud-native applications with Cassandra

Theory + Hands-on exercises -> Key takeaways

------

## Week 1: Getting Started with Cassandra - [video](https://www.youtube.com/watch?v=VW8C3nU0EzQ) and [material](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week1%20-%20Getting%20Started%20with%20Cassandra)

* Learn the fundamentals of Apache Cassandra™, its **distributed architecture**, and how data is stored
* Master Cassandra's internal architecture by studying the **read path**, **write path**, and **compaction**
* Develop the skills necessary to build disruptive cloud applications by understanding topics such as **consistency**, **replication**, **anti-entropy operations**, and **gossip**
* Introduction of **CQL** and **Datastax Astra**

### [Apache Cassandra](https://cassandra.apache.org/doc/latest/getting_started/index.html) and its [Architecture](https://cassandra.apache.org/doc/latest/architecture/index.html)

* NoSQL Distributed Database
* Cassandra is written in Java
* The topology of Cassandra includes several layers:
	- **Node**: the single deployed instance of Cassandra
	- **Rack**: the collection of Cassandra’s instances grouped by some attribute and located in the same data center
	- **Datacenter**: the combination of all Racks located in the same data center	
	- **Cluster**: the collection of all Datacenters
* Per Cassandra node installation : **~2-4 TB capacity** : **LOTs Tx/sec/core throughput**
* Based on **peer-to-peer architecture** instead of leader/follower: No master or slave node : No master single point of failure : Every node is equal (can perform both reads and writes) : Every node acts as **Coordinator node** : Handles inter-node communication through the **Gossip protocol**
* Cassandra uses an IP address to identify a node
* Group of Cassandra nodes located in the same physical location, a cloud datacenter or an availability zone form **DataCenter** | **Ring**
* A **Cluster** is a group of datacenters configured to work together : Supporting Geographic Distribution, Hybrid-Cloud and Multi-Cloud : Can be deployed to any combination of on-premise and cloud provider
* **Scales linearly** (very low to zero overhead on adding new nodes) : Recommended to use all nodes with same level of state and computational power, disk, cpu, etc.
* Automatic partitioning and replication
	- Data is Distributed : **Partition** is a first-class feature : Using **Partition Key**
	- Resilient, fault-tolerent and disaster-tolerent system : **Replication** is a first-class feature : **Replication Factor (RF)** = No of Replica : Recommended RF = 3 : Replication Factor = Configurable per Keyspace per Datacenter
* [The CAP theoram](https://www.youtube.com/watch?v=82wuPR5exmM) : States that in a failure scenario with a distributed system only two of three guarantees are achieved : By default, **Cassandra is AP** (Availability + Partition Tolerance) state : Cassandra **can be configured to CP** (Consistency + Partition Tolerance) using Consistency Level (CL) - mentions that how many replicas for query to respond OK : Recommended way for Cassandra is **Immediate Consistency** (CL-Read + CL-Write > RF) using Read/Write#Quorum/Quorum : Lower the consistency level (e.g. ONE) = Faster read/write data and much more high availability : Higher the consistency level (e.g. ALL) = Much slower to read/write data, and less availabiliy
* Cassandra stores part of the data in RAM to speed up reading and writing
	- **Write path** = When a write query reaches to Cassandra coordinator node, it goes to Memory (sorted by partition key data in **MemTable on RAM** for keeping read-optimized real data) as well as Disk (append only **Commit Log on Disk** for reliability and change data capture), and returns the ACK : When MemTable RAM has enough data, it's flushed to immutable **SSTable (Sorted String Table) on Disk** based on multiple conditions like state of RAM, TTL, Manual flush... : Once data is flushed to SSTable (a log of mutations), the same data is not required in the MemTable and Commit Log : Multiple SSTables (holds ordered partitions) are optimized for disk usage and faster reads by compaction
	- **Read path** = Reading data from MemTable (by identifying partition) or SSTable (using partition summary on RAM and partition index on Disk) : Key Cache (RAM)
	
![Cassandra Use Cases](images/01-Cassandra-Use-Cases.png?raw=true)
	
### [Cassandra Query Language (CQL)](https://cassandra.apache.org/doc/latest/cql/index.html)

* The **CQL** is the API to access Cassandra databases like SQL for relational databases : To use CQL, connect to the cluster either using cqlsh or through a client driver for Cassandra
* The **cqlsh** a command line shell for interacting with Cassandra through CQL : Shipped with every Cassandra package : Connects to the single node specified on the command line
* **Apache Cassandra Drivers** are the mechanisms to interact with Apache Cassandra™ from a programming language or within an application

### [DataStax Astra](https://www.datastax.com/products/datastax-astra)

[Do-it-yourself](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week1%20-%20Getting%20Started%20with%20Cassandra)

![Cloud-native Cassandra-as-a-Service built on Apache Cassandra™](images/01-Astra-Inroduction.png?raw=true)

### Homework: [DS101: Introduction to Apache Cassandra™](https://academy.datastax.com/#/online-courses/0da20519-364d-47a9-9916-b59c02175393) & [DS201 - DataStax Enterprise 6 Foundations of Apache Cassandra™](https://academy.datastax.com/#/online-courses/6167eee3-0575-4d88-9f80-f2270587ce23)

* The **Lessons Learned** from the Failures of building Big Data Distributed Systems using RDBMS notions: 
	- Consistency (in ACID) is not practical due to replication lag in master-slave deployment model, so give it up with notion of **eventual consistency**
	- Manual **sharding and rebalancing** is hard, so it should be **built-in** as first-class feature
	- High availability is complicated, and requires additional operational overhead (e.g. for master failover, planned vs. unplanned downtime?), so should be **simplified architecture without master/slave** notion
	- Scaling up is expensive, so rather prefer **horizontal scaling using commodity hardware**
	- Third Normal Form (with complex join query and expensive disk seeks) doesn't scale and Scatter/Gather is no good for query big data across multiple nodes, so rather **denormalize** for real time query performance and aim to always hit 1 machine
* Choosing a Distribution:
	- **Open Source** : Latest, bleeding edge features : Support and bug fixes using IRC and mailing lists of Community : Perfect for hacking or trying new stuff
	- **Datastax Enterprise** : Integrated Multi-DC search : Integrated Spark for analysis : Focused on stable releases for enterprise (Extended support, Additional QA)
	- No license is required to use **DataStax Enterprise Edition (DSE)** for development : Must first obtain a license before using DataStax Enterprise edition for production
* Cassandra Fundamentals:
	- The ACID compliance is a feature of RDBMS, but not supported in Cassandra
	- **Availability and Partition Tolerance** for CAP Theoram
	- The **Replication Factor** (RF) represents that how many copied of each piece of data should you have in Cassandra cluster : can be set when creating a keyspace
	- The **Consistency Level** (CL) is set on a **per-query** basis : can be set for both read and write requests : impacts speed of data read and write : impacts high availability goal
	- **Hinted handoff** is used when a node is down to replay all of writes that occurred
	- Supports **multiple datacenters** out of the box
* Cassandra Internals: 


------