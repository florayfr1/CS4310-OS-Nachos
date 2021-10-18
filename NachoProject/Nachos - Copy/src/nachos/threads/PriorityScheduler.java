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
            //queueMaxPriority = -1;
            //initialise our queue
            queue = new LinkedList<>();
            /*queue = new java.util.PriorityQueue<ThreadState>(new Comparator<ThreadState>() {
                public int compare(ThreadState t1, ThreadState t2) {
                    if (t1.getEffectivePriority() < t2.getEffectivePriority()) { // t2.checkMaxPriority(queueMaxPriority)
                        return -1;
                    }
                    if (t1.getEffectivePriority() == t2.effectivePriority) {
                        return 0;
                    }
                    return 1;
                }
            });*/
        }

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

            if(queue.isEmpty())
                return null;

            ThreadState threadState = pickNextThread();
            if (threadState == null) {
                return null;
            }

            //KThread kthread = queue.poll();
            //return kthread;

            threadState.acquire(this);
            threadSResources = threadState.thread;
            queue.remove(threadSResources);
            return threadSResources;
        }

        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return the next thread that <tt>nextThread()</tt> would
         * return.
         */
         protected ThreadState pickNextThread() { //"processor"
            int maxPriority = -1;
            ThreadState result = null;

            for (ThreadState ts : queue) {
                if (ts.getEffectivePriority() > maxPriority) {
                    maxPriority = ts.getEffectivePriority();
                    result = ts;
                }
            }
            if (result == null) {
                return null;
            }
            return result;

        }



        public void print() {
            Lib.assertTrue(Machine.interrupt().disabled());
            //implement me (if you want)
            int count = 0;
            for(ThreadState ts:queue){
                System.out.println("current queue #" + count++ +  " ThreadState priority: " + ts.effectivePriority);
            }
        }

        /**
         * <tt>true</tt> if this queue should transfer priority from waiting
         * threads to the owning thread.
         */
        public boolean transferPriority;
        //public int queueMaxPriority;
        //queue that holds all the thread
        //"priority ready queue"
        //public java.util.PriorityQueue<ThreadState> queue;
        public LinkedList<ThreadState> queue;

        //know which thread is holding the resources
        public KThread threadSResources;
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

            //remember the queue from acquire and waitforacess
            //resources that we're waiting on/want
            wantQueue = new LinkedList<>(); //LinkedList of Priority Queues

            setPriority(priorityDefault);
            effectivePriority = this.priority; //might not make a difference
        }

        /*
        protected int checkMaxPriority(int queueMaxPriority) {
            int currentPriority = -1;
            if (this.getEffectivePriority() > queueMaxPriority) {
                queueMaxPriority = this.getEffectivePriority();
                currentPriority = queueMaxPriority;
            } else
            {
                currentPriority = this.priority;
            }
            return currentPriority;
        }*/

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
        public int getEffectivePriority() { //calculating new priority
            //kinda like base case
            effectivePriority = this.priority;
            for (PriorityQueue pQ : this.wantQueue) {

                if (pQ.transferPriority && pQ.queue.size() != 0) {
                    int tempNext = pQ.pickNextThread().getEffectivePriority();
                    if (effectivePriority < tempNext) {
                        effectivePriority = tempNext;
                        //pQ.print();
                    }
                }
            }
            return effectivePriority;
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

            effectivePriority = getEffectivePriority();
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

            Lib.assertTrue(Machine.interrupt().disabled());
            //queue inside the queue, we are adding the thread
            /*System.out.println("waitQueue null?" + (waitQueue == null));
            System.out.println("queue null?" + (waitQueue.queue == null));
            System.out.println("this null?" + (this == null));*/
            waitQueue.queue.add(getThreadState(thread));

            // Effective priority of whole queue should be recalculated
            effectivePriority = getEffectivePriority();
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
            Lib.assertTrue(Machine.interrupt().disabled());

            //waitQueue from parameter now becomes one of resources on which this thread waits
            waitQueue.queue.remove(getThreadState(thread));
            getThreadState(thread).wantQueue.add(waitQueue);
            //own the queue = own the lock
            waitQueue.threadSResources = thread;
        }

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


        // the queue from acquire and waitforacess
        protected LinkedList<PriorityQueue> wantQueue; //resources that we're waiting on/want "waitlist"

    }
}
