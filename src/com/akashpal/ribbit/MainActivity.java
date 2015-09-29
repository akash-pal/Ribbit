package com.akashpal.ribbit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.parse.ParseUser;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	
	protected static final int TAKE_PHOTO_REQUEST = 0;
	protected static final int TAKE_VIDEO_REQUEST = 1;
	protected static final int PICK_PHOTO_REQUEST = 2;
	protected static final int PICK_VIDEO_REQUEST = 3;
	
	protected static final int MEDIA_TYPE_IMAGE = 4; 
	protected static final int MEDIA_TYPE_VIDEO = 5; 
	
	public static final int FILE_SIZE_LIMIT = 1024 *1024 *10; //10MB
	
	private Uri MediaUri;
	
	public static final String TAG = MainActivity.class.getSimpleName();
	

	protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			switch(which)
			{
			case 0:
				//take photo
				Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				
				MediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
				if(MediaUri == null)
				{
					Toast.makeText(MainActivity.this,R.string.error_external_storage,Toast.LENGTH_SHORT).show();
				}
			    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaUri); // set the image file name
				startActivityForResult(takePhotoIntent,TAKE_PHOTO_REQUEST);
				break;
			case 1:
				//take video
				Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				MediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
				if(MediaUri == null)
				{
					Toast.makeText(MainActivity.this,R.string.error_external_storage,Toast.LENGTH_SHORT).show();
				}
			    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaUri);
			    takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); //10 sec
			    takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);//low res
			    startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
				break;
			case 2:
				//choose photo
				Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
				choosePhotoIntent.setType("image/*");
				startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
				break;
			case 3:
				//choose video
				Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
				chooseVideoIntent.setType("video/*");
				Toast.makeText(MainActivity.this,R.string.video_file_size_warning, Toast.LENGTH_LONG).show();
				startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
				break;
			}
			
		}

		private Uri getOutputMediaFileUri(int mediaType) {
			
			// To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this.
			
			if(isExternalStorageAvailable())
			{
				//get the URI
				String appname = MainActivity.this.getString(R.string.app_name);
				//1.Get the external storage directory
				File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),appname);
				//2.Create a sub directory
				if (! mediaStorageDir.exists()){
			        if (! mediaStorageDir.mkdirs()){
			            Log.d(TAG, "failed to create directory");
			            return null;
			        }
			    }
				//3.Create File name
				//4.Create the file
				File mediaFile;
				Date now = new Date(); 
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(now);
				String path = mediaStorageDir.getPath() + File.separator; 
				if(mediaType == MEDIA_TYPE_IMAGE)
				{
					mediaFile = new File(path + "IMG_" + timeStamp + ".jpg");
				}
				else if(mediaType == MEDIA_TYPE_VIDEO)
				{
					mediaFile = new File(path + "VID_" + timeStamp + ".mp4");
				}
				else
				{
					return null;
				}
				//5.Return the file's URI
				Log.d(TAG, "File:" + Uri.fromFile(mediaFile));
				return Uri.fromFile(mediaFile);
			}
			else
			{
				return null;
			}
		}

		private boolean isExternalStorageAvailable() {
			String state =Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			  return true;
			}
			else
			{
				return false;
			}
			
		}
	};
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		
		
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null) {
        	  // do stuff with the user
        	Log.i(TAG, currentUser.getUsername());
        } 
        else
        {
        	  // show the signup or login screen
	        	navigateToLogin();
        }
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(this,getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if(resultCode == RESULT_OK)
			{
				if(requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST)
				{
					if(data==null)
					{
						Toast.makeText(this,R.string.general_error, Toast.LENGTH_SHORT).show();
					}
					else
					{
						MediaUri = data.getData();
					}
					
					if(requestCode == PICK_VIDEO_REQUEST)
					{
						int fileSize = 0;
						InputStream inputStream = null;
						try {
							inputStream = getContentResolver().openInputStream(MediaUri);
							fileSize = inputStream.available();
						} catch (FileNotFoundException e) {
							Toast.makeText(this,R.string.error_opening_file, Toast.LENGTH_SHORT).show();
							return;
						} catch (IOException e) {
							Toast.makeText(this,R.string.error_opening_file, Toast.LENGTH_SHORT).show();
							return;
						}	
						finally{
							try {
								inputStream.close();
							} catch (IOException e) { }
						}
						
						if(fileSize >= FILE_SIZE_LIMIT)
						{
							Toast.makeText(this,R.string.error_file_size_too_large, Toast.LENGTH_SHORT).show();
							return;
						}
					}	
				}
				else
				{
					Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					mediaScanIntent.setData(MediaUri);
					sendBroadcast(mediaScanIntent); //broadcast the Uri of photo/video taken to add to gallery
				}
				
				Intent intent = new Intent(this,RecipientActivity.class);
				intent.setData(MediaUri);
				
				String fileType = null;
				if(requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST)
				{
					fileType = ParseConstants.TYPE_IMAGE;
				}
				else if(requestCode == PICK_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST)
				{
					fileType = ParseConstants.TYPE_VIDEO;
				}
				intent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
				startActivity(intent);
			}
			else if(resultCode != RESULT_CANCELED )
			{
				Toast.makeText(this,R.string.general_error, Toast.LENGTH_SHORT).show();
			}
	}
	
	private void navigateToLogin() {
		Intent intent = new Intent(this,LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.action_logout) {
			ParseUser.logOut();
			navigateToLogin();
		}
		else if(id == R.id.action_edit_friends){
			Intent intent = new Intent(this,EditFriendsActivity.class);
			startActivity(intent);
		}
		else if(id==R.id.action_camera)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(R.array.camera_choices, mDialogListener);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
}
