package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {

        this.conditionLock = conditionLock;
        this.waitQueue = ThreadedKernel.scheduler.newThreadQueue(true);

    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        System.out.println(conditionLock.isHeldByCurrentThread());
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());


        //All thread queue methods must be invoked with interrupts disabled.
        boolean intStatus = Machine.interrupt().disable();
        conditionLock.release();
        waitQueue.waitForAccess(KThread.currentThread());
        KThread.currentThread().sleep();
        conditionLock.acquire();
        Machine.interrupt().restore(intStatus);

    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable();
        KThread p = waitQueue.nextThread();
        if(p!= null) //if not empty
        {
            p.ready(); //place in scheduler ready queue
        }
        Machine.interrupt().restore(intStatus);

    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
        System.out.println("2 "+conditionLock.isHeldByCurrentThread());
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable();
        KThread p = waitQueue.nextThread();
        while(p != null){
            p.ready();
            p = waitQueue.nextThread();
        }
        Machine.interrupt().restore(intStatus);
    }

    private static Lock conditionLock;
    private static ThreadQueue waitQueue;
}
