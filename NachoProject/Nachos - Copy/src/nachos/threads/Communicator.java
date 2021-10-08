package nachos.threads;

import nachos.machine.*;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */

    private Lock lock;
    private int countListener;
    private int countSpeaker;
    private Condition2 condSpeaker;
    private Condition2 condListener;

    private static PriorityQueue<CommuncatorPair> pairsQueue;

    public Communicator() {
        lock = new Lock();
        condSpeaker = new Condition2(lock);
        condListener = new Condition2(lock);
        pairsQueue = new PriorityQueue<>();

        countListener = 0;
        countSpeaker = 0;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param    word    the integer to transfer.
     */
    public void speak(int word) { //producer
        lock.acquire();
        countSpeaker++;
        CommuncatorPair pair = new CommuncatorPair();
        pair.setSpeakerThread(KThread.currentThread());

        pairsQueue.add(pair);
        System.out.println("Speaker Test1: " +pair.speakerThread);

        if(countListener == 0 && !pairsQueue.peek().isComplete()){

            boolean intStatus = Machine.interrupt().disable();
            condSpeaker.sleep();
            Machine.interrupt().restore(intStatus);
        }

        pairsQueue.peek().setWord(word);

        System.out.println(word);

        boolean intStatus = Machine.interrupt().disable();
        condListener.wake();
        Machine.interrupt().restore(intStatus);

        countSpeaker--;

        lock.release();

    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */
    public int listen() {
        lock.acquire();
        countListener++;
        pairsQueue.peek().setListenerThread(KThread.currentThread()); //making it complete

        System.out.println("Listener Test1: " + KThread.currentThread());

        if(countSpeaker==0 && !pairsQueue.peek().isComplete()){

            boolean intStatus = Machine.interrupt().disable();
            condListener.sleep();
            Machine.interrupt().restore(intStatus);

        }

        boolean intStatus = Machine.interrupt().disable();
        condSpeaker.wake();
        Machine.interrupt().restore(intStatus);

        CommuncatorPair currentPair = pairsQueue.poll();
        countListener--;

        lock.release();

        return currentPair.word;
    }

    private class CommuncatorPair implements Comparable<CommuncatorPair>{


        private KThread speakerThread;
        private KThread listenerThread;
        //private Condition2 condSpeaker;
        //private Condition2 condListener;
        private int word;

        private long timeCreate;

        public CommuncatorPair() {
            timeCreate = Machine.timer().getTime();
            //condSpeaker = new Condition2(lock);
            //condListener =  new Condition2(lock);
        }

        public void setWord(int word) {
            this.word = word;
        }

        public void setSpeakerThread(KThread speakerThread) {
            this.speakerThread = speakerThread;
        }

        public void setListenerThread(KThread listenerThread) {
            this.listenerThread = listenerThread;
        }

        public boolean isComplete(){
            return (speakerThread != null && listenerThread != null);
        }

        @Override
        public int compareTo(CommuncatorPair other) {
            if (this.timeCreate < other.timeCreate) {
                return -1;
            }
            if (this.timeCreate == other.timeCreate) {
                return 0;
            }
            return 1;
        }
    }
}
