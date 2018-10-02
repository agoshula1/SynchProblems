/**
 * Based on solution to Dining Philosophers problem given on the website
 * "Geeks for Geeks" provided by Mayank Rana:
 * https://www.geeksforgeeks.org/dining-philosophers-solution-using-monitors/
 * The main concurrency mechanisms used in this solution are monitors and
 * condition variables.
 */

import java.util.concurrent.*;

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

  synchronized void pickup(int i){
    states[i] = State.HUNGRY;
    test(i);
    synchronized(cvs[i]){
      while(states[i] != State.EATING)
        try{
          cvs[i].wait();
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
    }
  }
}
