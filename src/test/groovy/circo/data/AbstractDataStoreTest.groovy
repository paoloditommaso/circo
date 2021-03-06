package circo.data
import circo.model.Job
import circo.model.JobStatus
import circo.model.NodeData
import circo.model.NodeStatus
import circo.model.TaskEntry
import circo.model.TaskId
import circo.model.TaskReq
import circo.model.TaskResult
import circo.model.TaskStatus
import circo.model.WorkerRefMock
import spock.lang.Shared
import spock.lang.Specification
import test.TestHelper

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
abstract class AbstractDataStoreTest extends Specification {

    @Shared
    DataStore store


    // ------------------ JOB operations tests ---------------------

    def 'test getJob and putJob' () {

        setup:
        final id = UUID.randomUUID()
        def job = new Job(id)
        job.status = JobStatus.PENDING

        when:
        store.saveJob(job)

        then:
        store.getJob(id) == job
        store.getJob(UUID.randomUUID()) == null

    }

    def 'test updateJob' () {

        setup:
        def job1 = new Job(UUID.randomUUID())
        job1.status = JobStatus.RUNNING
        store.saveJob(job1)

        def job2 = new Job(UUID.randomUUID())
        job2.status = JobStatus.ERROR
        store.saveJob(job2)

        when:
        boolean done1 = store.updateJob( job1.requestId ) { Job it -> it.status = JobStatus.PENDING }
        boolean done2 = store.updateJob( job2.requestId ) { Job it -> it.status = JobStatus.ERROR }
        boolean done3 = store.updateJob( UUID.randomUUID() ) { Job it -> it.status = JobStatus.ERROR }

        then:
        done1  // this must be TRUE
        !done2  // this must be FALSE, because the object does not change, so it is not updated
        !done3 // this must be FALSE, because the entry does not exist

        store.getJob(job1.requestId).submitted

    }


    def 'test listJobs' () {

        setup:
        store.saveJob( new Job( UUID.randomUUID() ) )
        store.saveJob( new Job( UUID.randomUUID() ) )
        store.saveJob( new Job( UUID.randomUUID() ) )
        store.saveJob( new Job( UUID.randomUUID() ) )

        when:
        def list = store.listJobs()

        then:
        list.size() == 4

    }


    def 'test findJobsByStatus' () {

        setup:
        def job1 = Job.create { Job it -> it.status = JobStatus.PENDING }
        def job2 = Job.create { Job it -> it.status = JobStatus.RUNNING }
        def job3 = Job.create { Job it -> it.status = JobStatus.RUNNING }
        def job4 = Job.create { Job it -> it.status = JobStatus.RUNNING }

        store.saveJob(job1)
        store.saveJob(job2)
        store.saveJob(job3)
        store.saveJob(job4)

        expect:
        store.findJobsByStatus( JobStatus.PENDING ) == [job1]
        store.findJobsByStatus( JobStatus.RUNNING ) as Set == [job2,job3,job4] as Set
        store.findJobsByStatus( JobStatus.ERROR ) == []
        store.findJobsByStatus( JobStatus.PENDING, JobStatus.RUNNING) as Set == [job1,job2,job3,job4] as Set

    }


    def 'test findJobsByRequestId' () {

        setup:
        def id1 = UUID.fromString('550e8400-e29b-41d4-a716-446655440000')
        def id2 = UUID.fromString('550e8400-e29b-41d4-a716-446655440009')
        def id3 = UUID.randomUUID()
        def id4 = UUID.randomUUID()


        def job1 = Job.create(id1)
        def job2 = Job.create(id2)
        def job3 = Job.create(id3)
        def job4 = Job.create(id4)

        store.saveJob(job1)
        store.saveJob(job2)
        store.saveJob(job3)
        store.saveJob(job4)

        expect:
        store.findJobsByRequestId( id1.toString() ) == [job1]
        store.findJobsByRequestId( '550e8400' ) as Set == [job1,job2] as Set
        store.findJobsByRequestId( '550e8400-*' ) as Set == [job1,job2] as Set
        store.findJobsByRequestId( '550e8400-e29b-41d4-a716-44665544000?' ) as Set == [job1,job2] as Set
        store.findJobsByRequestId( '450e8400-e29b-41d4-a716-44665544000?' ) == []
        store.findJobsByRequestId( '*446655440000' ) == [job1]


    }





    // ------------------------ TASK operations tests ---------------------------------------

    def 'test nextTaskId' () {

        when:
        TaskId first = store.nextTaskId()
        TaskId second = store.nextTaskId()
        TaskId third = store.nextTaskId()

        then:
        first != second
        second != third
        first.value +1 == second.value
        second.value +1 == third.value

    }

    def 'test getTask and storeTask'( ) {

        when:
        def id = TaskId.of(1)
        def entry = new TaskEntry( id, new TaskReq(script: 'Hola') )
        store.saveTask(entry)

        then:
        entry == store.getTask(id)
        entry == store.getTask(TaskId.of(1))
        null == store.getTask( TaskId.of(321) )

    }

    def 'test getTask' () {

        setup:
        def id0 = TaskId.of('123')
        def id1 = TaskId.of('111')
        def id2 = TaskId.of(222)

        store.saveTask( TaskEntry.create(id1) { it.req.script = 'script1' } )
        store.saveTask( TaskEntry.create(id2) { it.req.script = 'script2' } )

        expect:
        store.getTask(id0) == null
        store.getTask(TaskId.of('111')).req.script == 'script1'
        store.getTask(TaskId.of(222)).req.script == 'script2'

    }

    def 'test listTasks' () {

        setup:
        def task1 = TaskEntry.create('1') { it.status = TaskStatus.NEW }
        def task2 = TaskEntry.create('2') { it.status = TaskStatus.PENDING }
        def task3 = TaskEntry.create('3') { it.status = TaskStatus.PENDING }
        def task4 = TaskEntry.create('4') { it.status = TaskStatus.TERMINATED }
        def task5 = TaskEntry.create('5') { it.status = TaskStatus.TERMINATED }
        def task6 = TaskEntry.create('6') { it.status = TaskStatus.TERMINATED }

        store.saveTask(task1)
        store.saveTask(task2)
        store.saveTask(task3)
        store.saveTask(task4)
        store.saveTask(task5)
        store.saveTask(task6)

        def task7 = TaskEntry.create('7') { it.status = TaskStatus.TERMINATED }


        when:
        def list = store.listTasks()

        then:
        list.size() == 6
        list.contains(task1)
        list.contains(task2)
        list.contains(task3)
        list.contains(task4)
        list.contains(task5)
        list.contains(task6)
        !list.contains(task7)
    }


    def 'test findTasksByStatus' () {

        setup:
        def task1 = TaskEntry.create('1') { TaskEntry it-> it.status = TaskStatus.NEW }
        def task2 = TaskEntry.create('2') { TaskEntry it-> it.status = TaskStatus.PENDING }
        def task3 = TaskEntry.create('3') { TaskEntry it-> it.status = TaskStatus.PENDING }
        def task4 = TaskEntry.create('4') { TaskEntry it-> it.status = TaskStatus.TERMINATED }
        def task5 = TaskEntry.create('5') { TaskEntry it-> it.status = TaskStatus.TERMINATED }
        def task6 = TaskEntry.create('6') { TaskEntry it-> it.status = TaskStatus.TERMINATED }

        store.saveTask(task1)
        store.saveTask(task2)
        store.saveTask(task3)
        store.saveTask(task4)
        store.saveTask(task5)
        store.saveTask(task6)

        expect:
        store.findTasksByStatus(TaskStatus.NEW).toSet() == [task1] as Set
        store.findTasksByStatus(TaskStatus.PENDING).toSet() == [task2,task3] as Set
        store.findTasksByStatus(TaskStatus.TERMINATED).toSet() == [task4,task5,task6] as Set
        store.findTasksByStatus(TaskStatus.READY) == []
        store.findTasksByStatus(TaskStatus.NEW, TaskStatus.PENDING).toSet() == [task1,task2,task3] as Set
    }

    def 'test findTasksByStatusString' () {

        setup:
        def task1 = TaskEntry.create('1') { TaskEntry it-> it.status = TaskStatus.NEW }
        def task2 = TaskEntry.create('2') { TaskEntry it-> it.status = TaskStatus.PENDING }
        def task3 = TaskEntry.create('3') { TaskEntry it-> it.status = TaskStatus.PENDING }
        def task4 = TaskEntry.create('4') { TaskEntry it-> it.status = TaskStatus.TERMINATED; it.result = new TaskResult() }
        def task5 = TaskEntry.create('5') { TaskEntry it-> it.status = TaskStatus.TERMINATED; it.result = new TaskResult() }
        def task6 = TaskEntry.create('6') { TaskEntry it-> it.status = TaskStatus.TERMINATED; it.result = new TaskResult(exitCode: 0) }
        def task7 = TaskEntry.create('7') { TaskEntry it-> it.status = TaskStatus.TERMINATED; it.result = new TaskResult(cancelled: true) }
        def task8 = TaskEntry.create('8') { TaskEntry it-> it.killed = true }

        store.saveTask(task1)
        store.saveTask(task2)
        store.saveTask(task3)
        store.saveTask(task4)
        store.saveTask(task5)
        store.saveTask(task6)
        store.saveTask(task7)
        store.saveTask(task8)

        expect:
        store.findTasksByStatusString('new').toSet() == [task1] as Set
        store.findTasksByStatusString('pending').toSet() == [task2,task3] as Set
        store.findTasksByStatusString( 'success' ).toSet() == [task6] as Set
        store.findTasksByStatusString( 'error' ).toSet() == [task4,task5] as Set
        store.findTasksByStatusString( 'cancelled' ).toSet() == [task7] as Set
        store.findTasksByStatusString( 'killed' ).toSet() == [task8] as Set
    }

    def 'test findTasksByRequestId' () {
        setup:
        def req1 = UUID.randomUUID()
        def req2 = UUID.randomUUID()

        def task1 = TaskEntry.create('1') { TaskEntry it -> it.status = TaskStatus.NEW; it.req.requestId = req1 }
        def task2 = TaskEntry.create('2') { TaskEntry it -> it.status = TaskStatus.PENDING; it.req.requestId = req1  }
        def task3 = TaskEntry.create('3') { TaskEntry it -> it.status = TaskStatus.PENDING; it.req.requestId = req2  }
        def task4 = TaskEntry.create('4') { TaskEntry it -> it.status = TaskStatus.TERMINATED; it.req.requestId = req2   }
        def task5 = TaskEntry.create('5') { TaskEntry it -> it.status = TaskStatus.TERMINATED; it.req.requestId = req2   }
        def task6 = TaskEntry.create('6') { TaskEntry it -> it.status = TaskStatus.TERMINATED  }

        store.saveTask(task1)
        store.saveTask(task2)
        store.saveTask(task3)
        store.saveTask(task4)
        store.saveTask(task5)
        store.saveTask(task6)

        expect:
        store.findTasksByRequestId( req1 ).toSet() == [task1,task2] as Set
        store.findTasksByRequestId( req2 ).toSet() == [task3,task4,task5] as Set

    }



    def 'test findTasksByOwnerId' () {

        setup:

        def task1 = TaskEntry.create(1) { TaskEntry it -> it.ownerId = 1 }
        def task2 = TaskEntry.create(2) { TaskEntry it -> it.ownerId = 2 }
        def task3 = TaskEntry.create(3) { TaskEntry it -> it.ownerId = 2 }
        def task4 = TaskEntry.create(4) { TaskEntry it -> it.ownerId = 2 }
        def task5 = TaskEntry.create(5)
        store.saveTask(task1)
        store.saveTask(task2)
        store.saveTask(task3)
        store.saveTask(task4)
        store.saveTask(task5)

        expect:
        store.findTasksByOwnerId(1).toSet() == [task1] as Set
        store.findTasksByOwnerId(2).toSet() == [task2, task3, task4] as Set
        store.findTasksByOwnerId(99) == []

    }


    // ---------------- NODE tests operations ----------------------------------

    def 'test nextNodeId' () {

        when:
        int first = store.nextNodeId()
        int second = store.nextNodeId()
        int third = store.nextNodeId()

        then:
        first != second
        second != third
        first +1 == second
        second +1 == third

    }


    def "test getAndPutNodeData" () {
        setup:
        def nodeInfo = new NodeData( id: 99, processed: 7843 )
        nodeInfo.createWorkerData( new WorkerRefMock('worker1') )
        nodeInfo.createWorkerData( new WorkerRefMock('worker2') )

        when:
        store.saveNode(nodeInfo)


        then:
        store.getNode(99) == nodeInfo
        store.getNode(77) == null

    }



    def 'test removeNodeData' () {


        setup:
        def node1 = new NodeData( id: 1, processed: 7843 )
        def node2 = new NodeData( id: 2, processed: 343 )

        store.saveNode(node1)
        store.saveNode(node2)

        def node3 = new NodeData( id: 3, processed: 8593 )

        when:
        def result1 = store.removeNode( node1 )
        def result2 = store.removeNode( node3 )

        then:
        result1
        !result2
        store.getNode( 1 ) == null
        store.getNode( 2 ) == node2

    }

    def 'test listNodes' () {

        setup:
        def node1 = new NodeData( id: 1, processed: 7843 )
        def node2 = new NodeData( id: 2, processed: 343 )
        def node3 = new NodeData( id: 3, processed: 8593 )

        store.saveNode(node1)
        store.saveNode(node2)
        store.saveNode(node3)

        when:
        def list = store.listNodes()

        then:
        list.size() == 3
        list.toSet() ==  [ node1, node2, node3 ] as Set

    }

   def 'test findNodeDataByAddress' () {

       setup:
       def addr1 = TestHelper.randomAddress()
       def addr2 = TestHelper.randomAddress()
       def addr3 = TestHelper.randomAddress()

       def node1 = new NodeData( id: 1, processed: 7843, address: addr1 )
       def node2 = new NodeData( id: 2, processed: 343, address: addr2 )
       def node3 = new NodeData( id: 3, processed: 8593, address: addr3 )
       def node4 = new NodeData( id: 4, processed: 59054, address: addr3 )

       store.saveNode( node1 )
       store.saveNode( node2 )
       store.saveNode( node3 )
       store.saveNode( node4 )

       expect:
       store.findNodesByAddress( addr1 ) == [node1]
       store.findNodesByAddress( addr2 ) == [node2]
       store.findNodesByAddress( addr3 ).toSet() == [ node3, node4 ] as Set

   }


    def 'test findNodeDataByAddressAndStatus' () {

        setup:
        def addr1 = TestHelper.randomAddress()
        def addr2 = TestHelper.randomAddress()
        def addr3 = TestHelper.randomAddress()

        def node1 = new NodeData( id: 1, processed: 7843, address: addr1 )
        def node2 = new NodeData( id: 2, processed: 343, address: addr2, status: NodeStatus.PAUSED )
        def node3 = new NodeData( id: 3, processed: 8593, address: addr3, status: NodeStatus.DEAD )
        def node4 = new NodeData( id: 4, processed: 8593, address: addr3, status: NodeStatus.ALIVE )

        store.saveNode( node1 )
        store.saveNode( node2 )
        store.saveNode( node3 )
        store.saveNode( node4 )

        expect:
        store.findNodesByAddressAndStatus( addr1, NodeStatus.ALIVE ) == []
        store.findNodesByAddressAndStatus( addr2, NodeStatus.PAUSED ) == [node2]
        store.findNodesByAddressAndStatus( addr3, NodeStatus.ALIVE ).toSet() == [ node4 ] as Set

    }


//    // ---------------- tasks queue operations
//
//    def 'test appendToQueue and takeFromQueue and isEmptyQueue' () {
//
//        setup:
//        def wasEmpty = store.isEmptyQueue()
//        store.appendToQueue( TaskId.of(1) )
//        store.appendToQueue( TaskId.of(2) )
//        store.appendToQueue( TaskId.of(3) )
//
//        when:
//
//        Set<TaskId> set = []
//        def count = 0
//        while( !store.isEmptyQueue() ) {
//            set << store.takeFromQueue()
//            count++
//        }
//
//        then:
//        wasEmpty
//        set.size() == 3
//        set == [ TaskId.of(1),  TaskId.of(2),  TaskId.of(3) ] as Set
//        count == 3
//
//    }


    def 'test get and put file' () {

        setup:
        def str = """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse eu velit felis. Nullam fringilla interdum ipsum,
        at accumsan mauris cursus non. Sed et felis et nisl viverra dignissim vel ut nulla. Sed ultricies, turpis et
        sollicitudin faucibus, nibh eros dignissim lacus, non fringilla dui erat quis nisi.

        Sed turpis mi, elementum ut sollicitudin iaculis, mattis non nunc. Phasellus at leo eu tellus auctor convallis.
        Aenean ipsum diam, feugiat vitae ullamcorper mattis, porttitor eu neque. Suspendisse faucibus, massa ut tincidunt
        vestibulum, felis ligula iaculis sapien, quis imperdiet diam tellus sed lacus. Nulla aliquet ullamcorper quam,
        vitae consequat sem mollis non.

        Nunc mattis turpis nec eros lobortis at condimentum diam consequat. Nullam fermentum scelerisque sodales. Curabitur ac
        magna odio, nec sagittis lectus. Praesent at leo eget libero vestibulum elementum id non elit.
        """
        .stripIndent()
        def sourceFile = File.createTempFile('test',null)
        sourceFile.deleteOnExit()
        sourceFile.text = str

        //
        // we put a file in the cache
        //
        when:
        def fileId = UUID.randomUUID()
        store.saveFile(fileId, sourceFile)

        then:
        store.getFile(fileId) == sourceFile
        store.getFile(fileId).text == str

        //
        // test against different file name
        //
        where:
        fileName << ['simpleFile.txt', '/root.file', '/some/path/file.txt']

    }


    // ------------------------ TEST SINK ----------------------------------------

    def 'test put task sink ' () {

        setup:
        def req1 = UUID.randomUUID()
        def req2 = UUID.randomUUID()
        def task1 = TaskEntry.create(1) { TaskEntry it -> it.req.requestId = req1 }
        def task2 = TaskEntry.create(2) { TaskEntry it -> it.req.requestId = req2 }
        def task3 = TaskEntry.create(3) { TaskEntry it -> it.req.requestId = req2 }

        when:
        store.addToSink( task1 )
        store.addToSink( task2 )
        store.addToSink( task3 )
        // note: when the element is already store, following stores are skipped
        store.addToSink( task3 )
        store.addToSink( task3 )

        then:
        store.countTasksMissing(req1) == 1
        store.countTasksMissing(req2) == 2
        store.countTasksMissing(UUID.randomUUID()) == 0

    }



}
