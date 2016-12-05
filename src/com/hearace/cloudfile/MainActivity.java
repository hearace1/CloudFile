package com.hearace.cloudfile;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;
import com.hearace.cloudfile.model.AppConfig;
import com.hearace.cloudfile.model.DBManager;
import com.hearace.cloudfile.service.CloudService;
import com.hearace.cloudfile.service.PollingUtils;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnCheckedChangeListener, OnClickListener, OnTimeSetListener {
	private final int ADD_PATH_CODE = 10;
	private final int CONTEXT_MENU_EDIT = 10001;
	private final int CONTEXT_MENU_DELETE = 10002;
	private final String BACKUP_SERVICE_NAME = "com.hearace.cloudfile.backup";

	private ListView pathList;
	private ToggleButton serviceToggle;
	private TextView username;
	private Button loginBtn;
	private String mbOauth = null;
	private AppConfig config = null;
	private boolean enabledService;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pathList = (ListView) findViewById(R.id.pathList);
		registerForContextMenu(pathList);
		serviceToggle = (ToggleButton) findViewById(R.id.serviceToggle);
		serviceToggle.setOnCheckedChangeListener(this);
		username = (TextView) findViewById(R.id.username_area);
		loginBtn = (Button) findViewById(R.id.loginBtn);

		fillData();
	}

	private void fillData() {
		DBManager dbMgr = DBManager.getInstance(this);
		Cursor cur = dbMgr.getAllPath();

		if (config == null) {
			config = AppConfig.getInstance(dbMgr);
			config.init();
		}
		Log.d("MainActivity", "Find " + cur.getCount() + " items from the DB");
		startManagingCursor(cur);

		ListAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.path_list_item, cur,
				new String[] { "localpath", "cloudpath", "LastProcessDate", "_id" },
				new int[] { R.id.pathInItem, R.id.cloudPathInItem, R.id.lastUpdateTimeInItem, R.id.pathIdInItem });

		pathList.setAdapter(listAdapter);
		
		enabledService = PollingUtils.isSetPollingService(this, CloudService.class, BACKUP_SERVICE_NAME);
		serviceToggle.setChecked(enabledService);

		if (config.getUserName() != null && config.getServerToken() != null) {
			username.setText(config.getUserName());
			// loginBtn.setEnabled(false);
		}
	}

	public void addPathOnClick(View view) {
		Intent intent = new Intent(this, PathConfigure.class);
		intent.putExtra("pathID", "new");
		startActivityForResult(intent, ADD_PATH_CODE);
	}

	public void loginOnClick(View view) {
		BaiduOAuth oauthClient = new BaiduOAuth();
		oauthClient.startOAuth(this, AppDefine.mbApiKey, new String[] { "basic", "netdisk" },
				new BaiduOAuth.OAuthListener() {
					@Override
					public void onException(String msg) {
						Toast.makeText(getApplicationContext(), "Login failed " + msg, Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onComplete(BaiduOAuthResponse response) {
						if (null != response) {
							mbOauth = response.getAccessToken();
							Toast.makeText(getApplicationContext(),
									"Token: " + mbOauth + "    User name:" + response.getUserName(), Toast.LENGTH_SHORT)
									.show();
							DBManager dbMgr = DBManager.getInstance(context);
							if(dbMgr.getAllPath().getCount()==0){
								
							}
							config.setUserName(response.getUserName());
							config.setServerToken(response.getAccessToken());								
							
							fillData();
						}
					}

					@Override
					public void onCancel() {
						Toast.makeText(getApplicationContext(), "Login cancelled", Toast.LENGTH_SHORT).show();
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADD_PATH_CODE && resultCode == RESULT_OK) {
			Log.d("MainActivity", "get result from configure path with ok");
			fillData();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		// int itemId =
		// Integer.parseInt(((TextView)view.findViewById(R.id.pathIdInItem)).getText().toString());
		String path = ((TextView) view.findViewById(R.id.pathInItem)).getText().toString();
		menu.setHeaderTitle(path);
		menu.add(0, CONTEXT_MENU_EDIT, 0, "Edit");
		menu.add(0, CONTEXT_MENU_DELETE, 0, "Delete");
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String pathId = ((TextView) info.targetView.findViewById(R.id.pathIdInItem)).getText().toString();

		Log.d("MainActivity", "click " + item.getItemId() + " on " + pathId);
		switch (item.getItemId()) {
		case CONTEXT_MENU_EDIT:
			Intent intent = new Intent(this, PathConfigure.class);
			intent.putExtra("pathID", pathId);
			startActivityForResult(intent, ADD_PATH_CODE);
			break;
		case CONTEXT_MENU_DELETE:
			DBManager dbMgr = DBManager.getInstance(this);
			dbMgr.delPath(pathId);
			fillData();
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		Log.d("MainActivity", "Switch server with " + (arg1 ? "on" : "off"));
		if (arg1) {
			if(!enabledService){
				TimePickerDialog time = new TimePickerDialog(MainActivity.this, this, 23, 00, true);
				time.setTitle("Select backup time");
				time.setButton(TimePickerDialog.BUTTON_NEGATIVE, "Cancel", this);
				time.show();
				
			}
		} else {
			 PollingUtils.stopPollingService(this, CloudService.class,
			 BACKUP_SERVICE_NAME);
			 enabledService = false;
		}
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		serviceToggle.setChecked(false);
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);
		Log.d("MainActivity", ""+(System.currentTimeMillis() - cal.getTimeInMillis()));
		 PollingUtils.startPollingService(this, cal.getTimeInMillis(), CloudService.class,
				 BACKUP_SERVICE_NAME);
		Toast.makeText(MainActivity.this, hourOfDay + "hour " + minute + "minute", Toast.LENGTH_SHORT)
				.show();
		enabledService = true;
	}
	
	public void viewBakItemOnClick(View view){
		Intent intent = new Intent(this, BackupItemActivity.class);
		startActivity(intent);
	}

}
