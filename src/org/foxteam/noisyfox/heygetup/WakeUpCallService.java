package org.foxteam.noisyfox.heygetup;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WakeUpCallService extends Service {
	public WakeUpCallService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
