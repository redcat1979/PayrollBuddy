package org.ryancutter.payrollbuddy;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class EmpWeek extends Activity {
    private Long mRowId;
    private PayrollDbAdapter mDbHelper;
    private Employee mEmployee;
    private DateTime mWeek;
    
    private String[] mDateArray;
    private DateTime[] mActualStart;
    private DateTime[] mDisplayStart;
    private DateTime[] mActualStop;
    private DateTime[] mDisplayStop;
    
    static final int MON_START_DIALOG = 0;
    static final int MON_STOP_DIALOG = 10;
    static final int TUE_START_DIALOG = 1;
	static final int TUE_STOP_DIALOG = 11;
    static final int WED_START_DIALOG = 2;
	static final int WED_STOP_DIALOG = 12;
    static final int THU_START_DIALOG = 3;
	static final int THU_STOP_DIALOG = 13;
    static final int FRI_START_DIALOG = 4;
	static final int FRI_STOP_DIALOG = 14;
    static final int SAT_START_DIALOG = 5;
	static final int SAT_STOP_DIALOG = 15;
    static final int SUN_START_DIALOG = 6;
	static final int SUN_STOP_DIALOG = 16;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PayrollDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.emp_date);

        // get profile data
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(PayrollDbAdapter.KEY_EMPS_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(PayrollDbAdapter.KEY_EMPS_ROWID)
									: null;
		}
		
		Integer year;
		year = (savedInstanceState == null) ? null :
            (Integer) savedInstanceState.getSerializable(PayrollDbAdapter.KEY_EMPS_YEAR);
		if (year == null) {
			Bundle extras = getIntent().getExtras();
			year = extras != null ? extras.getInt(PayrollDbAdapter.KEY_EMPS_YEAR)
									: null;
		}
		
		Integer month;
		month = (savedInstanceState == null) ? null :
            (Integer) savedInstanceState.getSerializable(PayrollDbAdapter.KEY_EMPS_MONTH);
		if (month == null) {
			Bundle extras = getIntent().getExtras();
			month = extras != null ? extras.getInt(PayrollDbAdapter.KEY_EMPS_MONTH)
									: null;
		}
		
		Integer day;
		day = (savedInstanceState == null) ? null :
            (Integer) savedInstanceState.getSerializable(PayrollDbAdapter.KEY_EMPS_DAY);
		if (day == null) {
			Bundle extras = getIntent().getExtras();
			day = extras != null ? extras.getInt(PayrollDbAdapter.KEY_EMPS_DAY)
									: null;
		}
		
        Cursor emp = mDbHelper.fetchEmp(mRowId);
        startManagingCursor(emp);
		mEmployee = buildEmployee(emp);
		emp.close();
		
		DateTime dt = new DateTime(year, month, day, 0, 0);
		mWeek = dt.withDayOfWeek(DateTimeConstants.MONDAY);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE MM/dd");
		mDateArray = new String[7];
		mActualStart = new DateTime[7];
		mDisplayStart = new DateTime[7];
		mActualStop = new DateTime[7];
		mDisplayStop = new DateTime[7];
		for(int i = 0; i < 7; i++) {
			mDateArray[i] = fmt.print(mWeek.plusDays(i));

			Cursor cTime = mDbHelper.fetchTime(mRowId, mWeek.plusDays(i).getMillis());
            startManagingCursor(cTime);
            
            long startTime = 0;
            try {
            	startTime = cTime.getLong(cTime.getColumnIndexOrThrow(PayrollDbAdapter.KEY_TIME_START));
            } catch (Exception e) {}
            			
			if(startTime > 0) {
				mActualStart[i] = new DateTime(startTime);
				mDisplayStart[i] = new DateTime(formatTime(startTime, mEmployee.getPrecision()));
			} else {
				mActualStart[i] = null;
				mDisplayStart[i] = null;
			}

            long stopTime = 0;
            try {
            	stopTime = cTime.getLong(cTime.getColumnIndexOrThrow(PayrollDbAdapter.KEY_TIME_STOP));
            } catch (Exception e) {}

			if(stopTime > 0) {
	            mActualStop[i] = new DateTime(stopTime);
	            mDisplayStop[i] = new DateTime(formatTime(stopTime, mEmployee.getPrecision()));
			} else {
				mActualStop[i] = null;
				mDisplayStop[i] = null;
			}
		}
		
		updateDisplay();
	}
    
    public static long formatTime(long startTime, int precision) {
    	DateTime t = new DateTime(startTime);

    	int hour = t.getHourOfDay();
    	int minute = t.getMinuteOfHour();
    	
    	switch(precision) {
    	case 6:
    		minute = calc6(minute);
    		break;
    	case 15:
    		minute = calc15(minute);
    		break;
    	}
    	
    	if(minute == -1) {
    		minute = 0;
    		hour += 1;
    	}
    	
    	DateTime newTime = new DateTime(t.getYear(), t.getMonthOfYear(), t.getDayOfMonth(), hour, minute);

    	return newTime.getMillis();
    }
    
    public static Employee buildEmployee(Cursor emp) {        
    	// id
    	int rowID = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_ROWID));
    	
        // name
        String name = emp.getString(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_NAME));

        // status
        int status = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_STATUS));

        // pay
        int pay = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PAY));
        
        // payrate
        String payrate = emp.getString(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PAYRATE));
        
        // payunit
        int payunit = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PAYUNIT));            

        // hourly precision
        int precision = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PRECISION));            

        // overtime
        int overtime = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_OVERTIME));
        
        return new Employee(rowID, name, status, pay, payrate, payunit, precision, overtime);
    }
    
    private void updateDisplay() {
    	int days = 0;
    	int hours = 0;
    	int minutes = 0;
    	int overtimeHours = 0;
    	int overtimeMinutes = 0;
    	for(int i = 0; i < 7; i++) {
			int dateID = getResources().getIdentifier("date_" + i, "id", getPackageName());
			TextView dateText = (TextView) findViewById(dateID);
			dateText.setText(mDateArray[i]);

			int startID = getResources().getIdentifier("start_" + i, "id", getPackageName());
			Button startButton = (Button) findViewById(startID);	
			if(mDisplayStart[i] != null) {
				startButton.setText(formatDisplayTime(mDisplayStart[i]));
			} else {
				startButton.setText("        ");
			}				
			
			int stopID = getResources().getIdentifier("stop_" + i, "id", getPackageName());
			Button stopButton = (Button) findViewById(stopID);
			if(mDisplayStop[i] != null) {
				stopButton.setText(formatDisplayTime(mDisplayStop[i]));
			} else {
				stopButton.setText("        ");
			}

			int clearID = getResources().getIdentifier("clear_" + i, "id", getPackageName());
			Button clearButton = (Button) findViewById(clearID);
			
			switch(i) {
				case 0: 
					startButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(MON_START_DIALOG);
						}
					});
					stopButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(MON_STOP_DIALOG);
						}
					});
					clearButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							clearDay(0);
						}
					});
					break;
				case 1: 
					startButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(TUE_START_DIALOG);
						}
					});
					stopButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(TUE_STOP_DIALOG);
						}
					});
					clearButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							clearDay(1);
						}
					});
					break;
				case 2: 
					startButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(WED_START_DIALOG);
						}
					});
					stopButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(WED_STOP_DIALOG);
						}
					});
					clearButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							clearDay(2);
						}
					});
					break;
				case 3: 
					startButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(THU_START_DIALOG);
						}
					});
					stopButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(THU_STOP_DIALOG);
						}
					});
					clearButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							clearDay(3);
						}
					});
					break;
				case 4: 
					startButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(FRI_START_DIALOG);
						}
					});
					stopButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(FRI_STOP_DIALOG);
						}
					});
					clearButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							clearDay(4);
						}
					});
					break;
				case 5: 
					startButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(SAT_START_DIALOG);
						}
					});
					stopButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(SAT_STOP_DIALOG);
						}
					});
					clearButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							clearDay(5);
						}
					});
					break;
				case 6: 
					startButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(SUN_START_DIALOG);
						}
					});
					stopButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							showDialog(SUN_STOP_DIALOG);
						}
					});
					clearButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							clearDay(6);
						}
					});
					break;
			}

			if(mEmployee.getPayunit() == Employee.HOURLY) {
				if(mDisplayStart[i] != null && mDisplayStop[i] != null) {
					Period p = new Period(mDisplayStart[i], mDisplayStop[i]);
					int currentHour = p.getHours();
					int currentMinute = p.getMinutes();

					if(mEmployee.isOvertime()) {
						if((currentHour > 8) || (currentHour == 8 && currentMinute > 0)) {
							overtimeHours += currentHour - 8;
							overtimeMinutes += currentMinute;
							
							currentHour -= (currentHour - 8);
							currentMinute = 0;
						}
					}
					
					hours += currentHour;
					minutes += currentMinute;
				}
			} else if(mEmployee.getPayunit() == Employee.DAILY) {
				if(mDisplayStart[i] != null && mDisplayStop[i] != null) {
					days += 1;
				}
			}
    	}

		TextView totalText = (TextView) findViewById(R.id.total_text);
		String totalStr = "";
    	if(mEmployee.getPayunit() == Employee.HOURLY) {
	    	hours += minutes / 60;
	    	minutes = minutes % 60;
	        String format = String.format("%%0%dd", 2);
			totalStr = Long.toString(hours)+":"+String.format(format, minutes);
			
			if(mEmployee.isPaid()) {
		    	double pay = mEmployee.getPayrate();
	    				    	
		    	double totTime = hours + (minutes / 60.0);
		    	double totPay = pay * totTime;
		    	
				if(mEmployee.isOvertime()) {
			    	overtimeHours += overtimeMinutes / 60;
			    	overtimeMinutes = overtimeMinutes % 60;
			    	
			    	double totOvertime = overtimeHours + (overtimeMinutes / 60.0);
			    	double overtimePay = 1.5 * totOvertime * pay;

			    	totPay += overtimePay;
			    	
					totalStr += " (" + Long.toString(overtimeHours)+":"+String.format(format, overtimeMinutes) + ")";
				}
				
				NumberFormat formatter = new DecimalFormat("#0.00");
		    	totalStr += " : $" + formatter.format(totPay);
			}
			
			totalText.setText(totalStr);
    	} else if(mEmployee.getPayunit() == Employee.DAILY) {
    		totalStr = Integer.toString(days);
    		
    		if(mEmployee.isPaid()) {
    			double pay = mEmployee.getPayrate();
    			double totPay = days * pay;
    			
				NumberFormat formatter = new DecimalFormat("#0.00");    			
    			totalStr += " : $" + formatter.format(totPay);
    		}
    		
			totalText.setText(totalStr);
    	}
		
		Button saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveAll();
				finish();
			}
		});

		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
    }
    
    private void saveAll() {
    	for(int i = 0; i < 7; i ++) {
    		if(mDisplayStart[i] != null && mDisplayStop[i] != null) {
    			mDbHelper.saveTime(mRowId, mWeek.plusDays(i).getMillis(), mDisplayStart[i].getMillis(), mDisplayStop[i].getMillis());
    		} else {
    			mDbHelper.deleteTime(mRowId, mWeek.plusDays(i).getMillis());
    		}
    	}
    }
    
    private void clearDay(int day) {
    	mDisplayStart[day] = null;
    	mDisplayStop[day] = null;
    	updateDisplay();
    }
    
    public static String formatDisplayTime(DateTime t) {
		String AMPMStr = "AM";
		int hour = t.getHourOfDay();
		String hourStr = Integer.toString(hour);
		if(hour > 12) {
			hour -= 12;
			hourStr = Integer.toString(hour);
			AMPMStr = "PM";
		} else if(hour == 12) {
			AMPMStr = "PM";
		} else if(hour == 0) {
			hour = 12;
			hourStr = "12";
		}
		
		if(hour < 10) {
			hourStr = "0" + hourStr;
		}

		int min = t.getMinuteOfHour();
		String minStr = Integer.toString(min);
		if(min < 10) {
			minStr = "0" + minStr;
		}
		
		return hourStr + ":" + minStr + " " + AMPMStr;    	
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case MON_START_DIALOG:
        	return dialogBuilder(mMonStartListener, mDisplayStart[0], 8);
        case MON_STOP_DIALOG:
        	return dialogBuilder(mMonStopListener, mDisplayStop[0], 16);
        case TUE_START_DIALOG:
        	return dialogBuilder(mTueStartListener, mDisplayStart[1], 8);
        case TUE_STOP_DIALOG:
        	return dialogBuilder(mTueStopListener, mDisplayStop[1], 16);
        case WED_START_DIALOG:
        	return dialogBuilder(mWedStartListener, mDisplayStart[2], 8);
        case WED_STOP_DIALOG:
        	return dialogBuilder(mWedStopListener, mDisplayStop[2], 16);
        case THU_START_DIALOG:
        	return dialogBuilder(mThuStartListener, mDisplayStart[3], 8);
        case THU_STOP_DIALOG:
        	return dialogBuilder(mThuStopListener, mDisplayStop[3], 16);
        case FRI_START_DIALOG:
        	return dialogBuilder(mFriStartListener, mDisplayStart[4], 8);
        case FRI_STOP_DIALOG:
        	return dialogBuilder(mFriStopListener, mDisplayStop[4], 16);
        case SAT_START_DIALOG:
        	return dialogBuilder(mSatStartListener, mDisplayStart[5], 8);
        case SAT_STOP_DIALOG:
        	return dialogBuilder(mSatStopListener, mDisplayStop[5], 16);
        case SUN_START_DIALOG:
        	return dialogBuilder(mSunStartListener, mDisplayStart[6], 8);
        case SUN_STOP_DIALOG:
        	return dialogBuilder(mSunStopListener, mDisplayStop[6], 16);
        }
        return null;
    }
    
    private TimePickerDialog dialogBuilder(TimePickerDialog.OnTimeSetListener listener, DateTime dateTime, int prefill) {
    	if(dateTime != null) {
    		return new TimePickerDialog(this, listener, dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), false);
    	} else {
    		return new TimePickerDialog(this, listener, prefill, 0, false);
    	}
    }
    
    private void processStop(int day, int hourOfDay, int minute) {
    	DateTime t = new DateTime(mWeek.plusDays(day).getYear(), mWeek.plusDays(day).getMonthOfYear(), mWeek.plusDays(day).getDayOfMonth(), hourOfDay, minute);
    	mActualStop[day] = t;

    	switch(mEmployee.getPrecision()) {
    	case 6:
    		minute = calc6(minute);
    		break;
    	case 15:
    		minute = calc15(minute);
    		break;
    	}
    	
    	if(minute == -1) {
    		minute = 0;
    		hourOfDay += 1;
    	}

    	t = new DateTime(mWeek.plusDays(0).getYear(), mWeek.plusDays(0).getMonthOfYear(), mWeek.plusDays(0).getDayOfMonth(), hourOfDay, minute);
    	mDisplayStop[day] = t;
    	
    	DateTime s = mDisplayStart[day];
    	if(s != null) {
	    	if(t.isBefore(s.getMillis())) {
	    		t = new DateTime(mWeek.plusDays(day).getYear(), mWeek.plusDays(day).getMonthOfYear(), mWeek.plusDays(day + 1).getDayOfMonth(), hourOfDay, minute);
	    		mDisplayStop[day] = t;
	    	}
    	}
    	updateDisplay();
    }
    
    private void processStart(int day, int hourOfDay, int minute) {
    	DateTime t = new DateTime(mWeek.plusDays(0).getYear(), mWeek.plusDays(0).getMonthOfYear(), mWeek.plusDays(0).getDayOfMonth(), hourOfDay, minute);
    	mActualStart[day] = t;

    	switch(mEmployee.getPrecision()) {
    	case 6:
    		minute = calc6(minute);
    		break;
    	case 15:
    		minute = calc15(minute);
    		break;
    	}
    	
    	if(minute == -1) {
    		minute = 0;
    		hourOfDay += 1;
    	}
    	
    	t = new DateTime(mWeek.plusDays(0).getYear(), mWeek.plusDays(0).getMonthOfYear(), mWeek.plusDays(0).getDayOfMonth(), hourOfDay, minute);
    	mDisplayStart[day] = t;
    	updateDisplay();    	
    }
       
    private static int calc15(int min) {
    	if(min > 0 && min <= 15) {
    		return 15;
    	} else if(min > 15 && min <= 30) {
    		return 30;
    	} else if(min > 30 && min <= 45) {
    		return 45;
    	} else if(min > 45) {
    		return -1;
    	}
    	
    	// min == 0
    	return 0;
    }
    
    private static int calc6(int min) {
    	if(min > 0 && min <= 6) {
    		return 6;
    	} else if(min > 6 && min <= 12) {
    		return 12;
    	} else if(min > 12 && min <= 18) {
    		return 18;
    	} else if(min > 18 && min <= 24) {
    		return 24;
    	} else if(min > 24 && min <= 30) {
    		return 30;
    	} else if(min > 30 && min <= 36) {
    		return 36;
    	} else if(min > 36 && min <= 42) {
    		return 42;
    	} else if(min > 42 && min <= 48) {
    		return 48;
    	} else if(min > 48 && min <= 54) {
    		return 54;
    	} else if(min > 54) {
    		return -1;
    	}
    	
    	// min == 0
    	return 0;
    }
    
    private TimePickerDialog.OnTimeSetListener mMonStartListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            	processStart(0, hourOfDay, minute);
            }
        };

    private TimePickerDialog.OnTimeSetListener mMonStopListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            	processStop(0, hourOfDay, minute);
            }
        };
            
    private TimePickerDialog.OnTimeSetListener mTueStartListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            	processStart(1, hourOfDay, minute);
            }
        };

    private TimePickerDialog.OnTimeSetListener mTueStopListener =
        new TimePickerDialog.OnTimeSetListener() {
        	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            	processStop(1, hourOfDay, minute);
            }
        };

    private TimePickerDialog.OnTimeSetListener mWedStartListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStart(2, hourOfDay, minute);
    		}
    	};

    private TimePickerDialog.OnTimeSetListener mWedStopListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStop(2, hourOfDay, minute);
    		}
    	};
            
    private TimePickerDialog.OnTimeSetListener mThuStartListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStart(3, hourOfDay, minute);
    		}
    	};

    private TimePickerDialog.OnTimeSetListener mThuStopListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStop(3, hourOfDay, minute);
    		}
    	};

    private TimePickerDialog.OnTimeSetListener mFriStartListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStart(4, hourOfDay, minute);
    		}
    	};

    private TimePickerDialog.OnTimeSetListener mFriStopListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStop(4, hourOfDay, minute);
    		}
    	};
        
    private TimePickerDialog.OnTimeSetListener mSatStartListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStart(5, hourOfDay, minute);
    		}
    	};

    private TimePickerDialog.OnTimeSetListener mSatStopListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStop(5, hourOfDay, minute);
    		}
    	};
        
    private TimePickerDialog.OnTimeSetListener mSunStartListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStart(6, hourOfDay, minute);
    		}
    	};

    private TimePickerDialog.OnTimeSetListener mSunStopListener =
    	new TimePickerDialog.OnTimeSetListener() {
    		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    			processStop(6, hourOfDay, minute);
    		}
    	};
                    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PayrollDbAdapter.KEY_EMPS_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
