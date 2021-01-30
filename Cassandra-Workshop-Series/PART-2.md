# PART 2: Test, deploy and monitor Cassandra clusters and applications in Kubernetes and GKE

Theory + Hands-on exercises -> Key takeaways

------

## Week 5: Cassandra in Kubernetes - [video](https://www.youtube.com/watch?v=q2zszeTIDxE) and [material](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week5-Cass-in-k8s)

------

## Week 6: Deploying App in Kubernetes - [video](https://www.youtube.com/watch?v=eMzHermGwYA) and [material](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week6-App-in-k8s)

------

## Week 7: Performance Benchmark the Data Model - [video](https://www.youtube.com/watch?v=rcyetfquNq4) and [material](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week7-Test-your-Apps)

------

## Week 8: Deploying project in GKE - [video](https://www.youtube.com/watch?v=I6xzFjcfehY) and [material](https://github.com/DataStax-Academy/cassandra-workshop-series/tree/master/week8-k8s-in-the-cloud)

------

## Homework

### [DS210: DataStax Enterprise 6 Operations with Apache Cassandra™](https://academy.datastax.com/#/online-courses/b0ef552b-4f01-4e0e-ac17-6e7ce29ad6f0)

* Know the tools and knowledge required to operate and fine-tune your DataStax Enterprise deployments
* Learn skills and techniques to tune performance and environments, deploy multi-data center functionality, diagnose and resolve common production problems

#### Configuring Clusters

* **Cassandra.yaml** : The main configuration file : Cassandra nodes read this file on start-up : Need to restart the node for the changes to take effect : cluster_name, listen_address, native_transport_address and seeds are minimum properties for quick start : Other commonly used configuration settings are endpoint_snitch, initial_token, commitlog_directory, data_file_directories, hints_directory, saved_cache_directory, hinted_handoff_enabled, max_hint_window_in_ms, row_cache_size_in_mb, etc.

#### Cluster Performance Estimation and Monitoring

* **Cassandra-stress**
	- Stress testing utility for benchmarking / load testing a cluster : Stimulates a user-defined workload
	- Use it to determine schema performance, understand how database scales, optimize data models and settings, determine production capacity
	- Configure it using few sections in YAML file : schema description (defines the keyspace and tabel), column description (outlines how to create the stimulated data), batch description (defines the data insertion pattern), query description (defines the possible queries for test performs)
* Linux commands : **top** (gives brief summary of the system resources - displays the CPU and memory utilization), **dstat** (shows all system resources in a single table like CPU, Disk IO, Network...)
* Cassandra's **nodetool** for performance analysis
	- `nodetool info` : displays node specific info (e.g. amount of space used for SSTables, Heap memory/Off Heap memory, Key cache, Name of Rack and Datacenter...)
	- `nodetool compactionhistory` : lists and describes the compaction actions that have taken place
	- `nodetool gcstats` : node-specific info on the JVM Garbage Collection
	- `nodetool gossipinfo` : cluster-level info which shows the current state of gossip entries according to the target node
	- `nodetool ring` : shows info about tokens in the ring :  use to determine the balance of the tokens in the cluster
	- `nodetool tablestats` : shows info about keyspace/tables flushed to disk
	- `nodetool histograms` : provides statistics about a specific table
	- `nodetool tpstats` : gives info about thread pools
* **System and Output Logs**
	- Cassandra's `system.log` (in `/var/log/cassandra directory by default`) for monitoring/diagnosing node behavior, which logs INFO messages and above : Directory location is configurable using `-Dcassandra.logdir` in the `/etc/dse/cassandra/jvm.options` : The `debug.log` logs all messages, if turned it on	
	- Two ways to configure logging : Using the `logback.xml` configuration file (restart node for changes to the file to take affect) / Using the `nodetool setlogginglevel` (only sets it temporarily until restart)
	- Java's Garbage Collection logging for tracking down garbage collection related issues : Need to turn on GC Logging (statically by editing jvm.options / dynamically by using jinfo) : Log file name is configurable `-Xloggc` in jvm.options
	
#### Adding/Removing Nodes

* Adding nodes - Best Practices
	- With **VNode** clusters, can add nodes incrementally : Adding a single node at a time : result in more data movement : will have gradual impact on cluster performance : will take longer to grow cluster
	- Adding multiple nodes at the same time : Double the size of cluster (**single-token nodes**) : although is possible, do with extreme caution : only attempt with deep understanding of DSE internals
* **Bootstrapping** 
	- A process of a new node joining a cluster 
	- To bootstrap a node : set up the node's configuration files (e.g. cassandra.yaml) and start up the node normally
	- `nodetool cleanup` : Perform cleanup (i.e. compaction)) after a bootstrap on the OTHER nodes
* Removing node from the cluster - Three options for dealing with data
	- `nodetool decommission` : redistribute the data from the node that is going away : do this if you want to decrease the size of the cluster : the node must still be active
	- `nodetool removenode` : redistribute data from replicas : do this if the node is offline and never coming back
	- `nodetool assassinate` : don't redistribute data, just make the node go away : do this as a last resort, if the node is offline and never coming back
* Replacing a Down node : replace rather than remove and add, so don't have to move the data twice : configure the new node for the cluster normally with one additional step of adding a `replace_address` option in the jvm.options
	
#### Care and Feeding of a Cassandra Cluster

* **Leveled Compaction** 
	- Uses multiplier of 10 per level by default
	- SSTable max size is 160MB (sstable_size_in_mb) 
	- Best for `read-heavy workload` 
	- Wastes less disk space compare to other compaction strategies
	- Disadvantages: IO intensive, Compacts many more SSTables at once over size tiered compaction, Compacts more frequently that size tiered, Can't ingest data at high insert speed
* **Size Tiered Compaction**
	- The default compaction strategy
	- Absorbs high `write-heavy workloads` by procastinating compaction as long as possible
	- Other compaction strategies don't handle ingesting data as well as size tiered
	- Triggers compaction based on the number of SSTables
	- compaction_throughput_mb_per_sec controls the compaction IO load on a node
	- **Major Compactions** : Can issue a major compaction via `nodetool compact` : Compacts all SSTables into a single SSTable : New monolithic SSTable will qualify for the largest tiered : Future updates/deletes will fall into smaller tiers : Takes a long time to propogate changes up to largest tiered : Major Compaction is not recommended
* **Time Window Compaction**
	- Built for `Time series workload`
	- An SSTable spanning two windows simply falls into the second windows
	- Good practice to aim for 50ish max SSTables on disk (20ish for active window and 30ish for all past windows combined)
	- For example, one month of data would have window of a day
	-  Tuning : `expired_sstable_check_frequency_seconds` determines how often to check for fully expired (tombstoned) SSTables : Good to tune when using a TTL
* **Repair**
	- Think of repairs as synchronizing replicas
	- Repair ensures that all replicas have identical copies of a given partition
	- Repair occurs : If necessary when detected by reads (e.g. CL=QUORUM) : Randomly with non-quorum reads (table property read_repair_chance) : Manually using `nodetool repair` 
	- Why is Repair necessary? : Nodes may go down for period of time and miss writes : If nodes become overloaded and drop writes
	- When to perform Repair? : If a node has been down for a while : On a regular basis (once every gc_grace_seconds, schedule in lower utilization periods)
	- A full repair can be a lot of work : Two ways to mitigate the work are Primary range repair and Sub-range repair
* **NodeSync**
	- New concept with DSE 6 : DSE only feature as a replacement of Repair
	- Behaves like continuous background repairs that delivers low overhead, consistent performance and ease of use
	- Use cases : Set-and-forget background synchronization : One-time manual synchronization for targeted use
	- How to use it? : Create a cluster with at least two nodes : Create a Keyspace with replication factor >= 2 : Create a table within keyspace with nodesync enabled 
	- Tuning options per node basis : Rate (maximum bytes/second NodeSync will validate and `rate_in_kb` is configurable in cassandra.yaml file) : Target (aspiration time between two validations of the same data & `deadline_target_sec` is configurable in table) : If target and rate conflicts, rate wins
	- CLI tools : `nodetool nodesyncservice` (performs various operations on a single node) : `nodesync` (performs cluster wide operations)
* **The sstablesplit command**
	- An anti-compaction in a way
	- SizeTieredCompactionStrategy for Major compaction can result in a excessively large SSTable (e.g. 200 GB file) : Good idea to split a table because won't get compacted again until the next huge compaction
	- Stop running Cassandra node before using the sstablesplit tool
* **Multi Datacenter Concepts**
	- A cluster of nodes can be logically grouped as racks and data centers : Enables Geographically aware read and write requests routing : Each node belongs to one rack in one data center 
	- The identity of each node's rack and data center may be configured in its `conf/cassandra-rackdc.properties` file
	- Data replication across data centers automatically and transparently : Consistency Level can be specified as LOCAL or EACH level for read/write operations
	- What if one data center goes down? : If node(s) fail, they will stop communicating via gossiping : Recovery can be accomplished with a rolling repair to all nodes in failed data center
	- For implementing multi datacenter cluster : Use NetworkTopologyStrategy rather than SimpleStrategy : Use LOCAL_* consistency level for read/write operations to limit latency : Specify the snitch : Run `nodetool rebuild` command specifying the existing datacenter on all nodes in the new datacenter
	
#### Backup, Restore and Moving Data

* **CQL Copy** : Imports and exports delimited data to and from Cassandra : COPY FROM is intended for small datasets (a few million rows or less) into Cassandra : Non-performant and not robust tool : For importing larger datasets, use DSBulk
* **The sstabledump Tool** : `tools/bin/sstabledump` allows you to see the row data of SSTable in a text format : Dumps the contents of the specified SSTable in the JSON format 
* **The sstableloader Tool** : Provides ability to bulk load external data into a cluster : Requires data to be in SSTable format : Load pre-existing SSTables into an existing cluster or new cluster : Great tool for switching between non-like clusters, as it can re-stream data and repartition it (e.g. going for 30 to 50 node cluster)
* **Spark for Data Loading** : Provides convenient functionality for loading larger external datasets into Cassandra tables in parallel : Ingesting files in CSV, TSV, JSON, XML, and other formats 
* **DSE DSBulk CLI Tool** : DSE Customer first feature : Moves Cassandra data to/from files in the file system : Supports CSV or JSON format : Use cases are loading data from a pile of files, unload data for backup, migration from DSE to DSE due to data model changes, etc.
* **Backup and Restore**	
	- **Incremental backups** are disabled by default, can be enabled by configuring `incremental_backup` in cassandra.yaml : Need a **Snapshot** before taking incremental backups : Snapshots and Incremental backups are stored on each Cassandra node	
	- **Auto Snapshot** configurable in cassandra.yaml (by default enabled), which indicates that whether or not a snapshot is taken of data before tables are truncated and tables and keyspaces are dropped
	- Commonly backup files are copied to an off-node location (e.g. using tablesnap program for backing up to S3)	
	- **nodetool snapshot** command takes a snapshot of one or more keyspaces
	- **nodetool clearsnapshot** command removes snapshots	
	- **Restore Snapshots** options : Delete the current data files and copy the snapshot and incremental files to the appropriate data directory (restart and repair the node after file copying is done) / Use the sstableloader (must be careful to use it as it can add significant load to cluster while loading)
	- Performing **Cluster-wide Backup and Restore** options : OpsCenter, clusterssh tool (to make changes on multiple servers at a time), tablesnap and tablerestore (for Cassandra backup to AWS S3), Recovery

#### Cassandra's Performance Tuning Options

* Cassandra settings (in cassandra.yaml)
* JVM settings (in jvm.options)
	- HEAP Sizing Options : `MAX_HEAP_SIZE` set to maximum of 8 GB : `HEAP_NEWSIZE` set to 100 MB per core (e.g. 800MB for 8 core)
	- As of Java 9, the default Garbage Collector is G1 garbage collector (), which is better than CMS for large heaps 
	- JMX Options supply hooks for monitoring java applications and services
* OS settings (kernel configuration) : ulimit : swap
* Hardware (adding or upgrading)
	- Persistence storage type : Avoid SAN storage, NAS devices, NFS : Go for SSD 
	- Memory : Recommended 16 GB to 64 GB in Production (the 8 GB is minimum)
	- CPU : 16 core CPU processors for Production	
	- Network : Recommended bandwidth is 1000 Mbit/s (Gigabit) or greater
	- Number of nodes

#### Security Considerations

* DSE Authentication and Authorization : Authentication is disabled by default, which can be enabled in dse.yaml (supports Internal, LDAP and Kerberos) : Grant or Revoke permissions to roles for Authorization
* Using SSL, Cassandra encrypts cluster communication

#### OpsCenter and LifeCycleManager

* OpsCenter : Browser based DSE cluster tool for configuring, monitoring and managing
* OpsCenter = Lifecycle Manager (LCM) + OpsCenter Monitoring
* LCM : Mostly configuration and deployment of cluster
* OpsCenter Monitoring : For monitoring and management

------
