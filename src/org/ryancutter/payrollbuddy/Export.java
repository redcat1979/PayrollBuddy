package org.ryancutter.payrollbuddy;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class Export extends Activity {
    private Spinner mEmployeesSpinner;
    private PayrollDbAdapter mDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PayrollDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.export);

        mEmployeesSpinner = (Spinner) findViewById(R.id.employees);
        
        Button empRunButton = (Button) findViewById(R.id.employees_run);
        Button dateRunButton = (Button) findViewById(R.id.date_run);
	    final DatePicker startPicker = (DatePicker) findViewById(R.id.startpicker);
	    final DatePicker stopPicker = (DatePicker) findViewById(R.id.stoppicker);

        populateFields();

        empRunButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		runEmployees();
        		setResult(RESULT_OK);
        	    finish();
            }
        });

        dateRunButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		runDates(startPicker.getYear(), startPicker.getMonth()+1, startPicker.getDayOfMonth(), 
        				stopPicker.getYear(), stopPicker.getMonth()+1, stopPicker.getDayOfMonth());
        		setResult(RESULT_OK);
        	    finish();
            }
        });
    }
    
    /**
     * fill profile screen with current data
     */
    private void populateFields() {
        Cursor cur = mDbHelper.fetchAllEmps();
        startManagingCursor(cur);
        
        // employees spinner
        SimpleCursorAdapter employeesAdapter = new SimpleCursorAdapter(this,
        	    android.R.layout.simple_spinner_item, cur, new String[] {PayrollDbAdapter.KEY_EMPS_NAME}, 
        	    new int[] {android.R.id.text1});
        employeesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEmployeesSpinner.setAdapter(employeesAdapter);
        mEmployeesSpinner.setSelection(0);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    /*
     * export by dates
     */
    private void runDates(int startYear, int startMonth, int startDay, int stopYear, int stopMonth, int stopDay) {
		DateTime start = new DateTime(startYear, startMonth, startDay, 0, 0);
		DateTime stop = new DateTime(stopYear, stopMonth, stopDay, 0, 0);
		
		if(start.getMillis() <= stop.getMillis()) {
	        Cursor time = mDbHelper.fetchTimes(start.getMillis(), stop.getMillis());
	        startManagingCursor(time);

	        String body = getRows(time);
	        
	        sendEmail(body);
		}
    }

    private String getRows(Cursor time) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/YYYY");
        String rows = "Employee,Date,Start,Stop,Pay\n";
        time.moveToFirst();
        while (time.isAfterLast() == false) {
        	int empID = time.getInt(time.getColumnIndexOrThrow(PayrollDbAdapter.KEY_TIME_EMPID));
        	Cursor emp = mDbHelper.fetchEmp(empID);
        	startManagingCursor(emp);        	
        	Employee employee = EmpWeek.buildEmployee(emp);

        	long t = time.getLong(time.getColumnIndexOrThrow(PayrollDbAdapter.KEY_TIME_DATE));
        	long start = time.getLong(time.getColumnIndexOrThrow(PayrollDbAdapter.KEY_TIME_START));
        	DateTime startDT = new DateTime(EmpWeek.formatTime(start, employee.getPrecision()));
        	long stop = time.getLong(time.getColumnIndexOrThrow(PayrollDbAdapter.KEY_TIME_STOP));
        	DateTime stopDT = new DateTime(EmpWeek.formatTime(stop, employee.getPrecision()));
        	        	
        	String empName = emp.getString(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_NAME));
        	emp.close();

        	String payStr = "";
        	if(employee.isPaid()) {
        		double totPay = 0.0;
				double pay = employee.getPayrate();
				if(employee.getPayunit() == Employee.HOURLY) {
					Period p = new Period(startDT, stopDT);
					int hours = p.getHours();
					int minutes = p.getMinutes();
					
					if(employee.isOvertime()) {
						if((hours > 8) || (hours == 8 && minutes > 0)) {
							int overtimeHours = hours - 8;
							int overtimeMinutes = minutes;
							
							hours -= (hours - 8);
							minutes = 0;

					    	double totOvertime = overtimeHours + (overtimeMinutes / 60.0);
					    	double overtimePay = 1.5 * totOvertime * pay;
	
					    	totPay = overtimePay;
						}
					}
			    	double totTime = hours + (minutes / 60.0);
			    	totPay += (pay * totTime);
				} else if(employee.getPayunit() == Employee.DAILY) {
					totPay = pay;
				}
				NumberFormat formatter = new DecimalFormat("#0.00");
				payStr = "$" + formatter.format(totPay);
	    	}
        	
        	rows += empName + "," + fmt.print(t) + "," + EmpWeek.formatDisplayTime(startDT) + "," + EmpWeek.formatDisplayTime(stopDT) + 
        				"," + payStr + "\n";
            time.moveToNext();
        }    	
        
        return rows;
    }
    
    /*
     * export by employee
     */
    private void runEmployees() {
        Cursor emp = (Cursor)(mEmployeesSpinner.getSelectedItem());
        startManagingCursor(emp);

        String empName = emp.getString(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_NAME));
        
        Cursor time = mDbHelper.fetchAllTime(emp.getLong(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_ROWID)));
        startManagingCursor(time);
        
		String body = getRows(time);
        
        sendEmail(body);
    }   
    
    private void sendEmail(String body) {
        Cursor profile = mDbHelper.fetchProfile();
        startManagingCursor(profile);
        String toAddr = profile.getString(profile.getColumnIndexOrThrow(PayrollDbAdapter.KEY_PROFILE_EMAIL));
    	
        String subject = "PayrollBuddy Export Report";
        
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text"); 
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {toAddr});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
