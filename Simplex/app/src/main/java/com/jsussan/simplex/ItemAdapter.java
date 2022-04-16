package com.jsussan.simplex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {
	private static final String TAG = "ItemAdapter";
	private List<Item> mItems;
	private Context mContext;
	InventoryDB inventoryDB;

	// Constructor
	public ItemAdapter(List<Item> items, Context ctx, InventoryDB inventoryDb) {
		mItems = items;
		mContext = ctx;
		inventoryDB = inventoryDb;
	}

	@Override
	public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item, parent, false);
		return new ItemHolder(view, inventoryDB);
	}

	@Override
	public void onBindViewHolder(ItemHolder holder, @SuppressLint("RecyclerView") int position) {
		// Get inventory item
		Item item = mItems.get(position);
		holder.bind(item);

		// Actions button listener
		holder.mButtonActions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(mContext, holder.mButtonActions);
				// Menu
				popup.inflate(R.menu.actions_menu);
				// Click listener
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						switch (menuItem.getItemId()) {
							case R.id.menuEdit:
								// Navigate to the edit screen and pass in the current item
								Log.i(TAG, "edit item at position " + position);

								Intent intent = new Intent(mContext, EditActivity.class);
								intent.putExtra(EditActivity.EXTRA_ITEM, item);
								mContext.startActivity(intent);

								return true;
							case R.id.menuRemove:
								// Delete the current item from the database and the list
								Log.i(TAG, "remove item at position " + position);

								// Confirmation dialog
								new AlertDialog.Builder(mContext).setIcon(android.R.drawable.ic_dialog_alert)
										.setTitle(R.string.deleteItemMessage).setMessage(R.string.confirmDelete)
										.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// Delete item from the database
												boolean deleted = inventoryDB.deleteItem(item);
												if (deleted) {
													// Remove the item from the list
													mItems.remove(position);
													notifyItemRemoved(position);
													notifyDataSetChanged();
												} else {
													// Error message
													Toast.makeText(mContext, R.string.deleteError, Toast.LENGTH_SHORT).show();
												}
											}
										}).setNegativeButton("No", null).show();

								return true;
							default:
								return false;
						}
					}
				});
				//display popup
				popup.show();
			}
		});
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	class ItemHolder extends RecyclerView.ViewHolder {
		private Item mItem;
		private TextView mNameTextView;
		private EditText mQuantityView;
		InventoryDB inventoryDB;

		ImageButton mDecrementQty;
		ImageButton mIncrementQty;
		ImageButton mButtonActions;

		// Constructor
		public ItemHolder(View itemView, InventoryDB inventoryDb) {
			super(itemView);
			inventoryDB = inventoryDb;
			mNameTextView = itemView.findViewById(R.id.itemName);
			mQuantityView = itemView.findViewById(R.id.editQuantity);
			mDecrementQty = itemView.findViewById(R.id.buttonDecrementQty);
			mIncrementQty = itemView.findViewById(R.id.buttonIncrementQty);
			mButtonActions = itemView.findViewById(R.id.buttonActions);

			// Update quantity when the decrement button is clicked
			mDecrementQty.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mItem.decrementQuantity();
					boolean updated = inventoryDB.updateItem(mItem);
					if (updated) {
						mQuantityView.setText(String.valueOf(mItem.getQuantity()));
					}
				}
			});

			// Update quantity when the increment button is clicked
			mIncrementQty.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mItem.incrementQuantity();
					boolean updated = inventoryDB.updateItem(mItem);
					if (updated) {
						mQuantityView.setText(String.valueOf(mItem.getQuantity()));
					}
				}
			});

			// Listen for changes in text field
			mQuantityView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
					mItem.setQuantity(getItemQuantity());
					boolean updated = inventoryDB.updateItem(mItem);
					Log.d(TAG, "Item quantity updated: " + updated);
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
		}

		// Bind model to controller
		public void bind(Item item) {
			mItem = item;
			Log.d("ItemHolder", "Bind item: " + mItem.getName());
			mNameTextView.setText(mItem.getName());
			mQuantityView.setText(String.valueOf(mItem.getQuantity()));
		}

		// Helper method to convert quantity to integer
		private int getItemQuantity() {
			String rawValue = mQuantityView.getText().toString().replaceAll("[^\\d.]", "").trim();
			int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

			// Quantity cannot be less than 0
			return Math.max(quantity, 0);
		}
	}
}
