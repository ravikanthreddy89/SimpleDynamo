package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LDumpListener implements OnClickListener {
	
	private static final String TAG = LDumpListener.class.getName();
	private static final int TEST_CNT = 20;
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";
	
	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;
	public ContentValues[] mContentValues;
	Context cv;

	
	public LDumpListener(TextView _tv, ContentResolver _cr) {
		mTextView = _tv;
		mContentResolver = _cr;
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
		}
	
	
	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	
	public void onClick(View v) {
		mTextView.setText("");
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	
	private class Task extends AsyncTask<Void, String , Void>
	{
		@Override
		protected Void doInBackground(Void... params){
			Cursor resultCursor = mContentResolver.query(mUri, null,
					"ldump", null, null);
		
			resultCursor.moveToFirst();
			while(resultCursor.isAfterLast()==false){
				String key= resultCursor.getString(0);
				String value= resultCursor.getString(1);
				resultCursor.moveToNext();
				publishProgress((key+":"+value));
			}
			
			return null;
		   }
		
		protected void onProgressUpdate(String...strings) {
			mTextView.append(strings[0]);
			mTextView.append("\n");
			//mTextView.append("===========");

			return;
		}
		
	}
}
