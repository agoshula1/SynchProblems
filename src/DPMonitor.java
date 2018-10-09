/**
 * Based on solution to Dining Philosophers problem given on the website
 * "Geeks for Geeks" provided by Mayank Rana:
 * https://www.geeksforgeeks.org/dining-philosophers-solution-using-monitors/
 * The main concurrency mechanisms used in this solution are monitors and
 * condition variables.
 */

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class DPMonitor{

  public enum State{
    THINKING, HUNGRY, EATING;
  }

  State[] states = new State[5];
  Object[] cvs = new Object[5];

  public DPMonitor(){
    for(int i = 0; i < 5; ++i){
      //init states and condition variables
      states[i] = State.THINKING;
      cvs[i] = new Object();
    }
  }

  private class Philosopher implements Runnable{
    private int id;
    public Philosopher(int i){
      id = i;
    }
    public void run() {
      //think
      sleep(ThreadLocalRandom.current().nextInt(0, 2));
      pickup(id);
      //eat
      sleep(ThreadLocalRandom.current().nextInt(0, 2));
      putdown(id);
    }
  }

  synchronized void pickup(int i){
    states[i] = State.HUNGRY;
    test(i);
    synchronized(cvs[i]){
      try{
        while(states[i] != State.EATING){
          cvs[i].wait();
        }
      }catch(InterruptedException e){
        throw new IllegalStateException(e);
      }
    }
  }

  synchronized void putdown(int i){
    states[i] = State.THINKING;
    test( (i + 1) % 5 ); //test right neighbour
    test( (i + 4) % 5 ); //test left neighbour
  }

  synchronized void test(int i){
    if(states[i] == State.HUNGRY
       && states[(i+1)%5] != State.EATING
       && states[(i+4)%5] != State.EATING){

         states[i] = State.EATING;
         synchronized(cvs[i]){
           cvs[i].notify();
         }
         System.out.println("Philosopher " + i + " is eating");
    }
  }

  public void simulate(int numIter){
    Thread[] threads = new Thread[5];

    Thread t;
    for(int i = 0; i < numIter; ++i){
      for(int j = 0; j < 5; j++){
        t = new Thread(new Philosopher(j));
        threads[j] = t;
        t.start();
      }

      //wait for all threads to complete
      for(int k = 0; k < 5; k++){
        try{
          threads[k].join();
        }catch (InterruptedException e){
          System.out.println("Thread " + k + " was interrupted");
        }
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
    //correctness testing
    DPMonitor dp = new DPMonitor();
    dp.simulate(1);
  }
}
