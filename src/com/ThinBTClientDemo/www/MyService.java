package com.ThinBTClientDemo.www;

import java.io.IOException;  
import java.io.InputStream;  
import java.io.OutputStream;  
import java.util.List;
import java.util.UUID;  

import android.app.Service;  
import android.bluetooth.BluetoothAdapter;  
import android.bluetooth.BluetoothDevice;  
import android.bluetooth.BluetoothSocket;  
import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.content.IntentFilter;  
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;  
import android.util.Log;  


public class MyService extends Service{  

	public boolean btThreadFlag = true; 
	//public boolean accelThreadFlag = true; 
	private MyThread myThread;
	//private AccelThread accelThread = null;
	//private SensorManager sensorManager;
	//private SensorEventListener sensorListener;
	//private Intent ccelIntent;
	SharedPreferences spf;
	public Context othercontext;
	private CommandReceiver cmdReceiver;//继承自BroadcastReceiver对象，用于得到Activity发送过来的命令  

	/**************service 命令*********/   
	static final int CMD_STOP_SERVICE = 0x01;  
	static final int CMD_SEND_DATA = 0x02;  
	static final int CMD_SYSTEM_EXIT =0x03;  
	static final int CMD_SHOW_TOAST =0x04; 

	private BluetoothAdapter mBluetoothAdapter = null;  
	private BluetoothSocket btSocket = null;  
	private OutputStream outStream = null;  
	private InputStream  inStream = null;  
	public  boolean bluetoothFlag  = true; 
	//public boolean accel_flag = false;
	//private float x, y, z;
	//private float[] floatarray;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  
	private static String address;// 定义要连接的蓝牙设备MAC地址  

	@Override  
	public IBinder onBind(Intent intent) {  
		// TODO Auto-generated method stub  
		return null;  
	}  

	@Override  
	public void onCreate() {  
		// TODO Auto-generated method stub  
		super.onCreate(); 
		
		//获取xml中的蓝牙地址
		try {  
			othercontext = createPackageContext("com.ThinBTClientDemo.www",  
					Context.CONTEXT_IGNORE_SECURITY);  
			spf = othercontext.getSharedPreferences("btaddr",othercontext.MODE_PRIVATE);  
		} catch (NameNotFoundException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}
		address = str_get();
	}  

	//前台Activity调用startService时，该方法自动执行  
	@Override  
	public int onStartCommand(Intent intent, int flags, int startId) {  
		// TODO Auto-generated method stub  
		cmdReceiver = new CommandReceiver();  
		IntentFilter filter = new IntentFilter();//创建IntentFilter对象  
		//注册一个广播，用于接收Activity传送过来的命令，控制Service的行为，如：发送数据，停止服务等  
		filter.addAction("android.intent.action.cmd");  
		//注册Broadcast Receiver  
		registerReceiver(cmdReceiver, filter);  
		doJob();//调用方法启动蓝牙线程
/*		ccelIntent = new Intent(this,Accelerometer.class);
		if(ccelIntent.getStringExtra("accel") == "accel_start"){
			accel_flag = true;
			doAccelJob();//调用方法启动重力传感器线程
		}*/
		return super.onStartCommand(intent, flags, startId);  
	}  

	@Override  
	public void onDestroy() {  
		// TODO Auto-generated method stub  
		super.onDestroy();  
		if(cmdReceiver != null){
			this.unregisterReceiver(cmdReceiver);//取消注册的CommandReceiver 
		}
		/*if(sensorListener != null){
			sensorManager.unregisterListener(sensorListener);
		}*/
		
		btThreadFlag = false; 
		//accelThreadFlag = false;
		boolean retry = true;  
		while(retry){  
			try{  
				myThread.join(); 
				/*if(accel_flag){
					accelThread.join();
				}*/
				retry = false;  
			}catch(Exception e){  
				e.printStackTrace();  
			}  
		}
		if(outStream != null){
			try {
				outStream.flush();
			} catch (IOException e) {
			}
		}

		if(btSocket != null){
			try {
				btSocket.close();
			} catch (IOException e2) {
			}
		}
	}  

	public class MyThread extends Thread{    //蓝牙线程      
		@Override  
		public void run() {  
			// TODO Auto-generated method stub  
			super.run();  
			while(btThreadFlag){
				try{  
					connectDevice();//连接蓝牙设备  
					btThreadFlag = false; 
				}catch(Exception e){  
					e.printStackTrace();  
				}              
			}  
		}      
	}  
	
	/*public class AccelThread extends Thread{	//重力传感器线程
		@Override  
		public void run() {  
			// TODO Auto-generated method stub  
			super.run();  
			while(accelThreadFlag && accel_flag){  
				try{  
					sendData(floatarray);//发送数组给Activity
					//accelThreadFlag = false;  
				}catch(Exception e){  
					e.printStackTrace();  
				}              
			}  
		}
	}*/

	public void doJob(){      
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();  
		if (mBluetoothAdapter == null) {   
			//DisplayToast("蓝牙设备不可用，请打开蓝牙！");
			showToast(getString(R.string.connect_e01));
			bluetoothFlag  = false;  
			return;  
		}  

		if (!mBluetoothAdapter.isEnabled()) {  
			//DisplayToast("请打开蓝牙并重新运行程序！");  
			bluetoothFlag  = false;  
			stopService();  
			showToast(getString(R.string.connect_e02));  
			return;  
		}        
		showToast(getString(R.string.try_connect));  
		btThreadFlag = true;    
		myThread = new MyThread();  
		myThread.start(); 
	}  
	
	/*public void doAccelJob(){
		//启动传感器服务
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		//获取重力感应传感器
		List<Sensor> sensors = sensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER);

		//设置传感器监听器
		sensorListener = new SensorEventListener() { 
			public void onSensorChanged(SensorEvent event) {
				x = event.values[SensorManager.DATA_X];     
				y = event.values[SensorManager.DATA_Y];     
				z = event.values[SensorManager.DATA_Z];
				floatarray = new float[]{x,y,z};
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
		
		//启动发送传感器数据线程
		accelThread = new AccelThread();
		accelThread.start();
	}
*/	
	public  void connectDevice(){   
		//DisplayToast("正在尝试连接蓝牙设备，请稍后····");  
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);  
		try {  
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);  
		} catch (IOException e) {  
			//DisplayToast("套接字创建失败！");  
			bluetoothFlag = false;  
		}  
		//DisplayToast("成功连接蓝牙设备！");  
		mBluetoothAdapter.cancelDiscovery();  
		try {  
			btSocket.connect();  
			DisplayToast(getString(R.string.connect_succeed));  
			showToast(getString(R.string.connect_succeed));  
			bluetoothFlag = true;  
		} catch (IOException e) {  
			try { 
				btSocket.close();  
				bluetoothFlag = false;  
			} catch (IOException e2) {                          
				DisplayToast(getString(R.string.connect_e03));  
			}  
		}     

		if(bluetoothFlag){  
			try {  
				inStream = btSocket.getInputStream();  
			} catch (IOException e) {  
				e.printStackTrace();  
			} //绑定读接口  

			try {  
				outStream = btSocket.getOutputStream();  
			} catch (IOException e) {  
				e.printStackTrace();  
			} //绑定写接口  
		}  
	}    

	public void sendCmd(String msg)//串口发送数据  
	{     
		if(!bluetoothFlag){  
			return;  
		}
		byte[] msgBuffer = msg.getBytes();
		
		try {  
			outStream.write(msgBuffer);  
			outStream.flush();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}   
	}

	public void stopService(){//停止服务      
		btThreadFlag = false;//停止蓝牙线程  
		//accelThreadFlag = false; //停止重力传感器线程
		stopSelf();//停止服务  
	}  

	public void showToast(String str){//显示提示信息  
		Intent intent = new Intent();  
		intent.putExtra("cmd", CMD_SHOW_TOAST);  
		intent.putExtra("str", str);  
		intent.setAction("android.intent.action.lxx");  
		sendBroadcast(intent);    
	}  
	
/*	public void sendData(float[] flt){//发送数据给Activity
		Intent intent = new Intent();  
		intent.putExtra("cmd", CMD_SEND_DATA);  
		intent.putExtra("flt", flt);  
		intent.setAction("android.intent.action.flt");  
		sendBroadcast(intent);
	}*/

	public void DisplayToast(String str)  
	{  
		Log.d("ThinBT--service",str);
	}  

	//接收Activity传送过来的命令  
	private class CommandReceiver extends BroadcastReceiver{  
		@Override  
		public void onReceive(Context context, Intent intent) {  
			if(intent.getAction().equals("android.intent.action.cmd")){  
				int cmd = intent.getIntExtra("cmd", -1);//获取Extra信息                              
				if(cmd == CMD_STOP_SERVICE){  
					stopService();  
				}    

				if(cmd == CMD_SEND_DATA)  
				{  
					String message = intent.getStringExtra("message");  
					sendCmd(message); 
				}  
			}     
		}                          
	} 
			
	//设置读取数据的xml
	public String str_get(){
		String str = spf.getString("btAddress", "");
		return str;
	}

}  