package org.ryancutter.payrollbuddy;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

public class EmpMain extends Activity {
	private static final int EMP_PROFILE = 1;
	private static final int EMP_DATE = 2;

    private Long mRowId;
    private PayrollDbAdapter mDbHelper;
    
    public static final int PROFILE_ID = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PayrollDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.emp_main);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(PayrollDbAdapter.KEY_EMPS_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(PayrollDbAdapter.KEY_EMPS_ROWID)
									: null;
		}
		
		Cursor profile = mDbHelper.fetchEmp(mRowId);
		startManagingCursor(profile);
		
		TextView header = (TextView) findViewById(R.id.header);
		String name = profile.getString(profile.getColumnIndexOrThrow(PayrollDbAdapter.KEY_EMPS_NAME));
		header.setText(name);

	    final DatePicker datePicker = (DatePicker) findViewById(R.id.datepicker);
	    Button dateButton = (Button) findViewById(R.id.datebutton);

        dateButton.setOnClickListener(new View.OnClickListener() {

        	public void onClick(View view) {
                Intent i = new Intent(EmpMain.this.getApplication(), EmpWeek.class);
                i.putExtra(PayrollDbAdapter.KEY_EMPS_ROWID, mRowId);
                i.putExtra(PayrollDbAdapter.KEY_EMPS_MONTH, (datePicker.getMonth()+1));
                i.putExtra(PayrollDbAdapter.KEY_EMPS_DAY, datePicker.getDayOfMonth());
                i.putExtra(PayrollDbAdapter.KEY_EMPS_YEAR, datePicker.getYear());
                startActivityForResult(i, EMP_DATE);
            }

        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == EMP_PROFILE) {
    		if (resultCode == RESULT_CANCELED) {
    			finish();
    	    }
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, PROFILE_ID, 0, R.string.profile);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case PROFILE_ID:
            Intent i = new Intent(EmpMain.this.getApplication(), EmpProfile.class);
            i.putExtra(PayrollDbAdapter.KEY_EMPS_ROWID, mRowId);
            startActivityForResult(i, EMP_PROFILE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }    
}
