package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class Put2Listener implements OnClickListener {

	private static final String TAG = Put2Listener.class.getName();
	private static final int TEST_CNT = 20;
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";
	
	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;
	public ContentValues[] mContentValues;
	Context cv;
	
	
	public Put2Listener(TextView _tv, ContentResolver _cr) {
		mTextView = _tv;
		mContentResolver = _cr;
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
		
		mContentValues = initTestValues();
	}
	
	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	
	
	private ContentValues[] initTestValues() {
		ContentValues[] cv = new ContentValues[TEST_CNT];
		for (int i = 0; i < TEST_CNT; i++) {
			cv[i] = new ContentValues();
			cv[i].put(KEY_FIELD, Integer.toString(i));
			cv[i].put(VALUE_FIELD, "Put2" + Integer.toString(i));
		}

		return cv;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
	}

	private class Task extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (testInsert()) {
			//	publishProgress("Insert success\n");
			} else {
				publishProgress("Insert fail\n");
				return null;
			}

			
			return null;
		}
		
		protected void onProgressUpdate(String...strings) {
			mTextView.append(strings[0]);

			return;
		}

		private boolean testInsert() {
			try {
				for (int i = 0; i < TEST_CNT; i++) {
					mContentResolver.insert(mUri, mContentValues[i]);
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				return false;
			}

			return true;
		}

			}	
		
	}

