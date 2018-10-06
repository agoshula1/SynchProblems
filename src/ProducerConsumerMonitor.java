/**
 * Based on a "solution" to Producer-Consumer (with finite buffer) problem
 * provided by J. Miller via USCViterbi:
 * http://www-scf.usc.edu/~csci201/lectures/Lecture18/ProducerConsumer.pdf
 * As in the slides above, this potential solution uses monitors and
 * condition variables as the main concurrency mechanisms.
 */

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
//import java.lang.Math;
import java.util.concurrent.TimeUnit;

public class ProducerConsumerMonitor{
  private List<String> buffer;
  private int buffSize;
  private Object notEmpty;
  private Object notFull;

  public ProducerConsumerMonitor(int buffSize){
    buffer = new ArrayList<String>(buffSize);
    this.buffSize = buffSize;
    notEmpty = new Object();
    notFull = new Object();
  }

  private class ProducerTask implements Runnable{
    int i;
    public ProducerTask(int i){
      this.i = i;
    }
    public void run(){
      String event = "event" + i;
      synchronized(notFull){
        synchronized(notEmpty){
          try{
            while(buffSize == buffer.size()){
              notFull.wait();
            }
            buffer.add(event);
            System.out.println("Producer writes: " + i);
            notEmpty.notify();
          } catch (InterruptedException e) {
              throw new IllegalStateException(e);
          }
        }
      }
    }
  }

  private class ConsumerTask implements Runnable {
    public void run(){
      String event = "";
      synchronized(notFull){
        synchronized(notEmpty){
          try {
            while(buffer.isEmpty()){
              notEmpty.wait();
            }
            event = buffer.remove(0);
            System.out.println("\t\tConsumer reads: " + event);
            notFull.notify();
          } catch (InterruptedException e) {
              throw new IllegalStateException(e);
          }
        }
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

  public void simulate(int numThreads){
    List<Thread> threads = new ArrayList<Thread>(numThreads);
    Thread t,r;
    for(int i = 0; i < numThreads/2; ++i){
      t = new Thread(new ProducerTask(i));
      threads.add(t);
      r = new Thread(new ConsumerTask());
      threads.add(r);
      t.start();
      r.start();
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
    ProducerConsumerMonitor pcm = new ProducerConsumerMonitor(5);
    pcm.simulate(20);
  }
}
