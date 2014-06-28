package com.ThinBTClientDemo.www;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyBTSearch extends ListActivity{

	private static final String TAG = "MyBTSEARCH";
	private Editor editor;
	BluetoothConnectActivityReceiver pairReceiver;
	SharedPreferences spf;
	private String btAddress;
	
	//声明界面控件变量
	private TextView bluetoothStatus;
	private Button buttonEnableBluetooth;
	private Button buttonDisableBluetooth;
	private Button buttonSearchBluetooth;
	private Button buttonCancelSearch;

	//声明全局蓝牙变量
	private static BluetoothDevice btDevice;
	private static String btPsw = "1234";	//蓝牙配对口令

	//声明蓝牙适配器变量
	private BluetoothAdapter btAdapter;

	//定义用于存储搜索到的蓝牙设备
	private List<BluetoothDevice> queriedBluetoothDevices = new ArrayList<BluetoothDevice>();

	//定义消息处理变量
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		
		//配置xml储存文件，可被其他应用程序使用
		spf = getSharedPreferences("btaddr", Context.MODE_WORLD_READABLE );

		//界面增加进度显示框
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.search);

		//初始化界面控件
		bluetoothStatus = (TextView)findViewById(R.id.bluetooth_current_status);
		buttonEnableBluetooth = (Button)findViewById(R.id.button_enable);
		buttonDisableBluetooth = (Button)findViewById(R.id.button_disable);
		buttonSearchBluetooth = (Button)findViewById(R.id.button_search);
		buttonCancelSearch = (Button)findViewById(R.id.button_cancel);

		//获取蓝牙适配器
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		//初始化按钮状态
		initControlStatus();

		//注册完成搜索蓝牙设备广播接收器
		IntentFilter searchFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(searchDeviceFinishReceiver,searchFilter);

		//注册发现蓝牙设备广播接收器
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(foundBluetoothDeviceReceiver,foundFilter);

		//注册取消蓝牙配对用户输入框广播接收器
		String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
		pairReceiver = new BluetoothConnectActivityReceiver(); 
		IntentFilter pairFilter = new IntentFilter();
		pairFilter.addAction(ACTION_PAIRING_REQUEST); 
		registerReceiver(pairReceiver,pairFilter);
		
		//注册打开蓝牙按钮单击事件监听器
		buttonEnableBluetooth.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//打开蓝牙操作
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent,1);
			}
		});

		//注册关闭蓝牙按钮单击按钮事件监听器
		buttonDisableBluetooth.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//关闭蓝牙
				closeBluetooth();
			}
		});

		//注册搜索蓝牙按钮单击事件监听器
		buttonSearchBluetooth.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//开始执行搜素
				new Thread(searchDevicesJob).start();
				//显示进度框
				setProgressBarIndeterminateVisibility(true);
			}
		});

		//注册取消搜索蓝牙按钮单击事件监听器
		buttonCancelSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//执行取消操作
				btAdapter.cancelDiscovery();

				//取消进度框显示
				setProgressBarIndeterminateVisibility(false);
			}
		});		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自动生成的方法存根
		initControlStatus();
	}

	private void initControlStatus(){
		//判断当前蓝牙是否开启
		if(btAdapter.isEnabled()){
			bluetoothStatus.setText(getString(R.string.bluetooth_enabled));

			//设置按钮的状态，当蓝牙开启时，打开按钮不可用
			buttonEnableBluetooth.setEnabled(false);
			buttonDisableBluetooth.setEnabled(true);
			buttonSearchBluetooth.setEnabled(true);
			buttonCancelSearch.setEnabled(true);
		}else{
			bluetoothStatus.setText(getString(R.string.bluetooth_disabled));

			//设置按钮的状态，当蓝牙关闭时，关闭、搜索及取消按钮不可用
			buttonEnableBluetooth.setEnabled(true);
			buttonDisableBluetooth.setEnabled(false);
			buttonSearchBluetooth.setEnabled(false);
			buttonCancelSearch.setEnabled(false);
		}
	}

	/**
	 * 关闭蓝牙
	 */
	private void closeBluetooth(){
		//取消进度框显示
		setProgressBarIndeterminateVisibility(false);

		//调用适配器方法进行关闭操作
		btAdapter.disable();

		//控制按钮及ListView显示
		initControlStatus();
		setListAdapter(null);

		//解除已经注册的广播监听器
		unregisterReceiver(foundBluetoothDeviceReceiver);
		unregisterReceiver(searchDeviceFinishReceiver);
		unregisterReceiver(pairReceiver);
	}

	/**
	 * 定义执行搜索蓝牙操作的任务
	 */
	private Runnable searchDevicesJob = new Runnable(){
		@Override
		public void run(){
			//执行搜索
			btAdapter.startDiscovery();
		}
	};

	/**
	 * 定义用于发现蓝牙设备后进行消息接收的接收器
	 */
	private BroadcastReceiver foundBluetoothDeviceReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO 自动生成的方法存根
			//获取设备详细信息
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			//添加到列表中
			queriedBluetoothDevices.add(device);

			//显示搜索到的设备信息
			displayDiscoveryDevices();
		}

	};

	/**
	 * 定义用于完成蓝牙设备搜索的接收器
	 */
	private BroadcastReceiver searchDeviceFinishReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO 自动生成的方法存根
			//删除已经注册的广播接收器
			unregisterReceiver(foundBluetoothDeviceReceiver);
			//unregisterReceiver(pairReceiver);
			unregisterReceiver(this);

			//取消显示进度框
			setProgressBarIndeterminateVisibility(false);
		}
	};

	/**
	 * 显示搜索到的设备信息
	 */
	private void displayDiscoveryDevices(){
		List<String> itemList = new ArrayList<String>();

		//构建用于显示的数据
		for(BluetoothDevice device : queriedBluetoothDevices){
			int connectState = device.getBondState();
			StringBuffer sb = new StringBuffer();
			//获取设备的名称及地址
			sb.append(getString(R.string.item_name_prefix));
			sb.append(isEmptyString(device.getName()) ? getString(R.string.no_device_name) : device.getName());
			sb.append("\n");
			sb.append(getString(R.string.item_address_prefix));
			sb.append(device.getAddress());
			sb.append("\n");
			sb.append(getString(R.string.item_pair_state));
			switch (connectState) {  
			// 未配对   
			case BluetoothDevice.BOND_NONE:  
				sb.append(getString(R.string.bt_bond_none));
				break;  
				// 已配对   
			case BluetoothDevice.BOND_BONDED:
				sb.append(getString(R.string.bt_bond_bonded));
				break;  
			}  

			//添加到显示列表
			itemList.add(sb.toString());
		}

		//构造数据适配器
		final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
				MyBTSearch.this,android.R.layout.simple_list_item_1,itemList);

		//通过UI线程显示发现设备列表数据
		handler.post(new Runnable(){

			@Override
			public void run() {
				// TODO 自动生成的方法存根
				setListAdapter(listAdapter);
			}
		});
	}

	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	private boolean isEmptyString(String str){
		if(str != null){
			if("".equals(str)){
				return true;
			}
			return false;
		}else{
			return true;
		}
	}

	//ListView设置监听器
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO 自动生成的方法存根
		super.onListItemClick(l, v, position, id);
		if (btAdapter.isDiscovering())  
			btAdapter.cancelDiscovery();
		String str = (String) l.getItemAtPosition(position);
		String[] values = str.split("："); //中文冒号，mac地址是英文冒号 
		String address = values[2].substring(0, 17);
		Log.e(TAG,address);
		btAddress = address;
		if(btAdapter != null)
			btDevice = btAdapter.getRemoteDevice(btAddress);

		bt_connect_dialog();

	}

	
	
	//点击列表项弹出对话框
	public void bt_connect_dialog(){
		new AlertDialog.Builder(MyBTSearch.this)
		.setTitle(R.string.confirm)
		.setMessage(R.string.confirm_q_bt)
		.setPositiveButton(R.string.bt_connection, listener)
		.setNegativeButton(R.string.no, listener)
		.show();
	}

	//定义蓝牙自动配对
	public static boolean bt_autoPair(String strAddr,String strPsw)
	{
		boolean result = false;
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		bluetoothAdapter.cancelDiscovery();
		if (!bluetoothAdapter.isEnabled())
		{
			bluetoothAdapter.enable();
		}
		if (!BluetoothAdapter.checkBluetoothAddress(strAddr))
		{ // 检查蓝牙地址是否有效
			Log.d("mylog", "devAdd un effient!");
		}
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);
		if (device.getBondState() != BluetoothDevice.BOND_BONDED)
		{
			try
			{
				Log.d("mylog", "NOT BOND_BONDED");
				boolean pinFlag = ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
				boolean bondFlag = false;
				if(pinFlag)
					bondFlag = ClsUtils.createBond(device.getClass(), device);
				btDevice = device; // 配对完毕就把这个设备对象传给全局的btDevice
				if(bondFlag)
					result = true;
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				Log.d("mylog", "setPiN failed!");
				e.printStackTrace();
			} //
		}
		else
		{
			Log.d("mylog", "HAS BOND_BONDED");
			try
			{
				boolean bondFlag1 = ClsUtils.createBond(device.getClass(), device);
				boolean pinFlag = ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
				boolean bondFlag2 = ClsUtils.createBond(device.getClass(), device);
				btDevice = device; // 如果绑定成功，就直接把这个设备对象传给全局的btDevice
				if(pinFlag && bondFlag1 && bondFlag2)
					result = true;
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				Log.d("mylog", "setPiN failed!");
				e.printStackTrace();
			}
			
		}
		return result;
	}

	//定义蓝牙手动配对
	public boolean bt_handPair(){
		try {  
			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");  
			createBondMethod.invoke(btDevice);
			return true;
		} catch (Exception e) {   
			return false;
		}  
	}

	/**监听对话框里面的点击事件*/  
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
	{  
		public void onClick(DialogInterface dialog, int which)  
		{  
			switch (which)  
			{  
			case AlertDialog.BUTTON_POSITIVE:// 连接
				boolean isPaired = false;
				//配对
				try {  
					//boolean pair = bt_handPair();//手动配对
					boolean pair = bt_autoPair(btAddress,btPsw);  // 自动配对   
					if(pair){
						str_put(btAddress);
						display_Toast(R.string.bt_bond_bonded);
						isPaired = true;
						finish();
					}else{
						display_Toast(R.string.bt_bond_none);
					}
				} catch (Exception e) {   
					display_Toast(R.string.bt_bond_none);
				} 

				//连接
				/*if(isPaired){
					try {
						if((btDevice != null)){
							connect(btDevice);
							display_Toast(R.string.connect_succeed);
						}
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						Log.e(TAG,"连接失败了");
						//e.printStackTrace();
					}
				}*/
				//MyApplication.getInstance().exit(); 
				break;  
			case AlertDialog.BUTTON_NEGATIVE:// 取消  
				break;  
			default:  
				break;  
			}  
		} 	    
	};

	//蓝牙连接
	private void connect(BluetoothDevice device) throws IOException {  
		// 固定的UUID   
		final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";  
		UUID uuid = UUID.fromString(MY_UUID);  
		BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);  
		socket.connect();  
	}  

	public class BluetoothConnectActivityReceiver extends BroadcastReceiver
	{

		String strPsw = btPsw;

		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					"android.bluetooth.device.action.PAIRING_REQUEST"))
			{
				BluetoothDevice btDevice = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// byte[] pinBytes = BluetoothDevice.convertPinToBytes("1234");
				// device.setPin(pinBytes);
				Log.i("tag11111", "ddd");
				try
				{
					ClsUtils.setPin(btDevice.getClass(), btDevice, strPsw); // 手机和蓝牙采集器配对
					ClsUtils.createBond(btDevice.getClass(), btDevice);
					ClsUtils.cancelPairingUserInput(btDevice.getClass(), btDevice);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
	}

	public void display_Toast(int id) {
		Toast toast = Toast.makeText(this, getResources().getString(id), Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 20);
		toast.show();
	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		unregisterReceiver(pairReceiver);
	}

	//设置存储数据的xml
	public void str_put(String str){
		editor = spf.edit();
		editor.putString("btAddress", str);
		editor.commit();
	}

}