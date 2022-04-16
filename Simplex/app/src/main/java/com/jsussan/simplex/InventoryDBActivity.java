package com.jsussan.simplex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class InventoryDBActivity extends AppCompatActivity {
	private static final String TAG = "InventoryList";

	// Inventory list
	private List<Item> mItemList;
	InventoryDB inventoryDB;
	RecyclerView itemListView;
	TextView textViewEmpty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		setContentView(R.layout.activity_inventory_list);

		// Initialize database and get inventory items
		inventoryDB = InventoryDB.getInstance(getApplicationContext());
		mItemList = inventoryDB.getItems();

		// Set up Recycler View
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		itemListView = findViewById(R.id.itemListView);
		itemListView.setLayoutManager(layoutManager);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(itemListView.getContext(),
				layoutManager.getOrientation());
		itemListView.addItemDecoration(dividerItemDecoration);

		// Display message if inventory is empty
		textViewEmpty = findViewById(R.id.textViewEmpty);

		// Send items to recycler view
		ItemAdapter adapter = new ItemAdapter(mItemList, this, inventoryDB);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				checkListIsEmpty();
			}
		});

		itemListView.setAdapter(adapter);

		// Check if inventory is empty
		checkListIsEmpty();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// App bar menu
		getMenuInflater().inflate(R.menu.appbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.action_add_item:
				// Switch to the "create item" view
				Log.d(TAG, "New item view");
				intent = new Intent(getApplicationContext(), EditActivity.class);
				startActivity(intent);
				return true;

			case R.id.actionToggleNotifications:
				// Switch to the notifications setting screen
				Log.d(TAG, "SMS Notifications view");
				intent = new Intent(getApplicationContext(), SmsActivity.class);
				startActivity(intent);
				return true;

			case R.id.actionLogout:
				// Log the user out
				Log.d(TAG, "Logging out");
				intent = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(intent);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// Set view depending on if inventory list is empty or not
	public void checkListIsEmpty() {
		Log.d(TAG, "Inventory size: " + mItemList.size());
		if (mItemList.isEmpty()) {
			itemListView.setVisibility(View.GONE);
			textViewEmpty.setVisibility(View.VISIBLE);
		} else {
			itemListView.setVisibility(View.VISIBLE);
			textViewEmpty.setVisibility(View.GONE);
		}
	}
}
