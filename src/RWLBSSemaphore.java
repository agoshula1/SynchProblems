/**
 * Based on solution to Readers-writers (without starvation) problem given
 * in the "Little Book of Semaphores" by Allen B. Downey. As in the book, this
 * solution uses semaphores as the main concurrency mechanism.
 */

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class RWLBSSemaphore{
  private int readers;
  private Semaphore mutex;
  private Semaphore roomEmpty;
  private Semaphore turnstile;
  private String sharedData;

  public RWLBSSemaphore(){
    readers = 0;
    mutex = new Semaphore(1);
    roomEmpty = new Semaphore(1);
    turnstile = new Semaphore(1);
    sharedData = "";
  }

  private class Writer implements Runnable{
    private String data;
    public Writer(int i){
      data = "item" + i;
    }
    public void run() {
      try{
        turnstile.acquire();
        roomEmpty.acquire();

        //critical section
        //sleep(1);
        sharedData += data;
        //System.out.println("\t\tWrite: " + data);
        //end critical section
      } catch (InterruptedException e) {
          throw new IllegalStateException(e);
      } finally {
        turnstile.release();
        roomEmpty.release();
      }
    }
  }

  private class Reader implements Runnable{
    private boolean turnstile_acq;
    private boolean roomEmpty_acq;
    private boolean mutex_acq;
    public Reader(){
      turnstile_acq  = true;
      roomEmpty_acq = true;
      mutex_acq = true;
    }
    public void run() {
      try{
        turnstile.acquire();
        turnstile.release();
        turnstile_acq = false; //avoid signaling twice

        mutex.acquire();
        ++readers;
        if(readers == 1){
          roomEmpty.acquire();
        }
        mutex.release();

        //critical section
        //System.out.println("Read: " + sharedData);
        //end critical section

        mutex.acquire();
        --readers;
        if(readers == 0){
          roomEmpty.release();
        }
        roomEmpty_acq = false; //avoid signaling twice
        mutex.release();
        mutex_acq = false; //avoid signaling twice
      } catch(InterruptedException e) {
        throw new IllegalStateException(e);
      } finally {
        if(turnstile_acq)
          turnstile.release();
        if(roomEmpty_acq)
          roomEmpty.release();
        if(mutex_acq)
          mutex.release();
      }
    }
  }

  public void test(int numThreads, int inc){
    List<Thread> threads = new ArrayList<Thread>(numThreads);

    Thread t;
    for(int i = 0; i < numThreads; ++i){
      if(i % inc == 0){
        t = new Thread(new Writer(i));
      } else{
        t = new Thread(new Reader());
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

  public void sleep(int seconds) {
    try {
        TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
        throw new IllegalStateException(e);
    }
  }

  public static void main(String[] args){
    RWLBSSemaphore rw = new RWLBSSemaphore();
/*
    //Correctness testing
    //launch readers and writers back-to-back
    System.out.println("Test 1:");
    rw.test(20,2);

    rw = new RWLBSSemaphore(); //clear data
    //launch several readers, with the occasional reader (to detect starvation)
    System.out.println("\nTest 2:");
    rw.test(30,10);
*/
    //Performance testing
    long t0 = System.currentTimeMillis();
    rw.test(20,2);
    System.out.println("Step 1: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);

    rw = new RWLBSSemaphore();
    t0 = System.currentTimeMillis();
    rw.test(200,2);
    System.out.println("Step 2: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);

    rw = new RWLBSSemaphore();
    t0 = System.currentTimeMillis();
    rw.test(2000,2);
    System.out.println("Step 3: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);
  }
}
