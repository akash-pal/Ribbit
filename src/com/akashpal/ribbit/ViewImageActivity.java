package com.akashpal.ribbit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class ViewImageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		
		final ImageView imageView = (ImageView) findViewById(R.id.imageView);
		final Uri imageUri =getIntent().getData();
		
		//Picasso.with(this).load(imageUri.toString()).into(imageView);
		/*
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				finish();			
			}
		}, 10*1000);//10 sec
		*/
		
		
		Transformation blurTransformation = new Transformation() {
		    @Override
		    public Bitmap transform(Bitmap source) {
		        Bitmap blurred = Blur.fastblur(ViewImageActivity.this, source, 10);
		        source.recycle();
		        return blurred;
		    }

		    @Override
		    public String key() {
		        return "blur()";
		    }
		};
		
		
	
		Picasso.with(this)
	    .load(imageUri.toString()) // thumbnail url goes here
	    .placeholder(R.drawable.ic_photo_black_24dp)
	    
	    .transform(blurTransformation)
	    .into(imageView, new Callback() {
	        public void onSuccess() {
	            Picasso.with(ViewImageActivity.this)
	                    .load(imageUri.toString()) // image url goes here
	                    
	                    .placeholder(imageView.getDrawable())
	                    .into(imageView);
	        }

	        @Override
	        public void onError() {
	        }
	    });
		
	}
}
