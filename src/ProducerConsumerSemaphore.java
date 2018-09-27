/**
 * Based on solution to Producer-Consumer (with finite buffer) problem given
 * in the "Little Book of Semaphores" by Allen B. Downey. As in the book, this
 * solution uses semaphores as the main concurrency mechanism.
 */

package pc;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class ProducerConsumerSemaphore{
  private Semaphore buffLock;
  private Semaphore numEvents;
  private Semaphore buffSpace;
  //buffer size limit enforced by using buffSpace semaphore
  private List<String> buffer;

  public ProducerConsumerSemaphore(int buffSize){
    Semaphore buffLock = new Semaphore(1);
    Semaphore numEvents = new Semaphore(0);
    Semaphore buffSpace = new Semaphore(buffSize);
    buffer = new ArrayList<String>(buffSize);
  }

  public void simulate(int numThreads){
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    Runnable producer = () -> {
        String event = ProducerConsumerUtil.waitForEvent();
        try {
            buffSpace.acquire(); //wait if buffer full
            buffLock.acquire();
            //critical section
            buffer.add(event);
            //end critical section
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            buffLock.release();
            numEvents.release();
        }
    };

    Runnable consumer = () -> {
        String event = "";
        try {
            numEvents.acquire(); //wait if buffer empty
            buffLock.acquire();
            //critical section
            event = buffer.remove(0);
            //end critical section
            ProducerConsumerUtil.processEvent(event);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            buffLock.release();
            buffSpace.release();
        }
    };

    for(int i = 0; i < numThreads/2; ++i){
      executor.submit(producer);
      executor.submit(consumer);
    }

    ProducerConsumerUtil.stop(executor);
  }

  public static void main(String[] args){
    ProducerConsumerSemaphore pc = new ProducerConsumerSemaphore(5);
    pc.simulate(20);
  }
}
