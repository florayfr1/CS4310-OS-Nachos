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


    private int countListener;
    private int countSpeaker;

    private static PriorityQueue<CommuncatorPair> pairsQueue;
    private CommuncatorPair pair;


    public Communicator() {
        pairsQueue = new PriorityQueue<>();
        pair = new CommuncatorPair();
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
        countSpeaker++;
        pair.setSpeakerThread(KThread.currentThread());
        System.out.println("Speaker Test1: " +pair.speakerThread);

        pair.setWord(word);

        pairsQueue.add(pair);
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */
    public int listen() {

        pair.setListenerThread(KThread.currentThread());
        System.out.println("Listener Test1: " + pair.listenerThread);

        CommuncatorPair currentPair = pairsQueue.poll();
        return currentPair.word;
    }

    private class CommuncatorPair implements Comparable<CommuncatorPair>{
        private Lock lock;
        private Condition2 condSpeaker;
        private Condition2 condListener;
        private KThread speakerThread;
        private KThread listenerThread;
        private int word;

        private long timeCreate;

        public CommuncatorPair() {
            lock = new Lock();
            condSpeaker = new Condition2(lock);
            condListener = new Condition2(lock);
            timeCreate = Machine.timer().getTime();
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
