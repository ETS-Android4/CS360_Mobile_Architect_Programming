package com.jsussan.simplex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

	// Database instance
	InventoryDB inventoryDB;
	EditText editTextUsername;
	EditText editTextPassword;
	Button buttonLogin;
	Button buttonRegister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		inventoryDB = InventoryDB.getInstance(this);
		editTextUsername = findViewById(R.id.editTextUsername);
		editTextPassword = findViewById(R.id.editTextPassword);
		buttonLogin = findViewById(R.id.buttonLogin);
		buttonRegister = findViewById(R.id.buttonRegister);

		// Set buttons to disabled by default
		buttonLogin.setEnabled(false);
		buttonRegister.setEnabled(false);

		// Text change listener
		editTextUsername.addTextChangedListener(textWatcher);
		editTextPassword.addTextChangedListener(textWatcher);
	}

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
			boolean fieldsAreEmpty = getUsername().isEmpty() || getPassword().isEmpty();
			buttonLogin.setEnabled(!fieldsAreEmpty);
			buttonRegister.setEnabled(!fieldsAreEmpty);
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	public void login(View view) {
		// Validate credentials
		if (!validCredentials()) {
			showError(view.getContext().getResources().getString(R.string.invalidLogin));
			return;
		}

		try {
			boolean isLoggedIn = inventoryDB.checkUser(getUsername(), hash(getPassword()));
			if (isLoggedIn) {
				handleLoggedInUser();
			} else {
				showError(view.getContext().getResources().getString(R.string.invalidLogin));
			}
		} catch (Exception e) {
			showError(view.getContext().getResources().getString(R.string.invalidLogin));
		}
	}

	public void register(View view) {
		// Validate credentials
		if (!validCredentials()) {
			showError(view.getContext().getResources().getString(R.string.registrationError));
		}

		try {
			boolean userCreated = inventoryDB.addUser(getUsername(), hash(getPassword()));
			if (userCreated) {
				handleLoggedInUser();
			} else {
				showError(view.getContext().getResources().getString(R.string.registrationError));
			}
		} catch (Exception e) {
			showError(view.getContext().getResources().getString(R.string.registrationError));
		}
	}

	private void handleLoggedInUser() {
		Intent intent = new Intent(getApplicationContext(), InventoryDBActivity.class);
		startActivity(intent);
	}

	private boolean validCredentials() {
		return !getUsername().isEmpty() && !getPassword().isEmpty();
	}

	// GET username input
	private String getUsername() {
		Editable username = editTextUsername.getText();
		return username != null ? username.toString().trim().toLowerCase() : "";
	}

	// GET password input
	private String getPassword() {
		Editable password = editTextPassword.getText();
		return password != null ? password.toString().trim() : "";
	}

	// Hash password that was input
	private String hash(String password) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(password.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}

	// Helper function
	private void showError(String errorMessage) {
		Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, -200);
		toast.show();
	}
}
