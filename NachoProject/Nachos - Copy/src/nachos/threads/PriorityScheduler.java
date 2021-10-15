package nachos.threads;

import nachos.machine.*;

import java.sql.SQLOutput;
import java.util.*;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }

    /**
     * Allocate a new priority thread queue.
     *
     * @param transferPriority <tt>true</tt> if this queue should
     *                         transfer priority from waiting threads
     *                         to the owning thread.
     * @return a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
        Lib.assertTrue(Machine.interrupt().disabled());

        Lib.assertTrue(priority >= priorityMinimum &&
                priority <= priorityMaximum);

        getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMaximum)
            return false;

        setPriority(thread, priority + 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    public boolean decreasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMinimum)
            return false;

        setPriority(thread, priority - 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;

    //TODO variable keep track of who call pickNextThread()

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param thread the thread whose scheduling state to return.
     * @return the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new ThreadState(thread);

        return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {


        PriorityQueue(boolean transferPriority) {
            this.transferPriority = transferPriority;
        }

        //TODO find out when to recalculate effective priority

        public void waitForAccess(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            getThreadState(thread).waitForAccess(this);
        }

        public void acquire(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            getThreadState(thread).acquire(this);
        }

        public KThread nextThread() {
            Lib.assertTrue(Machine.interrupt().disabled());
            // ):<
            ThreadState threadState = pickNextThread();
            if (threadState == null || queue.isEmpty()) {
                return null;
            }

            threadState.acquire(this);
            threadWithResources = threadState.thread;
            queue.remove(threadWithResources);

            return threadWithResources;
        }

        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return the next thread that <tt>nextThread()</tt> would
         * return.
         */
        protected ThreadState pickNextThread() {
            if (queue.isEmpty()) {
                return null;
            }

            KThread nextThread = queue.peek();
            return getThreadState(nextThread);

            //TODO save who call this method

            //TODO pick next Thread
        }

        public void print() {
            Lib.assertTrue(Machine.interrupt().disabled());
            System.out.println("hello world");
        }

        /**
         * <tt>true</tt> if this queue should transfer priority from waiting
         * threads to the owning thread.
         */
        public boolean transferPriority;

        //queue that holds all the thread (id)
        public java.util.PriorityQueue<KThread> queue = new java.util.PriorityQueue<KThread>(new Comparator<KThread>() {
            public int compare(KThread t1, KThread t2) {
                if (getThreadState(t1).effectivePriority < getThreadState(t2).effectivePriority) {
                    return -1;
                }
                if (getThreadState(t1).effectivePriority == getThreadState(t2).effectivePriority) {
                    return 0;
                }
                return 1;
            }
        });

        //know which thread is holding the resources
        public KThread threadWithResources;
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
        /**
         * Allocate a new <tt>ThreadState</tt> object and associate it with the
         * specified thread.
         *
         * @param thread the thread this state belongs to.
         */
        public ThreadState(KThread thread) {
            this.thread = thread;

            //TODO initialize (implement comparable)

            setPriority(priorityDefault);
        }

        /**
         * Return the priority of the associated thread.
         *
         * @return the priority of the associated thread.
         */
        public int getPriority() {
            return priority;
        }

        /**
         * Return the effective priority of the associated thread.
         *
         * @return the effective priority of the associated thread.
         */
        public int getEffectivePriority() {

            // implement me
            //TODO calculate effective priority with donated priority; save it

            // Only recalculate effective priority if required.


            // Loop through all thread resources.

            // If we have effective priority equal to maximum priority, there is no reason to continue


            // Change effective priority only if transferPriority is true,
            // there are some threads in queue and the current queue is
            // not the queue, which called this method initially.


            // Get thread state of thread with highest priority by calling pickNextThread

            // If this is not this thread and effective priority is higher donate it to this thread.

            // Do a sort of recursive call to getEffectivePriority(). It's not
            // purely recursive, because instance of thread state on which method
            // is called, is different from this thread state.

            return priority;
        }

        /**
         * Set the priority of the associated thread to the specified value.
         *
         * @param priority the new priority.
         */
        public void setPriority(int priority) {
            if (this.priority == priority)
                return;

            this.priority = priority;

            // implement me
        }

        /**
         * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
         * the associated thread) is invoked on the specified priority queue.
         * The associated thread is therefore waiting for access to the
         * resource guarded by <tt>waitQueue</tt>. This method is only called
         * if the associated thread cannot immediately obtain access.
         *
         * @param waitQueue the queue that the associated thread is
         *                  now waiting on.
         * @see nachos.threads.ThreadQueue#waitForAccess
         */
        public void waitForAccess(PriorityQueue waitQueue) {
            // implement me
            //TODO waitQueue is the resources this thread is waiting
            //TODO  remember which thread came sooner


            //TODO CHECK ON THIS, ADDING THREAD ?
            //waitQueue.queue.add(thread);
            //queue inside the queue, we are adding the thread

            //waitQueue.queue.add(thread);

            // Effective priority of whole queue should be recalculated.

        }

        /**
         * Called when the associated thread has acquired access to whatever is
         * guarded by <tt>waitQueue</tt>. This can occur either as a result of
         * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
         * <tt>thread</tt> is the associated thread), or as a result of
         * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
         *
         * @see nachos.threads.ThreadQueue#acquire
         * @see nachos.threads.ThreadQueue#nextThread
         */
        public void acquire(PriorityQueue waitQueue) {
            // implement me
            //TODO waitQueue from parameter now becomes one of resources on which this thread waits
            //ownedQueue.add(waitQueue);

            // TODO Effective priority of whole queue should be recalculated.
            recalculate = true;
            //wantQueue.recalcPriority();
        }

        //TODO class note: circle/square one incoming many outcoming

        /**
         * The thread with which this object is associated.
         */
        protected KThread thread;
        /**
         * The priority of the associated thread.
         */
        protected int priority;

        //variable save previous computed priority
        protected int effectivePriority;

        //boolean for need of recalculation
        protected boolean recalculate;

        //TODO remember the queue from acquire and waitforacess
        protected LinkedList<PriorityQueue> wantQueue; //resources that we're waiting on/want
        protected KThread ownedQueue; //resources that we have

        //TODO remember which thread came first

    }
}
