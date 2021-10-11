package nachos.threads;

import javax.swing.*;
import java.util.ArrayList;

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
    //private Condition2 condSpeaker;
    //private Condition2 condListener;


    private static ArrayList<CommunicatorPair> pairsList;

    public Communicator() {
        lock = new Lock();
        //condSpeaker = new Condition2(lock);
        //condListener = new Condition2(lock);
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
        countSpeaker++;
        if(pairsList.isEmpty() || pairsList.get(pairsList.size()-1).speakerThread != null){ //list empty or previous pair already has a speaker
            CommunicatorPair pair = new CommunicatorPair();
            pair.setSpeakerThread(KThread.currentThread());
            pair.setWord(word);
            pairsList.add(pair);
        }

        if (countListener == 0) {
            pairsList.get(pairsList.size()-1).condSpeaker.sleep();
        }

        pairsList.get(0).setSpeakerThread(KThread.currentThread());
        pairsList.get(0).setWord(word);

        pairsList.get(0).condListener.wake();
        countListener--;

        lock.release();
    }
    /* record 43
    public void speak(int word) { //producer
        lock.acquire();
        if (pairsList.isEmpty() || pairsList.get(0).speakerThread != null) {
            CommunicatorPair pair = new CommunicatorPair();
            pair.setSpeakerThread(KThread.currentThread());
            pair.setWord(word);
            pairsList.add(pair);
        } else {
            if (pairsList.get(0).listenerThread != null && pairsList.get(0).speakerThread == null){
                pairsList.get(0).setSpeakerThread(KThread.currentThread());
            }
        }
        if (countListener == 0) {
            countSpeaker++;

            condSpeaker.sleep();

            //condListener.wake();
            countSpeaker--;
        } else {
            condListener.wake();}


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

        if(pairsList.isEmpty() || pairsList.get(pairsList.size()-1).listenerThread != null){
            CommunicatorPair pair = new CommunicatorPair();
            pair.setListenerThread(KThread.currentThread());
            pairsList.add(pair);
        }

        if (countSpeaker == 0) {
            //condListener.sleep();
            pairsList.get(pairsList.size()-1).condListener.sleep();
        }

        pairsList.get(0).setListenerThread(KThread.currentThread());
        pairsList.get(0).condSpeaker.wake();
        countSpeaker--;


        int message = pairsList.get(0).word;
        pairsList.remove(0);
        lock.release();
        System.out.println(message);
        return message;
    }

    /*

    public int listen() {
        lock.acquire();
        if (pairsList.isEmpty() || pairsList.get(0).listenerThread != null) {
            CommunicatorPair pair = new CommunicatorPair();
            pair.setListenerThread(KThread.currentThread());
            pairsList.add(pair);
        } else {
            if (pairsList.get(0).speakerThread != null && pairsList.get(0).listenerThread == null){
                pairsList.get(0).setListenerThread(KThread.currentThread());
            }
        }

        if (countSpeaker != 0 && pairsList.get(0).isComplete()) {
            condSpeaker.wake();
            //condListener.sleep();
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
*/
    private class CommunicatorPair {


        private KThread speakerThread;
        private KThread listenerThread;
        private Condition2 condSpeaker;
        private Condition2 condListener;
        private int word;

        public CommunicatorPair() {
            condSpeaker = new Condition2(lock);
            condListener = new Condition2(lock);
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
