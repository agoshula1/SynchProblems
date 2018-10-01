import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class RWLBSSemaphore{
  private int readers;
  private Semaphore mutex;
  private Semaphore roomEmpty;
  private Semaphore turnstile;
  //private final List<String> sharedData;
  private String sharedData;

  public RWLBSSemaphore(int size){
    readers = 0;
    mutex = new Semaphore(1);
    roomEmpty = new Semaphore(1);
    turnstile = new Semaphore(1);
    //sharedData = new ArrayList<String>(size);
    sharedData = "";
  }

  public void simulate(int numThreads){
    List<Thread> threads = new ArrayList<Thread>(numThreads);

    Thread t,r;
    for(int i = 0; i < numThreads/2; ++i){
      final int ind = i;
      t = new Thread(new Runnable() {
        String newData = "item" + ind;
        //List<String> d = sharedData;
        boolean turnAcquired = true;
        boolean roomAcquired = true;

        public void run() {
          try{
            turnstile.acquire();
            //turnAcquired = true;
            roomEmpty.acquire();
          //  roomAcquired = true;

            //critical section
            //d.add(ind, newData);
            sharedData += newData;
            System.out.println("Write: " + newData);
            //end critical section
          } catch (InterruptedException e) {
              throw new IllegalStateException(e);
          } finally {
            if(turnAcquired)
              turnstile.release();
            if(roomAcquired)
              roomEmpty.release();
          }
        };
      });
      threads.add(t);
      t.start();

      r = new Thread(new Runnable() {
        int index = ind;
        //List<String> d = sharedData;
        boolean turnAcquired = true;
        boolean mutexAcquired = true;
        boolean roomAcquired = true;
        public void run() {
          try{
            turnstile.acquire();
            //turnAcquired = true;
            turnstile.release();
            turnAcquired = false;

            mutex.acquire();
            //mutexAcquired = true;
            ++readers;
            if(readers == 1)
              roomEmpty.acquire();
            mutex.release();

            //critical section
            System.out.println("Read: " + sharedData);
            //end critical section

            mutex.acquire();
            --readers;
            if(readers == 0){
              roomEmpty.release();
              roomAcquired = false;
            }
            mutex.release();
            mutexAcquired = false;
          } catch(InterruptedException e) {
            throw new IllegalStateException(e);
          } finally {
            if(turnAcquired)
              turnstile.release();
            if(roomAcquired)
              roomEmpty.release();
            if(mutexAcquired)
              mutex.release();
          }
        };
      });
      threads.add(r);
      r.start();

    }

    //wait for all threads to complete
    for(int i = 0; i < threads.size(); ++i){
      try{
        threads.get(i).join();
      } catch (InterruptedException e) {
          System.err.println("join interrupted");
      }
    }
  }

  public void stop(ExecutorService executor) {
    try {
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
        System.err.println("termination interrupted");
    }
    finally {
        if (!executor.isTerminated()) {
            System.err.println("killing non-finished tasks");
        }
        executor.shutdownNow();
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
    RWLBSSemaphore rw = new RWLBSSemaphore(20);
    rw.simulate(20);
  }
}
