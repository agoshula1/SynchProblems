import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Integer;

public class ConcurrentHashTable{

  //wrapper for concurrent hash map with Integers as keys and Strings as values
  private ConcurrentHashMap<Integer,String> hm;
  public ConcurrentHashTable(int initSize){
    hm = new ConcurrentHashMap<Integer,String>(initSize);
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
        System.out.println("Retrieval with key " + key.intValue() + " into empty slot");
      }
    }
  }

  private class RacingWrite implements Runnable{
    private Integer key;
    private int i;
    public RacingWrite(Integer k, int i){
      key = k;
      this.i = i;
    }
    public void run() {
      String val = hm.get(key);
      if(val != null){
        val = (Integer.parseInt(val) + i) + "";
      }else{
        val = i + "";
      }
      hm.put(key,val);
      System.out.println("new value is " + val);
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
        v = inputs.get(i);
        t = new Thread(new Write(k,v));
        t.setPriority(t.MAX_PRIORITY);
      }else{
        k = new Integer(i - 1);
        t = new Thread(new Read(k));
        t.setPriority(t.MIN_PRIORITY);
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

  public void test2(){
    List<Thread> threads = new ArrayList<Thread>();
    Thread t;
    Integer k = new Integer(1);

    for(int i = 0; i < 20; ++i){
      t = new Thread(new RacingWrite(k,i));
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
    System.out.println(hm.get(new Integer(1)));
  }

  public static void main(String[] args) throws Exception{
    ConcurrentHashTable javaHM = new ConcurrentHashTable(10);
    javaHM.test1(20);

    javaHM = new ConcurrentHashTable(10);
    javaHM.test2();

    /*
         * Test 3: (testing scalability)
     */
  }
}
