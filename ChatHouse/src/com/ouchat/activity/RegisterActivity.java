package com.ouchat.activity;
/*package com.way.activity;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.ouchat.R;
import com.way.service.IConnectionStatusCallback;
import com.way.service.XXService;
import com.way.util.Logout;

@SuppressWarnings("all")
public class RegisterActivity extends Activity implements OnClickListener, 
IConnectionStatusCallback{
	public static final String LOGIN_ACTION = "com.way.action.REGISTER";
	private Button mBtnRegister;
	private Button mRegBack;
	private EditText mEmailEt, mNameEt, mPasswdEt, mPasswdEt2,nameMCH;
	
	private XXService mXxService;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(RegisterActivity.this);
			// 开始连接xmpp服务器
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.register);
		startService(new Intent(this, XXService.class));
		bindXMPPService();
		mBtnRegister = (Button) findViewById(R.id.register_btn);
		mRegBack = (Button) findViewById(R.id.reg_back_btn);
		mBtnRegister.setOnClickListener(this);
		mRegBack.setOnClickListener(this);

		nameMCH = (EditText) findViewById(R.id.reg_nameMCH);
		mEmailEt = (EditText) findViewById(R.id.reg_email);
		mNameEt = (EditText) findViewById(R.id.reg_name);
		mPasswdEt = (EditText) findViewById(R.id.reg_password);
		mPasswdEt2 = (EditText) findViewById(R.id.reg_password2);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.reg_back_btn:
			login();
			break;
		case R.id.register_btn:
//			registered();
			 testRegister();
			break;
		default:
			break;
		}
		
	}

public void testRegister(){
	new Thread(new Runnable() {
		
		@Override
		public void run() {
			
			  ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222);  
			    config.setSecurityMode(SecurityMode.required);  
			    config.setSASLAuthenticationEnabled(false);  
			    config.setCompressionEnabled(false);  
			      
			    XMPPConnection connection = new XMPPConnection(config);  
			    try {  
			        connection.connect();  
			        Log.i("ouou", "XMPP connected successfully");  
			          
			    } catch (XMPPException e) {  
			        Log.d("ouou", "the connection is error ... ");  
			    }  
			 String newUsername = "orqtestw";
			 String newPassword = "qqqqqq";
			Registration registration = new Registration();
			PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
					registration.getPacketID()), new PacketTypeFilter(
					IQ.class));
			PacketListener packetListener = new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					Log.d("ouou", "packet:"+packet);
					// 服务器回复客户端
		            if(packet instanceof IQ) {
		            	IQ response = (IQ) packet;
		            	if(response.getType() == IQ.Type.ERROR) { // 注册失败
		            		if (!response.getError().toString().contains(
		                            "409")) {
		                        Log.e("ouou"," 注册失败--Unknown error while registering XMPP account! " + response.getError().getCondition());
		                    }
		            	} else if(response.getType() == IQ.Type.RESULT) { // 注册成功
		            		  Log.e("ouou"," 注册成功 ");
		            	}
		            }
				}
			};
			// 给注册的Packet设置Listener，因为只有等到正真注册成功后，我们才可以交流
			connection.addPacketListener(packetListener, packetFilter);
			  Log.e("ouou"," 注册 ");
			registration.setType(IQ.Type.SET);
			registration.addAttribute("username", newUsername);
		    registration.addAttribute("password", newPassword);
		    registration.addAttribute("android", "geolo_createUser_android");// 这边addAttribute不能为空，否则出错。所以做个标志是android手机创建的吧！！！！！
			// 向服务器端，发送注册Packet包，注意其中Registration是Packet的子类
		    connection.sendPacket(registration);
			
		}
	}).start();
		
		
}
//	private void registered() {
//
//		String accounts = mNameEt.getText().toString();
//		String password = mPasswdEt.getText().toString();
//		String email = mEmailEt.getText().toString();
//		String mingcheng = nameMCH.getText().toString();
//		
//		Registration reg = new Registration();
//		reg.setType(IQ.Type.SET);
//		reg.setTo(mXxService.getTestConnection().getServiceName());
//		reg.setUsername(accounts);
//		reg.setPassword(password);
//		reg.addAttribute("name", mingcheng);
//		reg.addAttribute("email", email);
//		
//		reg.addAttribute("android", "geolo_createUser_android");
//		PacketFilter filter = new AndFilter(new PacketIDFilter(
//		                                reg.getPacketID()), new PacketTypeFilter(
//		                                IQ.class));
//		PacketCollector collector = mXxService.getTestConnection().
//		createPacketCollector(filter);
//		mXxService.getTestConnection().sendPacket(reg);
//		IQ result = (IQ) collector.nextResult(SmackConfiguration
//		                                .getPacketReplyTimeout());
//		                        // Stop queuing results
//		collector.cancel();// 停止请求results（是否成功的结果）
//		if (result == null) {
//		Toast.makeText(getApplicationContext(), "服务器没有返回结果", Toast.LENGTH_SHORT).show();
//		} else if (result.getType() == IQ.Type.ERROR) {
//		if (result.getError().toString()
//		                        .equalsIgnoreCase("conflict(409)")) {
//		    Toast.makeText(getApplicationContext(), "这个账号已经存在", Toast.LENGTH_SHORT).show();
//		    } else {
//		        Toast.makeText(getApplicationContext(), "注册失败",
//		                                        Toast.LENGTH_SHORT).show();
//		    }
//		} else if (result.getType() == IQ.Type.RESULT) {
//			try {
//				mXxService.getTestConnection().login(accounts, password);
//				Presence presence = new Presence(Presence.Type.available);
//				mXxService.getTestConnection().sendPacket(presence);
//				Log.d("ouou", "注册---亲，恭喜你，注册成功了！");
//				Intent intent = new Intent();
//				intent.putExtra("USERID", accounts);
//				intent.setClass(RegisterActivity.this, MainActivity.class);
//				startActivity(intent);
//			} catch (XMPPException e) {
//				e.printStackTrace();
//			}	
//		}
//		
//	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindXMPPService();
	}
	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			Log.d("ouou","RegisterActivity.class, ----[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
		}
	}

	private void bindXMPPService() {
		Log.d("ouou","RegisterActivity.class----[SERVICE] Unbind");
		Intent mServiceIntent = new Intent(this, XXService.class);
		mServiceIntent.setAction(LOGIN_ACTION);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}
	private void login() {
		Intent intent = new Intent();
		intent.setClass(RegisterActivity.this, LoginActivity.class);
		startActivity(intent);
	}

	 (non-Javadoc)
	 * @see com.way.service.IConnectionStatusCallback#connectionStatusChanged(int, java.lang.String)
	 
	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		// TODO Auto-generated method stub
		
	}
}*/