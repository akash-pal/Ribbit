package com.akashpal.ribbit;

import android.app.Application;

import com.parse.Parse;

public class RibbitApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "0r8rs0t3IbIUyQSSS5KWsOwYRy67yuFAmrorWRxG", "dMRnRvt7a0xajLbKfk023kK8YU4SxBOh4FlPt0q0");		
		
		
	}
}
