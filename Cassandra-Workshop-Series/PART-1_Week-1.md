# PART 1: How to build cloud-native applications with Cassandra

Theory + Hands-on exercises -> Key takeaways

------

## Week 1: Getting Started with Cassandra - [video](https://www.youtube.com/watch?v=VW8C3nU0EzQ) and [material](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week1%20-%20Getting%20Started%20with%20Cassandra)

* Learn the fundamentals of Apache Cassandra™, its **distributed architecture**, and how data is stored
* Introduction of **CQL** and **Datastax Astra**
* Master Cassandra's internal architecture by studying the **read path**, **write path**, and **compaction**
* Develop the skills necessary to build disruptive cloud applications by understanding topics such as **consistency**, **replication**, and **gossip**

### [Apache Cassandra](https://cassandra.apache.org/doc/latest/getting_started/index.html) and its [Architecture](https://cassandra.apache.org/doc/latest/architecture/index.html)

* NoSQL Distributed Database
* Cassandra is written in Java
* The topology of Cassandra includes several layers:
	- **Node**: the single deployed instance of Cassandra which runs on JVM as a Java process
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
* [The CAP theoram](https://www.youtube.com/watch?v=82wuPR5exmM) : States that in a failure scenario with a distributed system only two of three guarantees are achieved : By default, **Cassandra is AP** (Availability + Partition Tolerance) state : Cassandra **can be configured to CP** (Consistency + Partition Tolerance) using Consistency Level (CL) which tells how to keep the data replicas consistent
* Cassandra stores part of the data in RAM to speed up reading and writing
	
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
* Cassandra Fundamentals and Internals:
	- The ACID compliance is a feature of RDBMS, but not supported in Cassandra
	- **Availability and Partition Tolerance** for CAP Theoram
	- Supports **multiple datacenters** out of the box
	- **Partitions** - Group rows physically together on disk based on the partition key
	- What is the **role of the partitioner**? - It hashes the partition key values to create a partition token
	- What can a single Cassandra node handle? - Typically, **6,000-12,000 transactions/second/core** and **2-4 Terabytes data on SSD** (do not consider network attached storage / SAN)	
	- Apache Cassandra™ **Token Ring** - A ring keeps track of tokens : Enables Apache Cassandra™ to know exactly which nodes contain which partitions : Eliminates any single points of failure
	- **New node joining the cluster** - (1) Nodes join the cluster by communicating with any node in the cluster (2) Apache Cassandra finds these seed nodes list of possible nodes in cassandra.yaml (3) Seed nodes communicate cluster topology to the joining node (4) Once the new node joins the cluster, all nodes are peers
	- Per-query basis, the **drivers** intelligently choose which node would best coordinate a request using one of supported policies like **TokenAwarePolicy** (driver chooses the node which contains data), **RoundRobinPolicy** (driver round robins the ring), **DCAwareRoundRobinPolicy** (driver round robins the target data center)
	- **VNodes** (virtual nodes) - Managing partitions on solely physical nodes causes problems. For example, when a physical node goes down, it is necessary to redistribute partitions. This is where VNodes come in : When redistributing partitions across physical nodes (e.g. in case of adding/removing nodes) with VNodes, it helps keeping the cluster balanced : VNodes automate token range assignment : VNode settings configurable in cassandra.yml (e.g. `num_token` value to change default 128 vnodes for each node and keep it greater than one to turn on vnode usage)
		![Cassandra VNodes](images/01.03-Cassandra-Overview-VNodes.png?raw=true)
	- **Gossiping** - Broadcasting protocol for disseminating nodes' status amongst its peers (i.e. gossiping cluster metadata) : Doesn't cause network spikes (Minimal constant rate of network traffic compared to data streaming, hints, etc.)
	- **About choosing a Gossip Node** - Each node initiates a gossip round every second : Picks one to three nodes to gossip with : Nodes can gossip with ANY other node in the cluster : Probabilistically (slightly favor) seed and downed nodes : Nodes do not track which nodes they gossiped with prior : Reliably and efficiently spreads node metadata through the cluster : Fault tolerant--continues to spread when nodes fail
	- The **Replication Factor** (RF) represents that how many copies of each piece of data should you have in Cassandra cluster : can be set when creating a keyspace : RF greater than one (i) widens the range of token values a single node is responsible for (ii) causes overlap in the token ranges amonst nodes (iii) requires more storage in your cluster
		![Replication Concepts](images/01.04-Cassandra-Overview-Replication.png?raw=true)
	- **Consistency** - The higher the consistency, the less chance you may get stale data : Cassandra hasn't optimized for consistency over availability, rather offers tunable consistency : The **Consistency Level** (CL) is set on a **per-query** basis to inform Cassandra that how the data is acknowledged on write : Can be set for both read and write requests : Impacts speed of data read and write : Impacts high availability goal : Recommended way for Cassandra is **Immediate Consistency** (CL-Read + CL-Write > RF) using Read/Write#Quorum/Quorum : Lower the consistency level (e.g. ONE) = Faster read/write data and much more high availability : Higher the consistency level (e.g. ALL) = Much slower to read/write data, and no availabiliy : With a replication factor of three, following all options guarantee strong consistency `write all, read one`, `write all, read quorum`, `write quorum, read all`, `write one, read all` 
		![Consistency Concepts](images/01.05-Cassandra-Overview-Consistency.png?raw=true)
	- **Hinted handoff** - Helps Cassandra maintain consistency even when a node is down : Used when a node is down to replay all of writes that occurred : Configurable in cassandra.yml : Can disable hinted handoff : Choose directory to store hints file : Set amount of time a node will store hints : Default is three hours
	- **Read Repair** - As distributed systems trade-off consistency for performance, some of the nodes in a cluster may become inconsistent. When Cassandra notices these inconsistencies, it takes steps to resolve the consistencies. This resolution is the role of Read-Repair. : Read repair always occurs when consistency level is set to ALL
	- **Write path** = When a write query reaches to Cassandra coordinator node, it goes to Memory (sorted by partition key data in **MemTable on RAM** for keeping read-optimized real data) as well as Disk (append only **Commit Log on Disk** for reliability and change data capture), and returns the ACK : When MemTable RAM has enough data, it's flushed to **SSTable (Sorted String Table) on Disk** based on multiple conditions like state of RAM, TTL, Manual flush... : Once data is flushed to immutable SSTable (a log of mutations), the same data is not required in the MemTable and Commit Log : Multiple SSTables (holds ordered partitions) are optimized for disk usage and faster reads by compaction :In summary, if there were no commit log segments found during startup, no replay needs to be done. If Cassandra finds commit log files, it will replay the mutations in those files into memtables and then flush the memtables to disk. MemTable and SSTable are stored sorted by clustering columns.
		![The Write Path](images/01.01-Cassandra-Overview-TheWritePath.png?raw=true)
		![SSTables](images/01.02-Cassandra-Overview-SSTables.png?raw=true)
	- **Read path** = Reading data from MemTable (by identifying partition) or SSTable (using `partition summary` on RAM and `partition index` on Disk) : `Partition Summary` stores byte offsets into the partition index : `Key Cache` on RAM stores byte offsets of the most recently accessed records : To avoid checking every SSTable data file for the partition being requested, Cassandra employs a data structure known as a **Bloom Filter** (a probabilistic data structure that allows Cassandra to determine one of two possible states - the data definitely does not exist in the given file or the data probably exists in the given file).
		![The Read Path](images/01.01-Cassandra-Overview-TheReadPath.png?raw=true)
* Cassandra Operations:
	- The **nodetool** for node management (e.g. `info` displays current node settings and stats; `status` prints cluster information)
	- The difference between **dsetool status and nodetool status** - The dsetool works with DataStax Enterprise™ as a whole (Apache Cassandra™, Apache Spark™, Apache Solr™, Graph) whereas nodetool is specific to Apache Cassandra™
	- **Snitch** is used to determine/declare each node's rack and data center : Informs Cassandra about the network topology so that requests are routed efficiently and allows Cassandra to distribute replicas by grouping machines into datacenters and racks : Different types of snitches available such as Regular (SimpleSnitch, PropertyFileSnitch, GossipingPropertyFileSnitch, DynamicSnitch) and Cloud Based (Ec2Snitch, Ec2MultiRegionSnitch, GoogleCloudSnitch, CloudstackSnitch) : `SimpleSnitch` is default one to place all nodes in the same data center and rack : `GossipingPropertyFileSnitch` is most popular one to declare the current node's DC/rack information in a cassandra-rackdc.properties file and gossip spreads the setting through the cluster : `DynamicSnitch` is layered on top of actual snitch to maintain pulse on each node's performance and turned on by default for all snitches. See [Dynamic snitching in Cassandra: past, present, and future](https://www.datastax.com/blog/dynamic-snitching-cassandra-past-present-and-future) : Configurable in cassandra.yml (e.g. `endpoint_snitch: SimpleSnitch`) : All nodes in the cluster must use same snitch : Changing network topology requires restarting all nodes
	- **Read-Repair chance** - Performed when read is at a consistency level less than ALL : Request reads only a subset of the replicas : Response sent immediately when consistency level is met : Read repair done asynchronously in the background : `dclocal_read_repair_chance` set to 0.1 (10%) by default for read repair that is confined to the same datacenter as the coordinator node : `read_repair_chance` set to O by default for a read repair across all datacenters with replicas
	- **Nodetool Repair** - Syncs all data in the cluster : Expensive : Grows with amount of data in cluster : Use with clusters servicing high writes/deletes : Last line of defense : Run to synchronize a failed node coming back online : Run on nodes not read from very often
	- **Full Repairs** - Bog down the system : Bigger the cluster and dataset, the worse the time : In times past, it was recommended running full repair within `gc_grace_seconds` : Datastax Enterprise has **NodeSync** feature (Runs in the background continuously repairing your data : Better to repair in small chunks as we go rather than full repair : NodeSync automatic enabled by default to run on each node : Per-table basis need to enable, as default is disabled i.e. `CREATE TABLE myTab1e ( … ) WITH nodesync = { 'enabled': 'true'};` : NodeSync simply performs a read repair on the segment)
	- The **cassandra-stress tool** - Can be used to write several thousand records to the node, e.g. `cassandra-stress write no-warmup n=250000 -port native=9041 -rate threads=1`
	- The **compaction** process in Cassandra merges keys, combines columns, evicts tombstones, consolidates SSTables, and creates a new index in the merged SSTable : Benefits from compaction are Faster read, More optimal disk usage and Less memory pressure : Compaction strategies are configurable, i.e., `SizeTiered Compaction` (Default) triggers when multiple SSTables of a similar size are present; `Leveled Compaction` groups SSTables into levels each of which has a fixed size limit which is 10 times larger than the previous level; `TimeWindow Compaction` creates time windowed buckets of SSTables that are compacted with each other using the Size Tiered Compaction Strategy : Use the ALTER TABLE command to change the strategy, i.e., `ALTER TABLE mykeyspace.mytable WITH compaction = { 'class' : ' LeveledCompactionStrategy' };` 
	
------