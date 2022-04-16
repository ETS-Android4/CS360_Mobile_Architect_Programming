package com.jsussan.simplex;

import java.io.Serializable;

public class Item implements Serializable {
	private long mId;
	private String mName;
	private int mQuantity;

	// Default constructor
	public Item() {
	}

	// Constructor that holds item id, name, and quantity
	public Item(long id, String name) {
		mId = id;
		mName = name;
		mQuantity = 0;
	}

	public Item(long id, String name, int quantity) {
		mId = id;
		mName = name;
		mQuantity = quantity;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public int getQuantity() {
		return mQuantity;
	}

	// Set quantity and make sure number is not negative
	public void setQuantity(int quantity) {
		this.mQuantity = Math.max(0, quantity);
	}

	// Increment quantity
	public void incrementQuantity() {
		this.mQuantity++;
	}

	// Decrement quantity
	public void decrementQuantity() {
		this.mQuantity = Math.max(0, this.mQuantity - 1);
	}
}
