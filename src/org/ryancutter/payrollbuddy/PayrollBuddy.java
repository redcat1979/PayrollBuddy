package org.ryancutter.payrollbuddy;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class PayrollBuddy extends ListActivity {
	private static final int EMP_MAIN = 1;
	private static final int EMP_PROFILE = 2;
	private static final int PAYROLL_PROFILE = 3;
	private static final int PAYROLL_EXPORT = 4;
	
	public static final int INSERT_ID = Menu.FIRST;
	public static final int PROFILE_ID = Menu.FIRST + 1;
	public static final int EXPORT_ID = Menu.FIRST + 2;
	
	private PayrollDbAdapter mDbHelper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payrollbuddy);
        mDbHelper = new PayrollDbAdapter(this);
        mDbHelper.open();
        fillData(); 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, PROFILE_ID, 0, R.string.menu_profile);
        menu.add(0, EXPORT_ID, 0, R.string.menu_export);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case INSERT_ID:
            createEmp();
            return true;
    	case PROFILE_ID:
            Intent i = new Intent(this, PayrollProfile.class);
            startActivityForResult(i, PAYROLL_PROFILE);    		
    		return true;
    	case EXPORT_ID:
            Intent i2 = new Intent(this, Export.class);
            startActivityForResult(i2, PAYROLL_EXPORT);    		
    		return true;
    	}
        return super.onOptionsItemSelected(item);
    }
        
    private void createEmp() {
    	Cursor c = mDbHelper.fetchCounter();
    	long counter = c.getLong(c.getColumnIndexOrThrow(PayrollDbAdapter.KEY_PROFILE_COUNTER)) + 1;
        String empName = "Employee " + counter;    	
        long id = mDbHelper.createEmp(empName);
        mDbHelper.incCounter(counter);
        
        Intent i = new Intent(this, EmpProfile.class);
        i.putExtra(PayrollDbAdapter.KEY_EMPS_ROWID, id);
        startActivityForResult(i, EMP_PROFILE);
    }
    
    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllEmps();
        startManagingCursor(c);

        String[] from = new String[] { PayrollDbAdapter.KEY_EMPS_NAME };
        int[] to = new int[] { R.id.text1 };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter emps =
            new SimpleCursorAdapter(this, R.layout.emp_row, c, from, to);
        setListAdapter(emps);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, EmpMain.class);
        i.putExtra(PayrollDbAdapter.KEY_EMPS_ROWID, id);
        startActivityForResult(i, EMP_MAIN);
    }
    
}
