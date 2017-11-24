package com.demo.hdz.getbluetoothmac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView m_tvLocalBluetoothMAC;
    private TextView m_tvLocalBluetoothName;
    private TextView m_tvRemoteBluetoothMAC;
    private TextView m_tvRemoteBluetoothName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvAppVer = (TextView) findViewById(R.id.tvAppVer);
        tvAppVer.setText(BuildConfig.APP_VERSION);

        TextView tvDevMode = (TextView) findViewById(R.id.tvDevMode);
        tvDevMode.setText(android.os.Build.MODEL);

        TextView tvAndroidVer = (TextView) findViewById(R.id.tvAndroidVer);
        tvAndroidVer.setText(android.os.Build.VERSION.RELEASE);

        TextView tvVer = (TextView) findViewById(R.id.tvAPIVer);
        tvVer.setText(""+Build.VERSION.SDK_INT);
        //tvVer.setText(android.os.Build.VERSION.SDK);

        m_tvLocalBluetoothName = (TextView) findViewById(R.id.tvBluetoothName);
        m_tvLocalBluetoothMAC = (TextView) findViewById(R.id.tvBluetoothMac);
        m_tvRemoteBluetoothName = (TextView) findViewById(R.id.tvRemoteBluetoothName);
        m_tvRemoteBluetoothMAC = (TextView) findViewById(R.id.tvRemoteBluetoothMac);
    }


    //https://langw.gitbooks.io/blesummary-/content/24_android_bu_tong_lan_ya_ban_ben_huo_qu_lan_ya_di.html
    public String getLocalBluetoothMAC() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return "";
        }

        m_tvLocalBluetoothName.setText(btAdapter.getName());

        String bluetoothMAC = "";
        if (Build.VERSION.SDK_INT < 23) {
            bluetoothMAC = btAdapter.getAddress();
        } else {
            Class<? extends BluetoothAdapter> btAdapterClass = btAdapter.getClass();
            try {
                Class<?> btClass = Class.forName("android.bluetooth.IBluetooth");
                Field bluetooth = btAdapterClass.getDeclaredField("mService");
                bluetooth.setAccessible(true);
                Method btAddress = btClass.getMethod("getAddress");
                btAddress.setAccessible(true);
                bluetoothMAC = (String) btAddress.invoke(bluetooth.get(btAdapter));
            } catch (Exception e) {
                bluetoothMAC = btAdapter.getAddress();
            }
        }

        if (bluetoothMAC.equals("02:00:00:00:00:00")) {
            return "";
        } else {
            return bluetoothMAC;
        }
    }

//http://blog.csdn.net/lang523493505/article/details/53149726
//http://blog.csdn.net/jasonwang18/article/details/61214431
//http://www.cnblogs.com/Free-Thinker/p/6763635.html
    //只能连接蓝牙耳机等多媒体设备
    private void getConnectedBluetoothMAC(final Context context) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return;
        }
        int a2dp = btAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = btAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = btAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        if (flag != -1) {
            btAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {

                }
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        m_tvRemoteBluetoothMAC.setText( mDevices.get(0).getAddress() );
                        m_tvRemoteBluetoothName.setText( mDevices.get(0).getName() );
                    }
                }
            }, flag);
        } else {
            m_tvRemoteBluetoothMAC.setText( "蓝牙未连接" );
            m_tvRemoteBluetoothName.setText( "蓝牙未连接" );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //String macAddress = android.provider.Settings.Secure.getString(getContentResolver(),"bluetooth_address");
        String localBluetoothMAC = getLocalBluetoothMAC();
        if (localBluetoothMAC.equals("")) {
            m_tvLocalBluetoothMAC.setText("蓝牙未打开");
        } else {
            m_tvLocalBluetoothMAC.setText(localBluetoothMAC);
        }

        String remoteBluetoothMAC = getRemoteBluetoothMAC();
        if (!remoteBluetoothMAC.equals("")) {
            m_tvRemoteBluetoothMAC.setText(remoteBluetoothMAC);
        } else {
            getConnectedBluetoothMAC(this);
        }
    }


    //只能获取手机连接的蓝牙MAC
    private String getRemoteBluetoothMAC() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        try {
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);
            if(state == BluetoothAdapter.STATE_CONNECTED){
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                for(BluetoothDevice device : devices){
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if(isConnected){
                        m_tvRemoteBluetoothName.setText(device.getName());
                        return device.getAddress();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
