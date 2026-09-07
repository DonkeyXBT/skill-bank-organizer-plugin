package com.skillbankorganizer.data;

public final class BankStack
{
	public final int id;
	public final String name;
	public final int qty;
	public final int price;

	public BankStack(int id, String name, int qty, int price)
	{
		this.id = id;
		this.name = name == null ? "" : name;
		this.qty = qty;
		this.price = price;
	}

	public long geValue()
	{
		return (long) price * (long) qty;
	}
}
