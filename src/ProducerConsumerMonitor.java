/**
 * Inspired by a "solution" to Producer-Consumer (with finite buffer) problem
 * provided J. Miller via USCViterbi:
 * http://www-scf.usc.edu/~csci201/lectures/Lecture18/ProducerConsumer.pdf
 * As in the slides above, this potential solution uses monitors and
 * condition variables as the main concurrency mechanisms.
 */
package pc;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class ProducerConsumerMonitor{
  private List<String> buffer;
  private int buffSize;
  private Object notEmpty;
  private Object notFull;

  public ProducerConsumerMonitor(int buffSize){
    buffer = new ArrayList<String>(buffSize);
    buffSize = buffSize;
    notEmpty = new Object();
    notFull = new Object();
  }

  synchronized void produce(){
    String event = ProducerConsumerUtil.waitForEvent();
    synchronized(notFull){
      synchronized(notEmpty){
        try{
          while(buffSize == buffer.size()){
            notFull.wait();
          }
          buffer.add(event);
          notEmpty.notify();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
      }
    }
  }

  synchronized void consume(){
    String event = "";
    synchronized(notFull){
      synchronized(notEmpty){
        try {
          while(buffer.isEmpty()){
            notEmpty.wait();
          }
          event = buffer.remove(0);
          notFull.notify();
          ProducerConsumerUtil.processEvent(event);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
      }
    }
  }

  public void simulate(int numThreads){
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    for(int i = 0; i < numThreads/2; ++i){
      executor.submit(this::produce);
      executor.submit(this::consume);
    }

    ProducerConsumerUtil.stop(executor);
  }

  public static void main(String[] args){
    ProducerConsumerMonitor pcm = new ProducerConsumerMonitor(5);
    pcm.simulate(20);
  }
}
