package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Hashtable;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.telephony.TelephonyManager;

public class SimpleDynamoProvider extends ContentProvider {

	public static String node1="5556";
	public static String node2="5554";
	public static String node3="5558";
	
	public static int node1_portno=11112;
	public static int node2_portno=11108;
	public static int node3_portno=11116;
	public static int my_portno;
	
	public static int first_succ_portno;
	public static int second_succ_portno;
	public static int first_pred_portno;
	
	public static String node1_hash;
	public static String node2_hash;
	public static String node3_hash;
	public static String my_hash;
	
	public static Context context;	
	TelephonyManager tel;
	
	
	public static boolean ack_rxd=false;
	public static boolean query_ack= false;
	public static boolean get_replica_ack_rxd=false;
	public static int votes=0;
	
	public static MatrixCursor result;
	public static MatrixCursor query_result;
	
	public static Hashtable<String , String> KeyValueStore= new Hashtable<String ,String>();
    public static Hashtable<String , Integer>CountStore= new Hashtable<String ,Integer>();
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	///////////////////////////////////// onCreate Method   ///////////////////////////////////////////////
	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		
		new Server().start();
		try {
			node1_hash=genHash(node1);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Error in node1 hash calculation");
		}
		try {
			node2_hash=genHash(node2);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Error in node2 hash calculation");
		}
		try {
			node3_hash=genHash(node3);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Error in node3 hash calculation");
		}
		
		context=this.getContext();
        tel=(TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String  portStr=tel.getLine1Number().substring(tel.getLine1Number().length()-4);
        
        if(portStr.equals("5554")) {
        	try {
				my_hash=genHash("5554");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("error in my_hash calculation : lineno 99");
			}
        	my_portno=11108;
        	first_succ_portno=11116;
        	second_succ_portno=11112;
        	first_pred_portno=11112;
        } else if(portStr.equals("5556")) {
        	try {
				my_hash=genHash("5556");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("error in my_hash calculation : lineno 108");
			}
        	my_portno=11112;
        	first_succ_portno=11108;
        	second_succ_portno=11116;
        	first_pred_portno=11116;
        }else if(portStr.equals("5558")) {
        	try {
				my_hash=genHash("5558");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("error in my_hash calculation : lineno 117");
			}
        	my_portno=11116;
        	first_succ_portno=11112;
        	second_succ_portno=11108;
        	first_pred_portno=11108;
        }
        
        String get_replicas= "get_replicas"+":"+my_portno;
        //forward(get_replicas, first_pred_portno);
        System.out.println("forwarded get_replica message");
        
        String get_missed= "get_missed"+":"+my_portno;
        forward(get_missed, first_succ_portno);
        System.out.println("forwarded get_missed message");
        
        
		return false;
	}// end of onCreate method...


	///////////////////////////////////// Insert Method   /////////////////////////////////////////////
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		String key= values.getAsString("key");
    	String value= values.getAsString("value");
    	System.out.println(key+":"+value);
    	
    	ack_rxd=false;
    	ContentValues preference_list=getPreferenceList(key);
    	System.out.println("Preffered node1 :"+preference_list.getAsInteger("1"));
    	System.out.println("Preffered node2 :"+preference_list.getAsInteger("2"));
    	
    	String insert_message= "insert"+":"+my_portno+":"+key+":"+value;
    	forward(insert_message, preference_list.getAsInteger("1"));
    	
    	long time= System.currentTimeMillis();
    	while(true) {
    		if (ack_rxd || System.currentTimeMillis()==time+300) break;
    	}
    	// if ack is not rxd withing specified amount of time send it to the next node...
    	System.out.println("the value of ack_rxd is "+ack_rxd);
    	if(ack_rxd==false) {
    		System.out.println("forwarding to second node in preference list");
    		forward(insert_message, preference_list.getAsInteger("2"));
    	}
    	ack_rxd=false;
		return null;
	}

	
	
	////////////////////////////////////////////// Query Method  //////////////////////////////////////////////////
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		
		// ldump case...........
				if(selection.equals("ldump")){
					String [] coloumns= {"key", "value"};
					result= new MatrixCursor(coloumns);
					
					Enumeration<String> en= KeyValueStore.keys();
					
					while(en.hasMoreElements()){
						String key=en.nextElement();
						String value=KeyValueStore.get(key);
						
						String [] str= {key, value};
						result.addRow(str);
					}
					
		}// end of ldump case....
				
		else {
			// query from the co-ordinator...
			String key= selection;
			ContentValues  conv= getPreferenceList(key);
			int remote_portno1= conv.getAsInteger("1");
		    
			String query_message= "query"+":"+my_portno+":"+key;
			forward(query_message, remote_portno1);
			query_ack=false;
			long time= System.currentTimeMillis();
			while(true){
				if (query_ack /*|| (System.currentTimeMillis()==time+700)*/ ) break;
			}
			//
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			
			System.out.println("query ack rxd.........returning the result cursor....");
			result=null;
			result= query_result;
			//System.out.println();
			query_result=null;
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////
			
		}
		return result;
	}

	////////////////////////////////// End of Query Method    //////////////////////////////////////////////////////
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	/* Helper functions
	 * 
	 *   These functions are used 
	 *   in various parts of   
	 *   the ProviderClass
	 *   1) genHash : to calculate hash
	 *   2) getPreferenceList : to get the preference list of given key
	 *   3) forward method : method to forward the message
	 *   4) insert_handler : which handles incoming insert messages
	 *   5) insert_ack_handler : which handles incoming ack message to a request message
	 *   6) request handler : which handles request message...
	 *   
	 *   */
	
    private static String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    
    
    public static ContentValues getPreferenceList (String key) {
    	ContentValues cv= new ContentValues();
    	String key_hash=null;
    	try {
		  key_hash= genHash(key);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Error in key hash calculation : line number 104: Provider class");
		}
    	
    	if( key_hash.compareTo(node1_hash)>0 && key_hash.compareTo(node2_hash)< 0 ){
    		cv.put("1", /*node2*/ 11108);
    		cv.put("2", /*node3*/11116);
    	} else if (key_hash.compareTo(node2_hash)>0 && key_hash.compareTo(node3_hash)< 0 ) {
    		cv.put("1", /*node3*/11116);
    		cv.put("2", /*node1*/11112);
    	}else if (key_hash.compareTo(node3_hash)>0 || key_hash.compareTo(node1_hash)< 0 ) {
    		cv.put("1", /*node1*/11112);
    		cv.put("2", /*node2*/ 11108);
    	}
    	return cv;
    }
    

    public static void insert_ack_handler(String[] message) {
    	// TODO Auto-generated method stub
    	new Thread(new Runnable() {
    		public void run(){
    			ack_rxd=true;		
    		}
    	}).start();
    	
    } // end of insert_ack_handler...
    
    public static void insert_handler(final String[] message) {
    	// TODO Auto-generated method stub
    	
    	new Thread(new Runnable(){
    		public void run(){
    			// store it in local database a
    	    	String value=null;
    	    	String key= message[2];
    	    	if(!KeyValueStore.containsKey(key)) {
    	    		int version= 0;
    	    	    value= message[3]+"#"+version;
    	    	}
    	    	else {
    	    		int new_version= Integer.parseInt((KeyValueStore.get(key).split("\\#")[1]))+1;
    	    		value= message[3]+"#"+new_version;
    	    	}
    	    	
    	    	KeyValueStore.put(key, value); // store the message
    	    	
    	    	
    	    	int requester_portno= Integer.parseInt(message[1]);
    	    	// forward it to two other nodes...
    	    	// forwarding to first successor....
    	    	
    	    	
    			
    	    	message[0]="store";
    	    	String incoming= message[0]+":"+my_portno+":"+message[2]+":"+value;
    	    	
    	    	forward(incoming, first_succ_portno);
    	    	System.out.println("forwarded to first successor");
    	    	/* long time= System.currentTimeMillis();
    	    	 while (true){
    	    		 if (ack_rxd || (System.currentTimeMillis()> (time + 100))) break;
    	    	 }*/
    	    	 
    	    	 // forwarding to second successor
    	    	 
    	    	 //ack_rxd=false ;
    	    	 forward(incoming, second_succ_portno);
    	    	 System.out.println("forwarded to second successor");
    	    	 /*long t= System.currentTimeMillis();
    	    	 while (true){
    	    		 if (ack_rxd || (System.currentTimeMillis() > (t + 100))) break;
    	    	 }*/
    	    	 
    	    	 /*
    	    	 if(votes >=2) {
    	    		 String insert_ack= "insert_ack";
    	    		 forward(insert_ack, requester_portno);
    	    	 }
    	    	 ack_rxd=false;
    	    	 votes=0;
    	    	 */
    	    	 String insert_ack= "insert_ack";
    	 		forward(insert_ack, requester_portno);
    	    		
    		}
    	}).start();
    		
    }// end of insert handler....
    
    public static void store_handler(final String[] message) {
    	// TODO Auto-generated method stub
    	new Thread(new Runnable () {
    		public void run(){
    			String key= message[2];
    	    	String value= message[3];
    	    	KeyValueStore.put(key, value); // store the message 
    	    	System.out.println("message stored");		
    		}
    	}).start();
    	
    	
    	
    	//String store_ack= "store_ack";// create a ack message 
    	//int portno=Integer.parseInt(message[1]); // extract the port no
    	//forward(store_ack, portno); // reply with acknowledge..
    	//System.out.println("store ack sent");
    }// end of store_handler....
    

	
    
    //forward function which forwards the messages passed to it............
    public static void forward(final String message, final int portno) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			public void run(){
				Socket remote;
				try {
					remote= new Socket("10.0.2.2", portno);
					PrintWriter out= new PrintWriter(new BufferedWriter(new OutputStreamWriter(remote.getOutputStream())));
					out.println(message);
					out.close();
					remote.close();
				}catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("dude there is error in forwarding the message....");
				}
			}
		}).start();
    	
	}// end of forward method.......

	

	public static void get_missed_handler(String[] message) {
		// TODO Auto-generated method stub
		
		if (KeyValueStore.size()==0 ){
			return ;
		}
		String get_missed_ack="get_missed_ack";
		int remote_portno= Integer.parseInt(message[1]);
		Enumeration<String> en= KeyValueStore.keys();
		
		while(en.hasMoreElements()){
			String key=en.nextElement();
			//ContentValues cv= getPreferenceList(key);
			
			//if(cv.getAsInteger("1")==remote_portno){
				get_missed_ack=get_missed_ack+":"+key+"*"+KeyValueStore.get(key);	
				//KeyValueStore.remove(key);
			//}
		}
		forward(get_missed_ack, remote_portno);
		System.out.println("forwarded get_missed_ack");
	}

	public static void get_missed_ack_handler(String[] message) {
		// TODO Auto-generated method stub
		for (int i=1; i< message.length; i++){
			String []str= message[i].split("\\*");
			KeyValueStore.put(str[0], str[1]);
		}
		
	}

	public static void query_handler(final String[] message) {
		// TODO Auto-generated method stub
	
		new Thread(new Runnable () {
			public void run(){
				int remote_portno= Integer.parseInt(message[1]);
				String key= message[2];
				String get_replica= "get_replica"+":"+my_portno+":"+key;
				
				//int local_version=Integer.parseInt(KeyValueStore.get(key).split("\\#")[1]);
				votes=1; // vote locally...
				System.out.println("forwarding to first successor");
				forward(get_replica, first_succ_portno);
				get_replica_ack_rxd= false;
				long time= System.currentTimeMillis();
				while(true){
					if (get_replica_ack_rxd|| System.currentTimeMillis()==time+200) break;
				}
				
				System.out.println("forwarded to first predecessor...");
				forward(get_replica, first_pred_portno);
				ack_rxd=false;
				time= System.currentTimeMillis();
				while(true){
					if (get_replica_ack_rxd || System.currentTimeMillis()==time+200) break;
				}
				
				String query_ack=null;
				if(votes>=2) {
					query_ack="query_ack"+":"+key+":"+KeyValueStore.get(key).split("\\#")[0];
					
				}else {
					query_ack="query_ack"+":"+key+":"+"null";
				}
				
				forward(query_ack, remote_portno);
		
			}
		}).start();
			}

  public static void query_ack_handler(final String [] message){
	  
	  new Thread(new Runnable (){
		  public void run(){
			  String key=message[1];
			  String value= message[2];
			  String []values= {key , value};
			  String [] cls = {"key","value"};
			  query_result= new MatrixCursor(cls);
			  query_result.addRow(values);
			  query_ack=true;	  
		  }
	  }).start();
	  
  }
  
  
  public static void get_replica_handler(final String[] message) {
		// TODO Auto-generated method stub
	  
	  new Thread(new Runnable() {
		  public void run(){
			  int remote_portno_frustu= Integer.parseInt(message[1]);
			  String key=message[2];
			  String get_replica_ack= "get_replica_ack"+":"+key+":"+KeyValueStore.get(key);
			  System.out.println("forwarding get_replica_ack to "+remote_portno_frustu+" and the message is : "+get_replica_ack);
			  forward(get_replica_ack, remote_portno_frustu);	  
		  }
	  }).start();
	  
	}
  
  
  public static void get_replica_ack_handler(final String[] message) {
		// TODO Auto-generated method stub
	  new Thread(new Runnable() {
		  public void run() {
			  String key= message[1];
			  int remote_version= Integer.parseInt(message[2].split("\\#")[1] );
			  int local_version=Integer.parseInt(KeyValueStore.get(key).split("\\#")[1]);
			  if (remote_version== local_version) votes++;
			  get_replica_ack_rxd=true;
					  
		  }
	  }).start();
	  
	}// end of store_ack_handler....
    
}// end of content provider class....




/////////////////////////////////////////////////////////////////////////////////////////////////////
//server thread which handles incoming messages.........



class Server extends Thread {

ServerSocket server=null;
Socket client=null;

public Server(){

}
public void run() {
try {
	System.out.println("server starting at 10000");
	server=new ServerSocket(10000);
	while(true){
		client=server.accept();
		BufferedReader br= new BufferedReader(new InputStreamReader(client.getInputStream()));
		String incoming=br.readLine();
		br.close();
		client.close();

		System.out.println("incoming messag = "+incoming);
		String[] message=incoming.split(":");

		if(message[0].equals("store")){
			System.out.println("store message rxd");
			SimpleDynamoProvider.store_handler(message);
		}// end of if case
		else if (message[0].equals("insert")){
			System.out.println("insert message rxd");
			SimpleDynamoProvider.insert_handler(message);
		}
		else if (message[0].equals("insert_ack")){
			System.out.println("insert_ack message rxd");
			SimpleDynamoProvider.insert_ack_handler(message);
		}
		else if(message[0].equals("query")){
			System.out.println("query message rxd");
			SimpleDynamoProvider.query_handler(message);
		}
		else if(message[0].equals("query_ack")){
			System.out.println("query_ack message rxd");
			SimpleDynamoProvider.query_ack_handler(message);
		}
		else if(message[0].equals("get_replica")){
			System.out.println("get_replica message rxd");
			SimpleDynamoProvider.get_replica_handler(message);
		}
		else if (message[0].equals("get_replica_ack")){
			System.out.println("get_replica_ack message rxd");
			SimpleDynamoProvider.get_replica_ack_handler(message);
		}
		else if (message[0].equals("get_missed")){
			System.out.println("get_miised message rxd");
			SimpleDynamoProvider.get_missed_handler(message);
		}
		else if (message[0].equals("get_missed_ack")){
			System.out.println("get_missed_ack message rxd");
			SimpleDynamoProvider.get_missed_ack_handler(message);
		}
		else System.out.println("unknown message rxd and message is "+message);
		
		
	}// end of while loop
}catch (Exception e){
	System.out.println("error in server thread creation...");
	e.printStackTrace();
}// end of try block
}//end of run method




}// end of server thread