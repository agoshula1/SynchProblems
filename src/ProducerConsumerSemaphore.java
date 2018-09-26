package pc;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class ProducerConsumerSemaphore{

  public static void simulate(int numThreads, int buffSize){
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    Semaphore buffLock = new Semaphore(1);
    Semaphore numEvents = new Semaphore(0);
    Semaphore buffSpace = new Semaphore(buffSize);
    //size limit enforced by using buffSpace semaphore
    List<String> buffer = new ArrayList<String>(buffSize);

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
    simulate(20,5);
  }
}
