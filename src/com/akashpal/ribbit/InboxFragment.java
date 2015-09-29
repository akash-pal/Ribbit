package com.akashpal.ribbit;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class InboxFragment extends ListFragment {
	
	List<ParseObject> mMessages;
	
	public InboxFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_inbox, container,false);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGE);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
		query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> messages, ParseException e) {
				
				getActivity().setProgressBarIndeterminateVisibility(false);
				
				if(e==null)
				{	
					//messages retrieved 
					mMessages = messages;
					String [] usernames = new String[mMessages.size()];
					int i=0;
					for(ParseObject message : mMessages)
					{
						usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
						i++;
					}
					
					//ArrayAdapter<String> adapter = new ArrayAdapter<String>
					//(getListView().getContext(),android.R.layout.simple_list_item_1, usernames);
					
					if(getListView().getAdapter() == null)
					{
						MessageAdapter adapter = new MessageAdapter(getListView().getContext(), mMessages);
						setListAdapter(adapter);
					}
					else
					{
						//refill the adapter
						((MessageAdapter)getListView().getAdapter()).refill(mMessages);
					}
				}
			}
		});
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		ParseObject message = mMessages.get(position);
		String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
		ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
		Uri fileUri = Uri.parse(file.getUrl());
		Picasso.with(getActivity()).load(fileUri);
		
		if(messageType.equals(ParseConstants.TYPE_IMAGE))
		{
			
			//view image
			Intent intent = new Intent(getActivity(),ViewImageActivity.class);
			intent.setData(fileUri);
			startActivity(intent);
		}
		else
		{
			//view video
			Intent intent = new Intent(Intent.ACTION_VIEW,fileUri);
			intent.setDataAndType(fileUri,"video/*");
			startActivity(intent);
		}
		
		
		//delete message
		 List<String> ids= message.getList(ParseConstants.KEY_RECIPIENT_IDS);
		 if(ids.size() == 1)
		 {
			 //last recipient--then delete whole thing
		     message.deleteInBackground();
		 }
		 else
		 {
			// remove the recipient and save
				ids.remove(ParseUser.getCurrentUser().getObjectId());
				
				ArrayList<String> idsToRemove = new ArrayList<String>();
				idsToRemove.add(ParseUser.getCurrentUser().getObjectId());
				
				message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
				message.saveInBackground();
		 }
		 
		 
	}

}
