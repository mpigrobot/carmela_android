package com.ThinBTClientDemo.www;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Accelerometer extends Activity implements SensorEventListener {
	private static final String TAG = "ACCELEROMETER";
	private static final boolean D = true;
	private SensorManager sensorManager;
	private Button mButtonP;
	private TextView txtView;
	private TextView txtView02;
	private TextView txtView03;
	private TextView txtView04;
	private ImageView imageView;
	private float x, y, z;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private boolean btn_flag = false;//重力感应操控启动/暂停标志
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private String address;//要连接的蓝牙设备MAC地址
	public String forword;
	public String back;
	public String left;
	public String right;
	public String stop;
	public Context othercontext;
	public SharedPreferences sp1;
	public SharedPreferences sp2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accelerometer);

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
		
		//获取xml中保存的蓝牙地址
		try {  
			othercontext = createPackageContext("com.ThinBTClientDemo.www",  
					Context.CONTEXT_IGNORE_SECURITY);  
			sp2 = othercontext.getSharedPreferences("btaddr",othercontext.MODE_PRIVATE);  
		} catch (NameNotFoundException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}
		address = str_get();
		
		//获取传感器服务
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		//添加Activity到容器中，并获取唯一的MyApplication实例
		MyApplication.getInstance().addActivity(this);

		//定位各个TextView
		txtView = (TextView)findViewById(R.id.btn_state);
		txtView02 = (TextView)findViewById(R.id.accel_x);
		txtView03 = (TextView)findViewById(R.id.accel_y);
		txtView04 = (TextView)findViewById(R.id.accel_z);
		//定位ImageView
		imageView = (ImageView)findViewById(R.id.circle);		

		//暂停/继续
		mButtonP=(Button)findViewById(R.id.btn_pause);
		mButtonP.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				try {//更新button内容
					if(btn_flag){
						mButtonP.setText(R.string.btn_pause02);
						mButtonP.setBackgroundColor(getResources().getColor(R.color.pause_down));
						btn_flag = false;
					}else{
						mButtonP.setText(R.string.btn_pause01);
						mButtonP.setBackgroundColor(getResources().getColor(R.color.pause_up));
						btn_flag = true;
					}

				} catch (Exception e) {
					Log.e(TAG, "Add button failed.", e);
				}
			}
		});

		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			display_Toast(R.string.connect_e01);
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			display_Toast(R.string.connect_e02);
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.e(TAG, "++ ON START ++");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		//获取重力感应传感器
		List<Sensor> sensors = sensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER);

		//设置传感器监听器
		SensorEventListener sensorListener = new SensorEventListener() {  
			public void onSensorChanged(SensorEvent event) {  
				x = event.values[SensorManager.DATA_X];     
				y = event.values[SensorManager.DATA_Y];     
				z = event.values[SensorManager.DATA_Z]; 
				do_msg();
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO 自动生成的方法存根
			}  
		};

		//注册传感器
		if (sensors.size() > 0) {
			sensorManager.registerListener(sensorListener, sensors.get(0),
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		//蓝牙连接
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			Log.e(TAG, "+ fail to create BluetoothSocket +",e);
		}
		mBluetoothAdapter.cancelDiscovery();
		try {
			btSocket.connect();
			display_Toast(R.string.connect_succeed);
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.e(TAG, "+ fail to Socket connect,can't close +",e2);
			}
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
		do_socket_Close();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D) Log.e(TAG, "--- ON DESTROY ---");
	}

	//以下为menu生成及其事件处理
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		SubMenu subMenu = menu.addSubMenu(0, 00, 0, R.string.app_mode);
		subMenu.add(0, 10, 0, R.string.mode01);
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
			mode01();
			return true;
		case 11:
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
	public void mode03(){
		Intent intent = new Intent(this, Hand.class);
		startActivity(intent);
	}
	public boolean app_Exit(){
		new AlertDialog.Builder(Accelerometer.this) 
		.setTitle(R.string.confirm)
		.setMessage(R.string.confirm_q)
		.setPositiveButton(R.string.exit, listener)
		.setNegativeButton(R.string.no, listener)
		.show();
		return true;
	}

	//监听退出对话框里面的button点击事件
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
	{  
		public void onClick(DialogInterface dialog, int which)  
		{  
			switch (which)  
			{  
			case AlertDialog.BUTTON_POSITIVE:// 退出
				do_socket_Close();
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

	//取消注册的监听器，关闭数据输出流和socket
	public void do_socket_Close(){

		//取消注册的监听器，关闭数据输出流和socket
		sensorManager.unregisterListener(this);
		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				Log.e(TAG, "can't flush outStream", e);
			}
		}

		if(btSocket != null){
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.e(TAG, "can't close socket",e2);
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO 自动生成的方法存根

	}

	//执行指令，并更新状态
	public void do_msg(){
		int flag = 0;
		String message;
		byte[] msgBuffer;

		// 这段代码的作用是实时显示获取的重力加速度值。
		int yaxis = (int)y; 
		int xaxis = (int)x; 
		int zaxis = (int)z;

		txtView02.setText(Integer.toString(xaxis));
		txtView03.setText(Integer.toString(yaxis));
		txtView04.setText(Integer.toString(zaxis));
		
		//绘制方向盘
		orientation_Circle();

		// 这里获取Y轴和X轴的重力加速度值，经过试验发现用这两个轴的重力加速度比较合理。
		//z轴对人行走晃动产生的加速度，敏感，其值可能有时会 超出（-10）--（10）区间。
		if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			int tmp1 ;
			tmp1 = xaxis;
			xaxis = yaxis;
			yaxis = tmp1;

			if (xaxis >= 3 && btn_flag) {
				flag = 4;// 标志为4,横屏：车子右转
				txtView.setText(btn_state(4));
			} else if (xaxis <= -3 && btn_flag)
			{
				flag = 3;// 标志为3,横屏：车子左转
				txtView.setText(btn_state(3));
			}else if (yaxis >= 4 && xaxis > -3 && xaxis < 3 && btn_flag) {
				flag = 2;// 标志为2,车子后退
				txtView.setText(btn_state(2));

			} else if (yaxis <= -2 && xaxis > -3 && xaxis < 3 && btn_flag) {
				flag = 1;// 标志为1,车子前进
				txtView.setText(btn_state(1));
			}else {
				flag = 0;// 标志为0,车子停止
				txtView.setText(btn_state(0));
			}

		}else if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			
			if (xaxis >= 3 && btn_flag) {
				flag = 3;// 标志为3,竖屏：车子左转
				txtView.setText(btn_state(3));
			} else if (xaxis <= -3 && btn_flag)
			{
				flag = 4;// 标志为4,竖屏：车子右转
				txtView.setText(btn_state(4));
			}else if (yaxis >= 4 && xaxis > -3 && xaxis < 3 && btn_flag) {
				flag = 2;// 标志为2,车子后退
				txtView.setText(btn_state(2));

			} else if (yaxis <= -2 && xaxis > -3 && xaxis < 3 && btn_flag) {
				flag = 1;// 标志为1,车子前进
				txtView.setText(btn_state(1));
			}else {
				flag = 0;// 标志为0,车子停止
				txtView.setText(btn_state(0));
			}
		}

		switch (flag) {
		case 0:
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				//异常处理
			}

			message = stop;
			msgBuffer = message.getBytes();
			try {
				outStream.write(msgBuffer);
			} catch (IOException e) {
				//异常处理
			}
			break;
		case 1:
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				//异常处理
			}

			message = forword;
			msgBuffer = message.getBytes();
			try {
				outStream.write(msgBuffer);
			} catch (IOException e) {
				//异常处理
			}
			break;
		case 2:
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				//异常处理
			}

			message = back;
			msgBuffer = message.getBytes();
			try {
				outStream.write(msgBuffer);
			} catch (IOException e) {
				//异常处理
			}
			break;
		case 3:
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				//异常处理
			}

			message = left;
			msgBuffer = message.getBytes();
			try {
				outStream.write(msgBuffer);
			} catch (IOException e) {
				//异常处理
			}
			break;
		case 4:
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				//异常处理
			}

			message = right;
			msgBuffer = message.getBytes();
			try {
				outStream.write(msgBuffer);
			} catch (IOException e) {
				//异常处理
			}
			break;
		}
	}
	
	//实时绘制动态方向盘
	public void orientation_Circle(){

		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.circle);  
		// 获取图像资源的宽，高 
		int w = bmp.getWidth();  
		int h = bmp.getHeight();
		
		// 设置实时旋转角度 	
		if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			float tmp2 ;
			tmp2 = x;
			x = y;
			y = tmp2;
			x = -x;
		}
		float degrees = (float) ((float) (Math.atan(y/x)*180)/Math.PI);
		Matrix mtx = new Matrix(); 
		mtx.postRotate(degrees);
		
		// 旋转图像 
		Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);  
		BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);  
		imageView.setImageDrawable(bmd);  
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO 自动生成的方法存根
		super.onConfigurationChanged(newConfig);
	}
	
	//设置读取数据的xml
	public String str_get(){
		String str = sp2.getString("btAddress", "");
		return str;
	}
}