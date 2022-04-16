package com.jsussan.simplex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class InventoryDB extends SQLiteOpenHelper {

	// Database information
	private static final String LOG = "InventoryDB";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "inventoryApp.db";
	private static InventoryDB sInventoryDB;

	// Method to GET inventory
	public static InventoryDB getInstance(Context context) {
		Log.i(LOG, "Get instance of database");
		if (sInventoryDB == null) {
			sInventoryDB = new InventoryDB(context);
		}
		return sInventoryDB;
	}

	private InventoryDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Inventory table
	private static final class InventoryTable {
		private static final String TABLE = "inventory";
		private static final String COL_ID = "_id";
		private static final String COL_NAME = "name";
		private static final String COL_QUANTITY = "quantity";
	}

	// User table
	private static final class UsersTable {
		private static final String TABLE = "users";
		private static final String COL_ID = "_id";
		private static final String COL_USERNAME = "username";
		private static final String COL_PASSWORD = "password";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(LOG, "Create database");
		db.execSQL("CREATE TABLE " + InventoryTable.TABLE + " (" +
				InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				InventoryTable.COL_NAME + " TEXT, " +
				InventoryTable.COL_QUANTITY + " INTEGER)");
		db.execSQL("CREATE TABLE " + UsersTable.TABLE + " (" +
				UsersTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				UsersTable.COL_USERNAME + " TEXT, " +
				UsersTable.COL_PASSWORD + " TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + UsersTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + InventoryTable.TABLE);
		onCreate(db);
	}

	// GET inventory items
	public List<Item> getItems() {
		List<Item> items = new ArrayList<Item>();
		SQLiteDatabase db = getReadableDatabase();

		String sql = "SELECT * FROM " + InventoryTable.TABLE;
		Cursor cursor = db.rawQuery(sql, new String[]{});
		if (cursor.moveToFirst()) {
			do {
				long id = cursor.getLong(0);
				String name = cursor.getString(1);
				int quantity = cursor.getInt(2);
				items.add(new Item(id, name, quantity));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return items;
	}

	// Create a new user if unique
	public boolean addUser(String username, String password) {
		// Guard against registering an existing user
		if (usernameExists(username)) {
			return false;
		}

		SQLiteDatabase db = this.getWritableDatabase();

		// Set username and password
		ContentValues values = new ContentValues();
		values.put(UsersTable.COL_USERNAME, username);
		values.put(UsersTable.COL_PASSWORD, password);
		long userId = db.insert(UsersTable.TABLE, null, values);
		return userId != -1;
	}

	// Validate user credentials
	public boolean checkUser(String username, String password) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ? AND password = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{username, password});

		return cursor.getCount() > 0;
	}

	// Check if user is unique
	public boolean usernameExists(String username) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{username});
		return cursor.getCount() > 0;
	}

	// Add item to database
	public boolean addItem(String name, int quantity) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(InventoryTable.COL_NAME, name);
		values.put(InventoryTable.COL_QUANTITY, quantity);
		long itemId = db.insert(InventoryTable.TABLE, null, values);
		return itemId != -1;
	}

	// Update existing item in database
	public boolean updateItem(Item item) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(InventoryTable.COL_NAME, item.getName());
		values.put(InventoryTable.COL_QUANTITY, item.getQuantity());

		int rowsUpdated = db.update(InventoryTable.TABLE, values, InventoryTable.COL_ID + " = ?",
				new String[]{String.valueOf(item.getId())});
		return rowsUpdated > 0;
	}

	// Delete item from database
	public boolean deleteItem(Item item) {
		SQLiteDatabase db = getWritableDatabase();
		int rowsDeleted = db.delete(InventoryTable.TABLE, InventoryTable.COL_ID + " = ?",
				new String[]{String.valueOf(item.getId())});

		return rowsDeleted > 0;
	}
}
