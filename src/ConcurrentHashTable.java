import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Integer;

public class ConcurrentHashTable{

  //wrapper for concurrent hash map with Integers as keys and Strings as values
  public final ConcurrentHashMap<Integer,String> hm;
  public ConcurrentHashTable(int initSize){
    hm = new ConcurrentHashMap<Integer,String>(initSize);
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

  public static void main(String[] args) throws Exception{
    final ConcurrentHashTable javaHM = new ConcurrentHashTable(20);
    List<String> inputs = new ArrayList<String>();
    javaHM.getInputsFromFile("strinputdata.txt", inputs); //get values to put into hash table

    /* Test 1 (an idealistic scenario):
     * number of threads correspond to size of table
     * number of insertions == number of retrievals
     * inserting threads launched (though not necessarily executed) before retrieving threads
     * default priority levels of threads used
     */

    List<Thread> threads = new ArrayList<Thread>();
    Thread t;
    //launch threads to insert into the table
    for(int i = 0; i < 20; ++i){
      final String v = inputs.get(i);
      final Integer k = new Integer(i);

      t = new Thread(new Runnable() {
        ConcurrentHashMap<Integer,String> hashMap = javaHM.hm;
        Integer key = k;
        String value = v;
        public void run() {
          synchronized(hashMap){
            hashMap.put(k,v);
          }
        };
      });
      threads.add(t);
      t.start();
    }

    //launch threads to retrieve values from the table
    for(int i = 0; i < 20; ++i){
      final Integer k = new Integer(i);

      t = new Thread(new Runnable() {
        ConcurrentHashMap<Integer,String> hashMap = javaHM.hm;
        Integer key = k;
        public void run() {
          String val;
          synchronized(hashMap){
            val = hashMap.get(k);
          }
          if(val == null){
            System.out.println("Retrieval with key " + key.intValue() + " into empty slot");
          }
        };
      });
      threads.add(t);
      t.start();
    }

    //wait for all threads to complete
    for(int i = 0; i < threads.size(); ++i){
      threads.get(i).join();
    }

    /* Test 2 (a more realistic scenario):
     * number of insertion threads is twice the size of table
     * number of insertions != number of retrievals
     * threads launched back-to-back
     * higher priority levels for inserting threads than retrieving threads
     *//*
    javaHM.hm.clear();
    threads.clear();
    Thread t,r;
    //launch both threads (the insertion and corresponding retrieval) closer together
    for(int i = 0; i < 40; ++i){
      final String v = inputs.get(i);
      final Integer k = new Integer(i % 20);

      t = new Thread(new Runnable() {
        ConcurrentHashMap<Integer,String> hashMap = javaHM.hm;
        Integer key = k;
        String value = v;
        int step = i;
        public void run() {
          synchronized(hashMap){
            String previous = hashMap.put(k,v);
          }
          //indicate if "second" thread to launch is overriding or initializing entry
          if(i >= 20){
            if(previous != null){
              System.out.println("Override occurred with key " + key.intValue());
            }else{
              System.out.println("First insertion with key " + key.intValue());
            }
          }
          /*if(previous != null){
            System.out.println("Override occurred with key " + key.intValue() + " (previous value string '" + previous + "'):");
          }
          System.out.println("Insertion with key " + key.intValue() + " of string '" + v + "'\n");*/
        /*};
      });
      t.setPriority(t.MAX_PRIORITY);
      threads.add(t);
      t.start();

      r = new Thread(new Runnable() {
        ConcurrentHashMap<Integer,String> hashMap = javaHM.hm;
        Integer key = k;
        String value = v;
        public void run() {
          synchronized(hashMap){
            String curr = hashMap.get(k);
          }
          if(curr != null){
            //System.out.println("Retrieval with key " + key.intValue() + " returned string '" + val + "'\n");
            if(!curr.equals(value)){
              System.out.println("Retrieval with key " + key.intValue() + " returned old data");
            }
          }else{
            System.out.println("Retrieval with key " + key.intValue() + " into empty slot\n");
          }
        };
      });
      r.setPriority(t.MIN_PRIORITY);
      threads.add(r);
      r.start();
    }

    //wait for all threads to complete
    for(int i = 0; i < threads.size(); ++i){
      threads.get(i).join();
    }
    */

    /*
     * Test 3: (testing scalability)
     */
  }
}
