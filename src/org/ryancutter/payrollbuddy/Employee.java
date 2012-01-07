package org.ryancutter.payrollbuddy;

import java.math.BigDecimal;

public class Employee {
	private long mEmpId;
	private String mName;
	private int mStatus;
	private int mPay;
	private String mPayrate;
	private int mPayunit;
	private int mPrecision;
	private int mOvertime;
	
	public static final int HOURLY = 0;
	public static final int DAILY = 1;
	
	public Employee(long id, String name, int status, int pay, String payrate,
			int payunit, int precision, int overtime) {
		mEmpId = id;
		mName = name;
		mStatus = status;
		mPay = pay;
		mPayrate = payrate;
		mPayunit = payunit;
		mPrecision = precision;
		mOvertime = overtime;
	}
	
	public int getPayunit() { return mPayunit; }
	public int getPrecision() { return mPrecision; }

	public double getPayrate() {
		Double d = new Double(0.0);
		try {
			d = new Double(mPayrate);
		} catch (Exception e) {
			return 0.0;
		}
		
		return d;
	}

	public boolean isOvertime() {
		if(mOvertime == 1) {
			return true;
		}
		
		return false;
	}
	
	public boolean isPaid() {
		if(mPay == 1) {
			return true;
		}
		
		return false;
	}
}
