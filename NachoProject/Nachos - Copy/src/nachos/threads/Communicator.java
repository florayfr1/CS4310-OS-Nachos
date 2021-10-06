package nachos.threads;

import nachos.machine.*;

import java.util.ArrayList;
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
    private static Lock lock;
    private static Condition2 speaker;
    private static Condition2 listener;

    private static int countListener;


    private static int word;
    private static boolean isWordUpdated;

    public Communicator() {
        lock = new Lock();
        speaker = new Condition2(lock);
        listener = new Condition2(lock);
        countListener = 0;
        isWordUpdated = false;
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

        //buffer = word
        //notEmpty = listener
        //notFull = speaker
        //buf.isFull = isWordUpdated
        //buf.isEmpty = !isWordUpdated
        if (isWordUpdated || countListener == 0) {
            System.out.println(lock.isHeldByCurrentThread());
            speaker.sleep();
        }

        this.word = word;
        isWordUpdated = true;

        listener.wake();

        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */
    public int listen() { //consumer
        //buffer = wordQueue
        //notEmpty = listener
        //notFull = speaker

        lock.acquire();
        countListener++;

        if(!isWordUpdated){
            listener.sleep();
        }

        int message = word;
        isWordUpdated = false;

        speaker.wake();

        countListener--;
        lock.release();

        return message;
    }
}
