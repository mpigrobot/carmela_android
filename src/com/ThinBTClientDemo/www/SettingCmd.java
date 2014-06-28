package com.ThinBTClientDemo.www;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class SettingCmd extends Activity{

	private EditText set_editorF;
	private EditText set_editorB;
	private EditText set_editorL;
	private EditText set_editorR;
	private EditText set_editorS;
	private CheckBox set_check;
	private Button set_yes;
	private Button set_no;
	private Editor editor;
	private SharedPreferences spf;

	//定义公共字符串
	public static String forword;
	public static String back;
	public static String left;
	public static String right;
	public static String stop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingcmd);

		//绑定控件
		set_editorF = (EditText)findViewById(R.id.set_edtF);
		set_editorB = (EditText)findViewById(R.id.set_edtB);
		set_editorL = (EditText)findViewById(R.id.set_edtL);
		set_editorR = (EditText)findViewById(R.id.set_edtR);
		set_editorS = (EditText)findViewById(R.id.set_edtS);
		set_check = (CheckBox)findViewById(R.id.set_check);
		set_yes = (Button)findViewById(R.id.set_yes);
		set_no = (Button)findViewById(R.id.set_no);

		//设置控件监听
		set_yes.setOnClickListener(eventListener);
		set_no.setOnClickListener(eventListener);

		//配置其他应用程序可以使用的xml储存文件
		spf = getSharedPreferences("btcmd", Context.MODE_WORLD_READABLE );
		str_get();//从btcmd.xml中读取并显示保存的命令

	}

	private View.OnClickListener eventListener = new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			int id = v.getId();
			switch(id){
			case R.id.set_yes:
				if(set_check.isChecked()){
					str_put(); //保存的命令到btcmd.xml中
					finish();
				}
				break;
			case R.id.set_no:
				finish();
				break;
			}
		}
	};
	public void str_put(){
		editor = spf.edit();
		editor.putString("forword_key", set_editorF.getText().toString());
		editor.putString("back_key", set_editorB.getText().toString());
		editor.putString("left_key", set_editorL.getText().toString());
		editor.putString("right_key", set_editorR.getText().toString());
		editor.putString("stop_key", set_editorS.getText().toString());
		editor.commit();
	}
	public void str_get(){
		forword = spf.getString("forword_key", "");
		back = spf.getString("back_key", "");
		left = spf.getString("left_key", "");
		right = spf.getString("right_key", "");
		stop = spf.getString("stop_key", "");

		set_editorF.setText(forword);
		set_editorB.setText(back);
		set_editorL.setText(left);
		set_editorR.setText(right);
		set_editorS.setText(stop);
	}


}