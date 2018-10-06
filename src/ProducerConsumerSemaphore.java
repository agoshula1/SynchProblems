/**
 * Based on solution to Producer-Consumer (with finite buffer) problem given
 * in the "Little Book of Semaphores" by Allen B. Downey. As in the book, this
 * solution uses semaphores as the main concurrency mechanism.
 */

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ProducerConsumerSemaphore{
  private Semaphore buffLock;
  private Semaphore numEvents;
  private Semaphore buffSpace;
  //buffer size limit enforced by using buffSpace semaphore
  private List<String> buffer;

  public ProducerConsumerSemaphore(int buffSize){
    buffLock = new Semaphore(1);
    numEvents = new Semaphore(0);
    buffSpace = new Semaphore(buffSize);
    buffer = new ArrayList<String>(buffSize);
  }

  private class Producer implements Runnable{
    int i;
    public Producer(int i){
      this.i = i;
    }
    public void run(){
      String event = "event" + i;
      try {
          buffSpace.acquire(); //wait if buffer full
          buffLock.acquire();
          //critical section
          buffer.add(event);
          System.out.println("Producing: " + event);
          //end critical section
      } catch (InterruptedException e) {
          throw new IllegalStateException(e);
      } finally {
          buffLock.release();
          numEvents.release();
      }
    }
  }

  private class Consumer implements Runnable{
    public void run(){
      String event = "";
      try {
          numEvents.acquire(); //wait if buffer empty
          buffLock.acquire();
          //critical section
          event = buffer.remove(0);
          System.out.println("\t\tConsuming: " + event);
          //end critical section
      } catch (InterruptedException e) {
          throw new IllegalStateException(e);
      } finally {
          buffLock.release();
          buffSpace.release();
      }
    }
  }

  public static void sleep(int seconds) {
    try {
        TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
        throw new IllegalStateException(e);
    }
  }

  public void simulate(int prodThreads, int consThreads){
    List<Thread> threads = new ArrayList<Thread>(prodThreads + consThreads);

    Thread t;
    for(int i = 0; i < prodThreads; ++i){
      t = new Thread(new Producer(i));
      threads.add(t);
      t.start();
    }
    for(int i = 0; i < consThreads; ++i){
      t = new Thread(new Consumer());
      threads.add(t);
      t.start();
    }

    //wait for all threads to complete
    for(int i = 0; i < threads.size(); i++){
      try{
        threads.get(i).join();
      }catch (InterruptedException e){
        System.out.println("Thread " + i + " was interrupted");
      }
    }
  }

  public static void main(String[] args){
    ProducerConsumerSemaphore pc = new ProducerConsumerSemaphore(5);
    pc.simulate(10,10);
  }
}
