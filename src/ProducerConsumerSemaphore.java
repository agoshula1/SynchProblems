/**
 * Based on solution to Producer-Consumer (with finite buffer) problem given
 * in the "Little Book of Semaphores" by Allen B. Downey. As in the book, this
 * solution uses semaphores as the main concurrency mechanism.
 */

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.lang.Math;

public class ProducerConsumerSemaphore{
  private Semaphore buffLock;
  private Semaphore numEvents;
  private Semaphore buffSpace;
  //buffer size limit enforced by using buffSpace semaphore
  private List<String> buffer;
  private int buffSize;

  public ProducerConsumerSemaphore(int buffSize){
    buffLock = new Semaphore(1);
    numEvents = new Semaphore(0);
    buffSpace = new Semaphore(buffSize);
    buffer = new ArrayList<String>(buffSize);
    this.buffSize = buffSize;
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
          //sleep(5);
          buffLock.acquire();
          //critical section
          buffer.add(event);
          if(buffer.size() > buffSize)
            System.out.println("ERROR: adding to full buffer");
          //System.out.println("Producing: " + event);
          //end critical section
      } catch (InterruptedException e) {
          throw new IllegalStateException(e);
      } finally {
          buffLock.release();
          //sleep(5);
          numEvents.release();
      }
    }
  }

  private class Consumer implements Runnable{
    public void run(){
      String event = "";
      try {
          numEvents.acquire(); //wait if buffer empty
          //sleep(5);
          buffLock.acquire();
          //critical section
          event = buffer.remove(0);//should throw IndexOutOfBounds if buffer empty
          //System.out.println("\t\tConsuming: " + event);
          //end critical section
      } catch (InterruptedException e) {
          throw new IllegalStateException(e);
      } finally {
          buffLock.release();
          //sleep(5);
          buffSpace.release();
      }
    }
  }

  public static void sleep(int millisec) {
    try {
        TimeUnit.MILLISECONDS.sleep(millisec);
    } catch (InterruptedException e) {
        throw new IllegalStateException(e);
    }
  }

  //launch producers before consumers
  public void test1(int prodThreads, int consThreads){
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

  //launch consumers before producers
  public void test2(int prodThreads, int consThreads){
    List<Thread> threads = new ArrayList<Thread>(prodThreads + consThreads);

    Thread t;
    for(int i = 0; i < consThreads; ++i){
      t = new Thread(new Consumer());
      threads.add(t);
      t.start();
    }
    for(int i = 0; i < prodThreads; ++i){
      t = new Thread(new Producer(i));
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

  //launch consumers and producers back-to-back
  //numThreads must be even or there will an imbalance of producers to consumers
  public void test3(int numThreads){
    List<Thread> threads = new ArrayList<Thread>(numThreads);

    Thread t;
    for(int i = 0; i < numThreads; ++i){
      if(i % 2 == 0){
        t = new Thread(new Consumer());
      } else{
        t = new Thread(new Producer(i));
      }
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
    //correctness testing
    /*for(int i = 0; i < 100; i++){

      //System.out.println("Test 1:");
      ProducerConsumerSemaphore pc = new ProducerConsumerSemaphore(50);
      pc.test1(100,100);

      //System.out.println("\nTest 2:");
      pc.test2(100,100);

      //System.out.println("\nTest 3:");
      pc.test3(200);
    }*/

    //performance testing
    ProducerConsumerSemaphore pc = new ProducerConsumerSemaphore(10);
    long t0 = System.currentTimeMillis();
    pc.test3(20);
    System.out.println("Step 1: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);

    pc = new ProducerConsumerSemaphore(100);
    t0 = System.currentTimeMillis();
    pc.test3(200);
    System.out.println("Step 2: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);

    pc = new ProducerConsumerSemaphore(1000);
    t0 = System.currentTimeMillis();
    pc.test3(2000);
    System.out.println("Step 3: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);
  }
}
