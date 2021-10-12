package nachos.threads;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;

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
    private Condition2 condSpeaker;
    private Condition2 condListener;

    private static LinkedList<Message> speakerMessageList;
    private static LinkedList<Message> listenerMessageList;

    public Communicator() {
        lock = new Lock();
        speakerMessageList = new LinkedList<>();
        listenerMessageList = new LinkedList<>();
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
    public void speak(int word) {
        lock.acquire();

        if(listenerMessageList.size()==0){
            Message wordMessage = new Message();
            wordMessage.setWord(word);
            speakerMessageList.add(wordMessage);
            wordMessage.cond.sleep();
        } else{
            listenerMessageList.getFirst().cond.wake();
            if(!listenerMessageList.isEmpty())
                listenerMessageList.removeFirst();
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

        int word = 0;
        if(speakerMessageList.size()==0){
            Message wordMessage = new Message();
            listenerMessageList.add(wordMessage);
            word = wordMessage.word;
            wordMessage.cond.sleep();
        } else{
            word = speakerMessageList.getFirst().word;
            speakerMessageList.getFirst().cond.wake();
            if(!speakerMessageList.isEmpty())
                speakerMessageList.removeFirst();
        }

        lock.release();
        return word;
    }

    private class Message {
        int word;
        Condition2 cond;

        public Message() {
            this.word = 0;
            cond = new Condition2(lock);
        }

        public void setWord(int word) {
            this.word = word;
        }
    }
}
