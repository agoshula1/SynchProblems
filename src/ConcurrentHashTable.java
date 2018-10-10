/*
 * Using implementation of concurrent hash table provided in concurrent package:
 * https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html
 */

import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Integer;

public class ConcurrentHashTable{

  //wrapper for concurrent hash map with Integers as keys and Strings as values
  private ConcurrentHashMap<Integer,String> hm;
  private Semaphore mutex;
  public ConcurrentHashTable(int initSize){
    hm = new ConcurrentHashMap<Integer,String>(initSize);
    mutex = new Semaphore(1);
  }

  private class Write implements Runnable{
    private Integer key;
    private String value;
    public Write(Integer k, String v){
      key = k;
      value = v;
    }
    public void run() {
      hm.put(key,value);
    }
  }

  private class Read implements Runnable{
    private Integer key;
    public Read(Integer k){
      key = k;
    }
    public void run() {
      String val = hm.get(key);
      if(val == null){
        //System.out.println("Retrieval with key " + key.intValue() + " into empty slot");
      }
    }
  }

  public void getInputsFromFile(String file, List<String> strs) throws Exception {
		BufferedReader reader = new BufferedReader( new FileReader (file));
    String line;
    while( ( line = reader.readLine() ) != null ) {
    	String[] words = line.split(" ");
    	for(int i = 0; i < words.length; ++i){
		      strs.add(words[i]);
	    }
    }
    reader.close();
	}

  public void test1(int numThreads) throws Exception{
    List<String> inputs = new ArrayList<String>();
    getInputsFromFile("strinputdata.txt", inputs); //get values to put into hash table

    List<Thread> threads = new ArrayList<Thread>(numThreads);
    Thread t;
    Integer k;
    String v;
    //launch reading and writing threads back-to-back
    //set higher priority for writing, lower for reading
    for(int i = 0; i < numThreads; ++i){
      if(i % 2 == 0){
        k = new Integer(i);
        t = new Thread(new Read(k));
        t.setPriority(t.MIN_PRIORITY);
      }else{
        k = new Integer(i - 1);
        v = inputs.get(i);
        t = new Thread(new Write(k,v));
        t.setPriority(t.MAX_PRIORITY);
      }
      threads.add(t);
      t.start();
    }

    //wait for all threads to complete
    for(int i = 0; i < threads.size(); ++i){
      try{
        threads.get(i).join();
      }catch (InterruptedException e){
        System.out.println("Thread " + i + " was interrupted");
      }
    }
  }

  public static void main(String[] args) throws Exception{
    //correctness testing
    ConcurrentHashTable javaHM = new ConcurrentHashTable(10);
    //javaHM.test1(20);

    //performance testing
    //javaHM = new ConcurrentHashTable(10);
    long t0 = System.currentTimeMillis();
    javaHM.test1(20);
    System.out.println("Step 1: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);

    javaHM = new ConcurrentHashTable(100);
    t0 = System.currentTimeMillis();
    javaHM.test1(200);
    System.out.println("Step 2: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);

    javaHM = new ConcurrentHashTable(1000);
    t0 = System.currentTimeMillis();
    javaHM.test1(2000);
    System.out.println("Step 3: Time elapsed (sec) = " + (System.currentTimeMillis() - t0)/1000.0);
  }
}
