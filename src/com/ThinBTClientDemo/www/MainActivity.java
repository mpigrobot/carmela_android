package com.ThinBTClientDemo.www;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import com.ThinBTClientDemo.www.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
public class MainActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	public String forword;
	public String back;
	public String left;
	public String right;
	public String stop;
	public String address;
	public Context othercontext;
	public SharedPreferences sp1;
	public SharedPreferences spf;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//Intent intent = new Intent(MainActivity.this,MyService.class);
		//startService(intent);

		//添加Activity到容器中，并获取唯一的MyApplication实例
		MyApplication.getInstance().addActivity(this);

		//模式一按钮按下
		Button btn01 = (Button)findViewById(R.id.btn01);
		btn01.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				comfirm_pair_cmd();
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();
				//bluetoothAdapter.cancelDiscovery();
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
				
				try {
					if((forword == "") || (back == "") || (left == "") || 
							(right == "") || (stop == "")){
						display_Toast(R.string.cmd_no_config);
						return;
					}
					if(address == ""){
						display_Toast(R.string.no_bt_pair);
						return; 
					}
					boolean enable_Check = bt_Enable_Check();
					if(enable_Check){
						if (device.getBondState() != BluetoothDevice.BOND_BONDED){
							display_Toast(R.string.bt_no_pair);
							return;
						}
						mode01(); 
					}
				} catch(Exception e){
					display_Toast(R.string.btn_e01);
				}
			}
		});

		//模式二按钮按下
		Button btn02 = (Button)findViewById(R.id.btn02);
		btn02.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {

				comfirm_pair_cmd();
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();
				//bluetoothAdapter.cancelDiscovery();
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
				
				try {
					if((forword == "") || (back == "") || (left == "") || 
							(right == "") || (stop == "")){
						display_Toast(R.string.cmd_no_config);
						return;
					}//检查指令中是否有空的
					if(address == ""){//检查是否进行过蓝牙配对
						display_Toast(R.string.no_bt_pair);
						return; 
					}
					boolean enable_Check = bt_Enable_Check();
					if(enable_Check){
						if (device.getBondState() != BluetoothDevice.BOND_BONDED){
							display_Toast(R.string.bt_no_pair);
							return;
						}//检查储存的蓝牙当前是否为配对状态
						mode02(); 
					}
				} catch (Exception e) {
					display_Toast(R.string.btn_e02);
				}
			}
		});

		//模式三按钮按下
		Button btn03 = (Button)findViewById(R.id.btn03);
		btn03.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				comfirm_pair_cmd();
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();
				//bluetoothAdapter.cancelDiscovery();
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
				
				try {
					if((forword == "") || (back == "") || (left == "") || 
							(right == "") || (stop == "")){
						display_Toast(R.string.cmd_no_config);
						return;
					}
					if(address == ""){
						display_Toast(R.string.no_bt_pair);
						return; 
					}
					boolean enable_Check = bt_Enable_Check();
					if(enable_Check){
						if (device.getBondState() != BluetoothDevice.BOND_BONDED){
							display_Toast(R.string.bt_no_pair);
							return;
						}
						mode03(); 
					}
				} catch (Exception e) {
					display_Toast(R.string.btn_e03);
				}
			}
		});
	}

	//以下为menu生成及其事件处理
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu subMenu = menu.addSubMenu(0, 00, 0, R.string.app_mode);
		subMenu.add(0, 10, 0, R.string.mode01);
		subMenu.add(0, 11, 1, R.string.mode02);
		subMenu.add(0, 12, 2, R.string.mode03);
		menu.add(0, 01, 1, R.string.app_cmd);
		menu.add(0, 02, 2, R.string.app_service);
		menu.add(0, 03, 3, R.string.app_exit);

		//使用XML布局menu时，使用以下代码
		//MenuInflater inflater = getMenuInflater();
		//inflater.inflate(R.menu.menu_item01, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		Intent intent = new Intent(MainActivity.this,MyService.class);
		stopService(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 03:
			app_Exit();
			return true;
		case 01:
			app_cmd();
			return true;
		case 02:
			app_service();
			return true;
		case 10:
			mode01();
			return true;
		case 11:
			mode02();
			return true;
		case 12:
			mode03();
			return true;
		}
		return false;
	}

	public void display_Toast(int id) {
		Toast toast = Toast.makeText(this, getResources().getString(id), Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 20);
		toast.show();
	}
	public void mode01(){
		Intent intent = new Intent(this, TouchKey.class);
		startActivity(intent);
	}
	public void mode02(){
		Intent intent = new Intent(this, Accelerometer.class);
		startActivity(intent);
	}
	public void mode03(){
		Intent intent = new Intent(this, Hand.class);
		startActivity(intent);
	}
	public void app_Exit(){
		new AlertDialog.Builder(MainActivity.this) 
		.setTitle(R.string.confirm)
		.setMessage(R.string.confirm_q)
		.setPositiveButton(R.string.exit, listener)
		.setNegativeButton(R.string.no, listener)
		.show();
	}
	public void app_cmd(){
		Intent intent = new Intent(this, SettingCmd.class);
		startActivity(intent);
	}
	public void app_service(){
		Intent intent = new Intent(this, MyBTSearch.class);
		startActivity(intent);
	}

	@Override
	//首页按下返回键，来退出系统时的退出对话框（按需要添加）
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK )  
		{  
			// 创建退出对话框  
			AlertDialog isExit = new AlertDialog.Builder(this).create();  
			// 设置对话框标题  
			isExit.setTitle(R.string.confirm);  
			// 设置对话框消息  
			isExit.setMessage(getResources().getString(R.string.confirm_q));  
			// 添加选择按钮并注册监听  
			isExit.setButton(getResources().getString(R.string.exit), listener);  
			isExit.setButton2(getResources().getString(R.string.no), listener);  
			// 显示对话框  
			isExit.show();  
		}  
		return false;
	} 

	/**监听退出对话框里面的button点击事件*/  
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
	{  
		public void onClick(DialogInterface dialog, int which)  
		{  
			switch (which)  
			{  
			case AlertDialog.BUTTON_POSITIVE:// 退出
				//关闭启动的所有activity
				MyApplication.getInstance().exit(); 
				break;  
			case AlertDialog.BUTTON_NEGATIVE:// 取消  
				break;  
			default:  
				break;  
			}  
		} 	    
	};

	public boolean bt_Enable_Check(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			display_Toast(R.string.connect_e01);
			return false;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			display_Toast(R.string.connect_e02);
			return false;
		}
		return true;
	}

	//验证蓝牙已配对和指令已配置
	public void comfirm_pair_cmd(){
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

		//获取xml中的蓝牙地址
		try {  
			othercontext = createPackageContext("com.ThinBTClientDemo.www",  
					Context.CONTEXT_IGNORE_SECURITY);  
			spf = othercontext.getSharedPreferences("btaddr",othercontext.MODE_PRIVATE);  
		} catch (NameNotFoundException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}
		address = spf.getString("btAddress", "");
		
	}
}