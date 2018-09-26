package pc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ProducerConsumerUtil{
  //simulate processing event
  public static void processEvent(String event){
    sleep(1);
    System.out.println(event);
  }

  //simulate waiting for an event
  public static String waitForEvent(){
    sleep(1);
    return "1"; //represents data result from event
  }

  public static void stop(ExecutorService executor) {
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

  public static void sleep(int seconds) {
    try {
        TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
        throw new IllegalStateException(e);
    }
  }
}
