package org.ryancutter.payrollbuddy;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class PayrollProfile extends Activity {
    private EditText mEmailText;
    private PayrollDbAdapter mDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PayrollDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.payroll_profile);

        mEmailText = (EditText) findViewById(R.id.email);

        Button saveButton = (Button) findViewById(R.id.save_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);

		populateFields();

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
        Cursor emp = mDbHelper.fetchProfile();
        startManagingCursor(emp);
            
        // email
        mEmailText.setText(emp.getString(
                emp.getColumnIndexOrThrow(PayrollDbAdapter.KEY_PROFILE_EMAIL)));
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
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
    	// email
        String email = mEmailText.getText().toString();

        mDbHelper.updateProfile(email);
    }    
}
