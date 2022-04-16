package com.jsussan.simplex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Functions for creating or editing an item
public class EditActivity extends AppCompatActivity {
	public static final String EXTRA_ITEM = "com.jsussan.simplex.item";
	InventoryDB inventoryDB;

	EditText itemName;
	EditText itemQuantity;
	Button buttonSave;
	Button buttonDelete;

	private Item mItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		setContentView(R.layout.activity_edit_item);

		inventoryDB = InventoryDB.getInstance(this);
		itemName = findViewById(R.id.inputItemName);
		itemQuantity = findViewById(R.id.inputQuantity);
		buttonDelete = findViewById(R.id.buttonDelete);
		buttonSave = findViewById(R.id.buttonSave);

		// Default state of buttons
		buttonDelete.setVisibility(View.GONE);
		buttonSave.setEnabled(false);

		int initialQuantity = 0;

		Item item = (Item) getIntent().getSerializableExtra(EXTRA_ITEM);
		if (item != null) {
			mItem = item;
			itemName.setText(item.getName());
			initialQuantity = item.getQuantity();
			buttonDelete.setVisibility(View.VISIBLE);
		}

		// Set initial quantity
		itemQuantity.setText(String.valueOf(initialQuantity));

		// Text change listener
		itemName.addTextChangedListener(textWatcher);
		itemQuantity.addTextChangedListener(textWatcher);
	}

	// Enable buttons if text change is input
	private final TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
			buttonSave.setEnabled(!getItemName().isEmpty());
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	// Save item to database
	public void handleSaveItem(View view) {
		boolean saved;

		// Update existing item value
		if (mItem != null) {
			mItem.setName(getItemName());
			mItem.setQuantity(getItemQuantity());
			saved = inventoryDB.updateItem(mItem);
		} else {
			// Create new item if unique id
			saved = inventoryDB.addItem(getItemName(), getItemQuantity());
		}

		// Return to previous screen if saved successfully
		if (saved) {
			NavUtils.navigateUpFromSameTask(this);
		} else {
			Toast.makeText(EditActivity.this, R.string.saveError, Toast.LENGTH_SHORT).show();
		}
	}

	// Delete existing item
	public void handleDeleteItem(View view) {
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.deleteItemMessage).setMessage(R.string.confirmDelete)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Delete the item from the database
						boolean deleted = inventoryDB.deleteItem(mItem);
						finish();

						// Return to previous screen if delete successful
						if (deleted) {
							NavUtils.navigateUpFromSameTask(EditActivity.this);
						} else {
							Toast.makeText(EditActivity.this, R.string.deleteError, Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton("No", null).show();
	}

	// Increment item quantity by 1
	public void incrementQuantity(View view) {
		itemQuantity.setText(String.valueOf(getItemQuantity() + 1));
	}

	// Decrement item quantity by 1
	public void decrementQuantity(View view) {
		itemQuantity.setText(String.valueOf(Math.max(0, getItemQuantity() - 1)));
	}

	// Helper method for name
	private String getItemName() {
		Editable name = itemName.getText();
		return name != null ? name.toString().trim() : "";
	}

	// Helper method for quantity
	private int getItemQuantity() {
		String rawValue = itemQuantity.getText().toString().replaceAll("[^\\d.]", "").trim();
		int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

		// Quantity cannot be < 0
		return Math.max(quantity, 0);
	}
}
