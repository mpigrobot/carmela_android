package com.ThinBTClientDemo.www;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.IBinder;

import com.ThinBTClientDemo.www.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TouchKey extends Activity {
	private static final String TAG = "TOUCHKEY"; 
	private static final boolean D = true;
	private ImageButton mButtonF;
	private ImageButton mButtonB;
	private ImageButton mButtonL;
	private ImageButton mButtonR;
	private ImageButton mButtonS;
	public String forword;
	public String back;
	public String left;
	public String right;
	public String stop;
	public Context othercontext;
	public SharedPreferences sp1;
	
	MyReceiver receiver;  
	IBinder serviceBinder;  
	MyService mService;  
	Intent intent;  

	/**************service 命令*********/   
	static final int CMD_STOP_SERVICE = 0x01;  
	static final int CMD_SEND_DATA = 0x02;  
	static final int CMD_SYSTEM_EXIT =0x03;  
	static final int CMD_SHOW_TOAST =0x04; 


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState); 
		setContentView(R.layout.touchkey);
		
		//获取xml中保存的指令
		try {  
			othercontext = createPackageContext("com.ThinBTClientDemo.www",  
					Context.CONTEXT_IGNORE_SECURITY);  
			sp1 = othercontext.getSharedPreferences("btcmd",othercontext.MODE_PRIVATE);  
		} catch (NameNotFoundException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}
		forword = sp1.getString("forword_key", "");
		back = sp1.getString("back_key", "");
		left = sp1.getString("left_key", "");
		right = sp1.getString("right_key", "");
		stop = sp1.getString("stop_key", "");

		//启动服务
		Intent intent = new Intent(TouchKey.this,MyService.class);
		startService(intent);

		final TextView stateView = (TextView)findViewById(R.id.btn_state);

		//添加Activity到容器中，并获取唯一的MyApplication实例
		MyApplication.getInstance().addActivity(this);

		//前进
		mButtonF=(ImageButton)findViewById(R.id.btnF);
		mButtonF.setOnTouchListener(new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					stateView.setText(btn_state(1));
					sendCmd(forword); 
					break;					
				case MotionEvent.ACTION_UP:
					stateView.setText(btn_state(0));
					sendCmd(stop);
					break;
				}
				return false;
			}

		});

		//后退
		mButtonB=(ImageButton)findViewById(R.id.btnB);
		mButtonB.setOnTouchListener(new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					stateView.setText(btn_state(2));
					sendCmd(back);
					break;					
				case MotionEvent.ACTION_UP:
					stateView.setText(btn_state(0));
					sendCmd(stop);
					break;
				}					
				return false;
			}            	       
		});

		//左转
		mButtonL=(ImageButton)findViewById(R.id.btnL);
		mButtonL.setOnTouchListener(new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					stateView.setText(btn_state(3));
					sendCmd(left);
					break;					
				case MotionEvent.ACTION_UP:
					stateView.setText(btn_state(0));
					sendCmd(stop);
					break;
				}					
				return false;					
			}
		});

		//右转
		mButtonR=(ImageButton)findViewById(R.id.btnR);
		mButtonR.setOnTouchListener(new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				int action = event.getAction();
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					stateView.setText(btn_state(4));
					sendCmd(right);
					break;					
				case MotionEvent.ACTION_UP:
					stateView.setText(btn_state(0));
					sendCmd(stop);
					break;
				}					
				return false;				
			}            	        
		});

		//停止
		mButtonS=(ImageButton)findViewById(R.id.btnS);
		mButtonS.setOnTouchListener(new Button.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN)
					stateView.setText(btn_state(0));
					sendCmd(stop);
				return false;
			}           	        
		});

		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.e(TAG, "++ ON START ++");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		//注册广播接收器
		receiver = new MyReceiver();  
		IntentFilter filter=new IntentFilter();  
		filter.addAction("android.intent.action.lxx");  
		TouchKey.this.registerReceiver(receiver,filter);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {

		if(receiver!=null){  //取消注册的广播接收器
			TouchKey.this.unregisterReceiver(receiver);  
		}
		Intent intent = new Intent(TouchKey.this,MyService.class);
		stopService(intent);//关闭服务
		super.onDestroy();
		if (D) Log.e(TAG, "--- ON DESTROY ---");
	}

	public void display_Toast(int id)
	{
		Toast toast=Toast.makeText(this, getResources().getString(id), Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 20);
		toast.show();
	}

	//以下为menu生成及其事件处理
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		SubMenu subMenu = menu.addSubMenu(0, 00, 0, R.string.app_mode);
		subMenu.add(0, 10, 0, R.string.mode02);
		subMenu.add(0, 11, 1, R.string.mode03);
		menu.add(0, 01, 1, R.string.app_orientation);
		menu.add(0, 02, 2, R.string.app_exit);

		//使用XML布局menu时，使用以下代码
		//MenuInflater inflater = getMenuInflater();
		//inflater.inflate(R.menu.menu_item01, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 02:
			app_Exit();
			return true;
		case 01:
			if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}else if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			return true;
		case 10:
			mode02();
			return true;
		case 11:
			mode03();
			return true;
		}
		return false;
	}

	public void mode02(){
		Intent intent = new Intent(this, Accelerometer.class);
		startActivity(intent);
	}
	public void mode03(){
		Intent intent = new Intent(this, Hand.class);
		startActivity(intent);
	}
	public boolean app_Exit(){
		new AlertDialog.Builder(TouchKey.this) 
		.setTitle(R.string.confirm)
		.setMessage(R.string.confirm_q)
		.setPositiveButton(R.string.exit, listener01)
		.setNegativeButton(R.string.no, listener01)
		.show();
		return true;
	}

	//监听退出对话框里面的button点击事件 
	DialogInterface.OnClickListener listener01 = new DialogInterface.OnClickListener()  
	{  
		public void onClick(DialogInterface dialog, int which)  
		{  
			switch (which)  
			{  
			case AlertDialog.BUTTON_POSITIVE:// 退出
				if(receiver!=null){  //取消注册的广播接收器
					TouchKey.this.unregisterReceiver(receiver);  
				}
				Intent intent = new Intent(TouchKey.this,MyService.class);
				stopService(intent);//关闭服务
				//关闭所有的Activity
				MyApplication.getInstance().exit(); 
				break;  
			case AlertDialog.BUTTON_NEGATIVE:// 取消  
				break;  
			default:  
				break;  
			}  
		}
	};

	//获取string中btn_state_array
	public String btn_state(int btn_num){
		final String[] btn_state = getResources().getStringArray(R.array.btn_state_array);
		switch(btn_num){
		case 0:
			return btn_state [0];
		case 1:
			return btn_state [1];
		case 2:
			return btn_state [2];
		case 3:
			return btn_state [3];
		case 4:
			return btn_state [4];
		default:
			return btn_state [0];
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO 自动生成的方法存根
		super.onConfigurationChanged(newConfig);
	}

	public void showToast(String str){//显示提示信息  
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();      
	}  

	public class MyReceiver extends BroadcastReceiver{  
		@Override  
		public void onReceive(Context context, Intent intent) {  
			// TODO Auto-generated method stub  
			if(intent.getAction().equals("android.intent.action.lxx")){  
				Bundle bundle = intent.getExtras();  
				int cmd = bundle.getInt("cmd");  

				if(cmd == CMD_SHOW_TOAST){  
					String str = bundle.getString("str");  
					showToast(str);  
				}else if(cmd == CMD_SYSTEM_EXIT){  
					System.exit(0);  
				}  
			}  
		}     
	}  

	public void sendCmd(String msg){  
		Intent intent = new Intent();//创建Intent对象  
		intent.setAction("android.intent.action.cmd");  
		intent.putExtra("cmd", CMD_SEND_DATA);  
		intent.putExtra("message", msg);  
		sendBroadcast(intent);//发送广播      
	}


}