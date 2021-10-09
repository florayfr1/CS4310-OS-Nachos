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

    private static ArrayList<CommuncatorPair> pairsList;

    public Communicator() {
        lock = new Lock();
        condSpeaker = new Condition2(lock);
        condListener = new Condition2(lock);
        pairsList = new ArrayList<>();

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
     * @param word the integer to transfer.
     */
    public void speak(int word) { //producer
        lock.acquire();
        if (pairsList.isEmpty()) {
            CommuncatorPair pair = new CommuncatorPair();
            pair.setSpeakerThread(KThread.currentThread());
            pair.setWord(word);
            pairsList.add(pair);
        } else {
            for (int i = 0; i < pairsList.size(); i++) {
                if (pairsList.get(i).speakerThread == null) {
                    pairsList.get(i).setSpeakerThread(KThread.currentThread());
                    pairsList.get(i).setWord(word);
                    break;
                }
            }
        }
        if (countListener == 0) {
            countSpeaker++;

            condSpeaker.sleep();

            condListener.wake();
            countSpeaker--;
        } else {
            condListener.wake();
        }

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
        if (pairsList.isEmpty()) {
            CommuncatorPair pair = new CommuncatorPair();
            pair.setListenerThread(KThread.currentThread());
            pairsList.add(pair);
        }else {
            for (int i = 0; i < pairsList.size(); i++) {
                if (pairsList.get(i).listenerThread == null) {
                    pairsList.get(i).setListenerThread(KThread.currentThread());
                    break;
                }
            }
        }

        if (countSpeaker != 0 && pairsList.get(0).isComplete()) {
            condSpeaker.wake();
            condListener.sleep();
        } else {
            countListener++;
            condListener.sleep();
            countListener--;
        }

        int message = pairsList.get(0).word;
        pairsList.remove(0);
        lock.release();
        return message;
    }

    private class CommuncatorPair {


        private KThread speakerThread;
        private KThread listenerThread;
        //private Condition2 condSpeaker;
        //private Condition2 condListener;
        private int word;

        public CommuncatorPair() {
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

        public boolean isComplete() {
            return (speakerThread != null && listenerThread != null);
        }

    }
}
