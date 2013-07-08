package edu.buffalo.cse.cse486586.simpledynamo;

import edu.buffalo.cse.cse486586.simpledynamo.Put1Listener;
import edu.buffalo.cse.cse486586.simpledynamo.Put2Listener;
import edu.buffalo.cse.cse486586.simpledynamo.Put3Listener;
import edu.buffalo.cse.cse486586.simpledynamo.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class SimpleDynamoActivity extends Activity {

	private ContentResolver cr;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dynamo);
    
		TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        
        findViewById(R.id.button1).setOnClickListener(
                new Put1Listener(tv, getContentResolver()));
        findViewById(R.id.button2).setOnClickListener(
                new Put2Listener(tv, getContentResolver()));
        findViewById(R.id.button3).setOnClickListener(
                new Put3Listener(tv, getContentResolver()));
        
        findViewById(R.id.button4).setOnClickListener(
                new LDumpListener(tv, getContentResolver()));
        
        findViewById(R.id.button5).setOnClickListener(
                new GetListener(tv, getContentResolver()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simple_dynamo, menu);
		return true;
	}
	
		
	public void get(View view4){
		
	}

}
