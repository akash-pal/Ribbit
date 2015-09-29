package com.akashpal.ribbit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RecipientActivity extends ListActivity {
	
	public static final String TAG = RecipientActivity.class.getSimpleName(); 
	protected List<ParseUser> mFriends;
	protected ParseRelation<ParseUser> mFriendRelation;
	protected ParseUser mCurrentUser;
	
	protected MenuItem mSendMenuItem;
	protected Uri mMediaUri;
	protected String mFileType;
	private String mExtension;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_recepient);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mMediaUri = getIntent().getData();
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recepient, menu);
		mSendMenuItem = menu.getItem(0);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_send) {
			ParseObject message = createMessage();
			if(message == null)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.error_creating_message);
				builder.setTitle(R.string.error_label_dialog);
				builder.setPositiveButton(android.R.string.ok,null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			else
			{
				send(message);	
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	protected ParseObject createMessage()
	{
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGE);
		message.put(ParseConstants.KEY_SENDER_ID,ParseUser.getCurrentUser().getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME,ParseUser.getCurrentUser().getUsername());
		message.put(ParseConstants.KEY_RECIPIENT_IDS,getRecipientsIds());
		message.put(ParseConstants.KEY_FILE_TYPE, mFileType);
		
		byte [] fileBytes =  FileHelper.getByteArrayFromFile(this,mMediaUri);
		Log.d("SKY_FILE SIZE",fileBytes+"");
		if(fileBytes == null)
		{
			return null;
		}
		else
		{
			if(mFileType.equals(ParseConstants.TYPE_IMAGE))
			{
				mExtension = FilenameUtils.getExtension(mMediaUri.toString());
				fileBytes = FileHelper.reduceImageForUpload(fileBytes,mExtension);
				Log.d("SKY_FILE SIZE",fileBytes+"");
			}
			
			String fileName = FileHelper.getFileName(this,mMediaUri,mFileType);
			
			ParseFile file = new ParseFile(fileName, fileBytes);
			message.put(ParseConstants.KEY_FILE, file);
		}
		
		return message;
		
	}
	

	protected ArrayList<String> getRecipientsIds()
	{
		ArrayList<String> recipientIds = new ArrayList<String>();
	    for(int i=0;i< getListView().getCount() ; i++)
	    {
	       if(getListView().isItemChecked(i))
	       {
	    	   recipientIds.add(mFriends.get(i).getObjectId());
	       }
	    }
		return recipientIds; 
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setProgressBarIndeterminateVisibility(true);
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
		ParseQuery<ParseUser> query = mFriendRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		query.setLimit(1000);
		query.findInBackground(new FindCallback<ParseUser>() {
			
			@Override
			public void done(List<ParseUser> friends, ParseException e) {

				setProgressBarIndeterminateVisibility(false);
				if(e==null)
				{
					//success
					mFriends = friends;
					String[] username = new String[mFriends.size()];
					int i=0;
					for(ParseUser user : mFriends)
					{
						username[i]=user.getUsername();
						i++;
					}
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>
					      (getListView().getContext(), android.R.layout.simple_list_item_checked,username);
					setListAdapter(adapter);
					
				}
				else
				{
					//error
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
					builder.setMessage(e.getMessage())
					.setTitle(R.string.error_title)
					.setPositiveButton(android.R.string.ok, null);
					
					AlertDialog dialog = builder.create();
					dialog.show();	
				}
			}
		});
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if(l.getCheckedItemCount()>0)
		{
		   mSendMenuItem.setVisible(true);
		}
		else
		{
		   mSendMenuItem.setVisible(false);
		}
	}
	
	private void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e==null)
				{
					//success
					Toast.makeText(RecipientActivity.this, R.string.message_sent, Toast.LENGTH_LONG).show();
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientActivity.this);
					builder.setMessage(R.string.error_sending_message);
					builder.setTitle(R.string.error_label_dialog);
					builder.setPositiveButton(android.R.string.ok,null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				
			}
		});
	}

}
