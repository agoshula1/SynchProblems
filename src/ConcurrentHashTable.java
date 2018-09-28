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
          hashMap.put(k,v);
          System.out.println("Insertion in thread " + key.intValue() + " of string " + v);
        };
      });
      t.start();
    }

    //lanuch threads to retrieve values from the table
    for(int i = 0; i < 20; ++i){
      final Integer k = new Integer(i);

      t = new Thread(new Runnable() {
        ConcurrentHashMap<Integer,String> hashMap = javaHM.hm;
        Integer key = k;
        public void run() {
          String val = hashMap.get(k);
          if(val != null){
            System.out.println("Retrieval in thread " + key.intValue() + " returned " + val);
          }else{
            System.out.println("Retrieval in thread " + key.intValue() + " returned nothing");
          }
        };
      });
      t.start();
    }
  }
}
