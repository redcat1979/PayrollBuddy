package org.ryancutter.payrollbuddy;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*; 

public class EmpProfile extends Activity {
    private EditText mNameText;
    private CheckBox mStatusCheckbox;
    private CheckBox mPayCheckbox;
    private CheckBox mOvertimeCheckbox;
    private Spinner mPrecisionSpinner;
    private Spinner mPayUnitsSpinner;
    private EditText mPayRateText;
    private Long mRowId;
    private PayrollDbAdapter mDbHelper;
    
    public static final int DELETE_ID = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PayrollDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.emp_profile);

        mNameText = (EditText) findViewById(R.id.name);
		mStatusCheckbox = (CheckBox) findViewById(R.id.status);
		mPayCheckbox = (CheckBox) findViewById(R.id.pay);
        mPayRateText = (EditText) findViewById(R.id.payrate);
        mPayUnitsSpinner = (Spinner) findViewById(R.id.payunit);
        mPrecisionSpinner = (Spinner) findViewById(R.id.precision);
        mOvertimeCheckbox = (CheckBox) findViewById(R.id.overtime);
        
        Button saveButton = (Button) findViewById(R.id.save_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);

        // get profile data
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(PayrollDbAdapter.KEY_EMPS_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(PayrollDbAdapter.KEY_EMPS_ROWID)
									: null;
		}

		populateFields();

		// wire checkboxes and buttons
		mStatusCheckbox.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        if (((CheckBox) v).isChecked()) {
		            Toast.makeText(EmpProfile.this, "Employee Active", Toast.LENGTH_SHORT).show();
		        } else {
		            Toast.makeText(EmpProfile.this, "Employee Not Active", Toast.LENGTH_SHORT).show();
		        }
		    }
		});

		// if employee will not be paid, no need to track pay data
		mPayCheckbox.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        if (((CheckBox) v).isChecked()) {
		            Toast.makeText(EmpProfile.this, "Employee will be paid", Toast.LENGTH_SHORT).show();
		            mPayRateText.setEnabled(true);
		            mPayUnitsSpinner.setEnabled(true);
		            mPrecisionSpinner.setEnabled(true);
		            mOvertimeCheckbox.setEnabled(true);
		        } else {
		            Toast.makeText(EmpProfile.this, "Employee will not be paid", Toast.LENGTH_SHORT).show();
		            mPayRateText.setEnabled(false);
		            mPayUnitsSpinner.setEnabled(false);
		            mPrecisionSpinner.setEnabled(false);
		            mOvertimeCheckbox.setEnabled(false);
		        }
		    }
		});
		
		mOvertimeCheckbox.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        if (((CheckBox) v).isChecked()) {
		            Toast.makeText(EmpProfile.this, "Overtime rules enabled", Toast.LENGTH_SHORT).show();
		        } else {
		            Toast.makeText(EmpProfile.this, "Overtime rules disabled", Toast.LENGTH_SHORT).show();
		        }
		    }
		});

		// if pay unit is "daily", no need to track precision or overtime values
		mPayUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) { 
		        if (i == 1) {
    	            mPrecisionSpinner.setEnabled(false);
    	            mOvertimeCheckbox.setEnabled(false);            	            		
		        } else {
    	            mPrecisionSpinner.setEnabled(true);
    	            mOvertimeCheckbox.setEnabled(true);            	            		
		        }
		    }
			
			public void onNothingSelected(AdapterView<?> adapterView) {
		        return;
		    } 
		});
		
        saveButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		saveState();
        		setResult(RESULT_OK);
        	    finish();
            }
        });
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		setResult(RESULT_OK);
        	    finish();
            }
        });
    }
    
    /**
     * fill profile screen with current data
     */
    private void populateFields() {
        if (mRowId != null) {
            Cursor emp = mDbHelper.fetchEmp(mRowId);
            startManagingCursor(emp);
            
            // name
            mNameText.setText(emp.getString(
                    emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_NAME)));

            // status
            int status = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_STATUS));
            mStatusCheckbox.setChecked(false);
            if(status == 1) {
            	mStatusCheckbox.setChecked(true);
            }

            // pay
            int pay = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PAY));
            mPayCheckbox.setChecked(false);
            if(pay == 1) {
            	mPayCheckbox.setChecked(true);
            }
            
            // payrate
            mPayRateText.setText(emp.getString(
                    emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PAYRATE)));
            
            // payunit
            int payunit = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PAYUNIT));            
            ArrayAdapter<CharSequence> payunitAdapter = ArrayAdapter.createFromResource(
                    this, R.array.payunits, android.R.layout.simple_spinner_item);
            payunitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mPayUnitsSpinner.setAdapter(payunitAdapter);
            mPayUnitsSpinner.setSelection(payunit);

            // hourly precision
            int precision = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_PRECISION));            
            ArrayAdapter<CharSequence> precisionAdapter = ArrayAdapter.createFromResource(
                    this, R.array.precision, android.R.layout.simple_spinner_item);
            precisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mPrecisionSpinner.setAdapter(precisionAdapter);
            mPrecisionSpinner.setSelection(0);
            if(precision == 6) {
                mPrecisionSpinner.setSelection(1);
            } else if(precision == 1) {
            	mPrecisionSpinner.setSelection(2);
            }

            // overtime
            int overtime = emp.getInt(emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_OVERTIME));
            mOvertimeCheckbox.setChecked(false);
            if(overtime == 1) {
            	mOvertimeCheckbox.setChecked(true);
            }
            
            // if pay disabled, disable pay elements
            if(pay == 0) {
            	mPayRateText.setEnabled(false);
	            mPayUnitsSpinner.setEnabled(false);
	            mPrecisionSpinner.setEnabled(false);
	            mOvertimeCheckbox.setEnabled(false);            	
            } else {
            	// if pay is daily, disable hourly pay elements
            	if(payunit == 1) {
    	            mPrecisionSpinner.setEnabled(false);
    	            mOvertimeCheckbox.setEnabled(false);            	            		
            	}
            }
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(PayrollDbAdapter.KEY_EMPS_ROWID, mRowId);
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
     * save profile data
     */
    private void saveState() {
    	// name
        String name = mNameText.getText().toString();

        // status
        int status = 0;
        if(mStatusCheckbox.isChecked()) {
        	status = 1;
        }

        // pay
        int pay = 0;
        if(mPayCheckbox.isChecked()) {
        	pay = 1;
        }

        // payrate
        String payrate = mPayRateText.getText().toString();
        /*double payrate = new Double(mPayRateText.getText().toString()).doubleValue();
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        String payrateStr = n.format(payrate);*/
        
        // payunits
        int payunits = mPayUnitsSpinner.getSelectedItemPosition();
        
        // hourly precision
        int precision = mPrecisionSpinner.getSelectedItemPosition();
        switch (precision) {
		case 0: precision = 15;
				break;
		case 1: precision = 6;
				break;		
		case 2: precision = 1;
				break;		
        }

        // overtime
        int overtime = 0;
        if(mOvertimeCheckbox.isChecked()) {
        	overtime = 1;
        }

        mDbHelper.updateEmp(mRowId, name, status, pay, payrate, payunits, precision, overtime);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case DELETE_ID:
            deleteEmp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void deleteEmp() {
        mDbHelper.deleteEmp(mRowId);
		setResult(RESULT_CANCELED);
        finish();
    }
}
