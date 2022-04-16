package com.jsussan.simplex;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SmsActivity extends AppCompatActivity {
	private static final String TAG = "SmsNotificationsActivity";
	public static String PREFERENCE_RECEIVE_NOTIFICATIONS = "pref_receive_notifications";

	// Permissions
	private final int REQUEST_SEND_SMS_CODE = 0;
	SwitchMaterial notificationsToggle;
	SharedPreferences sharedPrefs;
	boolean receiveNotifications = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		setContentView(R.layout.activity_sms_notifications);
		notificationsToggle = findViewById(R.id.toggleSwitch);

		// Listen for changes to the toggle switch
		notificationsToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@SuppressLint("LongLogTag")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				receiveNotifications = isChecked;
				// Check for permissions
				if (isChecked && hasPermissions()) {
					Log.d(TAG, "Receive Notifications: Yes");
					notificationsToggle.setChecked(true);
				} else {
					Log.d(TAG, "Receive Notifications: No");
					notificationsToggle.setChecked(false);
					receiveNotifications = false;
				}

				// Save user preferences
				savePreferences();
			}
		});

		// Default preferences
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		receiveNotifications = sharedPrefs.getBoolean(PREFERENCE_RECEIVE_NOTIFICATIONS, false);

		// Set the initial state of the toggle switch
		if (receiveNotifications
				&& ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
				== PackageManager.PERMISSION_GRANTED
		) {
			notificationsToggle.setChecked(true);
		}
	}

	// Confirm user has given permission, request if not
	private boolean hasPermissions() {
		String smsPermission = Manifest.permission.SEND_SMS;
		if (ContextCompat.checkSelfPermission(this, smsPermission)
				!= PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, smsPermission)) {
				// Dialog box that explains why we need permissions
				new AlertDialog.Builder(this)
						.setTitle(R.string.smsNotificationBoxHeader)
						.setMessage(R.string.smsPermissionExplanation)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// Actually request permission from the user
								ActivityCompat.requestPermissions(
										SmsActivity.this,
										new String[]{smsPermission},
										REQUEST_SEND_SMS_CODE
								);
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create()
						.show();
			} else {
				// Request permissions from the user
				ActivityCompat.requestPermissions(
						this,
						new String[]{smsPermission},
						REQUEST_SEND_SMS_CODE
				);
			}
			return false;
		}
		return true;
	}

	@SuppressLint("LongLogTag")
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case REQUEST_SEND_SMS_CODE: {
				// Set the preference  based on changes to the granted permissions
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission granted!
					Log.d(TAG, "Permission granted");
					receiveNotifications = true;
					notificationsToggle.setChecked(true);
				} else {
					// Permission denied!
					Log.d(TAG, "Permission denied");
					receiveNotifications = false;
					notificationsToggle.setChecked(false);
				}

				// Save the app preferences
				savePreferences();
			}
		}
	}

	/**
	 * Save the app preferences
	 */
	private void savePreferences() {
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(PREFERENCE_RECEIVE_NOTIFICATIONS, receiveNotifications);
		editor.commit();
	}
}
