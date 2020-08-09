# PART 1: How to build Applications with Cassandra

Theory + Hands-on exercises

## Week 1: [Getting Started with Cassandra](https://community.datastax.com/questions/5834/index.html)

* Learn the fundamentals of Apache Cassandra™, its **distributed architecture**, and how data is stored
* Master Cassandra's internal architecture by studying the **read path**, **write path**, and **compaction**
* Develop the skills necessary to build disruptive cloud applications by understanding topics such as **consistency**, **replication**, **anti-entropy operations**, and **gossip**

### [Apache Cassandra](https://cassandra.apache.org/doc/latest/getting_started/index.html) and its [Architecture](https://cassandra.apache.org/doc/latest/architecture/index.html)

* NoSQL Distributed Database
* Per Cassandra node installation : **~2-4 TB capacity** : **LOTs Tx/sec/core throughput**
* Based on **peer-to-peer architecture** instead of leader/follower: No master or slave node : No master single point of failure : Every node is equal (can perform both reads and writes) : Every node acts as **Coordinator node** : Handles inter-node communication through the **Gossip protocol**
* Group of Cassandra nodes located in the same physical location, a cloud datacenter or an availability zone form **DataCenter** | **Ring**
* A **Cluster** is a group of datacenters configured to work together : Supporting Geographic Distribution, Hybrid-Cloud and Multi-Cloud : Can be deployed to any combination of on-premise and cloud provider
* **Scales linearly** (very low to zero overhead on adding new nodes) : Recommended to use all nodes with same level of state and computational power, disk, cpu, etc.
* Automatic partitioning and replication
	- Data is Distributed : **Partition** is a first-class feature : Using **Partition Key**
	- Resilient, fault-tolerent and disaster-tolerent system : **Replication** is a first-class feature : **Replication Factor (RF)** = No of Replica : Recommended RF = 3
* [The CAP theoram](https://www.youtube.com/watch?v=82wuPR5exmM) : States that in a failure scenario with a distributed system only two of three guarantees are achieved : By default, **Cassandra is AP** (Availability + Partition Tolerance) state : Cassandra **can be configured to CP** (Consistency + Partition Tolerance) using Consistency Level (CL) : Recommended way for Cassandra is **Immediate Consistency** (CL-Read + CL-Write > RF) using Read/Write#Quorum/Quorum
* **Write path** = When a write query reaches to Cassandra coordinator node, it goes to Memory (sorted by partition key data in **MemTable on RAM** for keeping read-optimized real data) as well as Disk (append only **Commit Log on Disk** for reliability and change data capture), and returns the ACK : When MemTable RAM has enough data, it's flushed to immutable **SSTable (Sorted String Table) on Disk** based on multiple conditions like state of RAM, TTL, Manual flush... : Once data is flushed to SSTable (a log of mutations), the same data is not required in the MemTable and Commit Log : Multiple SSTables (holds ordered partitions) are optimized for disk usage and faster reads by compaction
* **Read path** = Reading data from MemTable (by identifying partition) or SSTable (using partition summary on RAM and partition index on Disk) : Key Cache (RAM)
	
### [Cassandra Query Language (CQL)](https://cassandra.apache.org/doc/latest/cql/index.html)

* The **CQL** is the API to access Cassandra databases like SQL for relational databases : To use CQL, connect to the cluster either using cqlsh or through a client driver for Cassandra
* The **cqlsh** a command line shell for interacting with Cassandra through CQL : Shipped with every Cassandra package : Connects to the single node specified on the command line
* **Apache Cassandra Drivers** are the mechanisms to interact with Apache Cassandra™ from a programming language or within an application

### [DataStax Astra](https://www.datastax.com/products/datastax-astra)

* **Cloud-native Cassandra-as-a-Service** built on Apache Cassandra™
* [Do-it-yourself](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week1%20-%20Getting%20Started%20with%20Cassandra)

### [DS201: DataStax Enterprise 6 Foundations of Apache Cassandra™](https://academy.datastax.com/#/online-courses/6167eee3-0575-4d88-9f80-f2270587ce23)

* No license is required to use **DataStax Enterprise Edition (DSE)** for development : Must first obtain a license before using DataStax Enterprise edition for production

## Week 2: [Data Modelling for Apache Cassandra™](https://community.datastax.com/questions/6078/index.html)

## Week 3: [Application Development, Backend Services and CRUD](https://community.datastax.com/questions/6377/materials-and-homework-for-week-3.html) 

## Week 4: [Application Development, Microservices and REST API](https://community.datastax.com/questions/6738/index.html)
