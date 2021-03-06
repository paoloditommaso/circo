CIRCO change-log
================
0.3.3 - Fri, 8 Mar 2013
- Added kill command
- Enhanced tasks partitioning
- Disabled Hazelcast loadAllKeys on start
- Refactored store methods to save
- Fixed list tasks sort issue
- Fixed client print result issue
- Refactored Job finalization logic

0.3.2 - Tue, 26 Feb 2013
- Using BoneCP connection pool instead of CP30
- Refactored Hazelcast MapStore with enhanced JDBC support
- Virtual file access through persistent storage
- Enhanced job completion synchronization
- Upgraded to Groovy 2.0.7
- Upgraded to Akka 2.1.1

0.3.1 - Mon, 25 Feb 2013
- Validating context variable on submit command
- Job termination synchronization uses finder instead of list of missing items
- Tasks are any more dispatched by data-store add event
- Added files support to data store api
- Simplified context api
- Enhanced scratch folder creation

0.3.0 - Thu, 8 Feb 2013
- Big packages and naming refactoring (JobXxx -> TaskXxx)
- Improved tasks recovering on node crash
- Intercepting CTRL+C to graceful shutdown the cluster node
- Improved logging

0.2.1 - Tue, 29 Jan 2013
- Re-enabled cluster auto-join properties, since it is required to dispatch cluster event
- Handling CurrentClusterState to keep track up nodes that made-up the cluster
- JobMaster refactored to dispatchTable
- Fix issue on JobEntry#retryIsRequired method
- Trigger WorkerRequestWork on first worker creation to enable work stealing at node startup
-

0.2.0 - Sun, 27 Jan 2013
- Added job execution context feature
- Each iteration over context collection values
- Added 'context' command
- Added 'history' command + bang shell operator

0.1.5 - Fri, 25 Jan 2013
- Refactored JobId class to hold a single long value

0.1.4 - Fri, 25 Jan 2013
- Added command 'get' to retrieve command result
- Refactored XxxResponse to XxxReply
- Refactored client reply sync mechanism