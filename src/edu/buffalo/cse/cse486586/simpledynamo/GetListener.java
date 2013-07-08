package edu.buffalo.cse.cse486586.simpledynamo;

import edu.buffalo.cse.cse486586.simpledynamo.GetListener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class GetListener implements OnClickListener {
	
	private static final String TAG = GetListener.class.getName();
	private static final int TEST_CNT = 20;
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";

	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;
	//private final ContentValues[] mContentValues;

	public GetListener(TextView _tv, ContentResolver _cr) {
		mTextView = _tv;
		mContentResolver = _cr;
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
	//  mContentValues = initTestValues();
	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	

	@Override
	public void onClick(View v) {
		mTextView.append("");
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class Task extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				for (int i = 0; i < TEST_CNT; i++) {
					
					Cursor resultCursor = mContentResolver.query(mUri, null,
							Integer.toString(i), null, null);
					if (resultCursor == null) {
						Log.e("Error in the do inBackground of GetListener", "Result null");
						throw new Exception();
					}

					int keyIndex = resultCursor.getColumnIndex(KEY_FIELD);
					int valueIndex = resultCursor.getColumnIndex(VALUE_FIELD);
					if (keyIndex == -1 || valueIndex == -1) {
						Log.e("Error in GetListener...", "Wrong columns");
						resultCursor.close();
						throw new Exception();
					}

					resultCursor.moveToFirst();

					if (!(resultCursor.isFirst() && resultCursor.isLast())) {
						Log.e("Error in GetListener...", "Wrong number of rows");
						resultCursor.close();
						throw new Exception();
					}

					String returnKey = resultCursor.getString(keyIndex);
					String returnValue = resultCursor.getString(valueIndex);
					//String []returned_values= {returnKey, returnValue};
                    publishProgress(returnKey+":"+returnValue);
					
					resultCursor.close();
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				System.out.println("error dude............");
				e.printStackTrace();
				
			}
			
			return null;
		}
		
		protected void onProgressUpdate(String...strings) {
			mTextView.append(strings[0]);
            mTextView.append("\n");
			return;
		}



		private boolean testQuery() {
			
			return true;
		}
	}
		
	}

