package com.app.ant.app.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.app.ant.R;

import java.util.Set;


public class BluetoothSelectDeviceForm extends Activity 
{
	public static final String PARAM_NAME_BLUETOOTH_DEVICE_TYPE="BluetoothDeviceType";
	
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;    
    private int deviceTypeResourceID = R.string.preferences_cash_register_addr_key;

    /** ������������� �����. ��������� ������ ������ ���������*/
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{    		
	        super.onCreate(savedInstanceState);
	        
        	//check form params
	        Bundle params = getIntent().getExtras();	        
			if(params!=null && params.containsKey(PARAM_NAME_BLUETOOTH_DEVICE_TYPE))
	        {
				deviceTypeResourceID = params.getInt(PARAM_NAME_BLUETOOTH_DEVICE_TYPE); 
	        } 
	
	        // Setup the window
	        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	        setContentView(R.layout.bluetooth_device_list);
	
	        // Set result CANCELED incase the user backs out
	        setResult(Activity.RESULT_CANCELED);
	
	        // Initialize the button to perform device discovery
	        Button scanButton = (Button) findViewById(R.id.button_scan);
	        scanButton.setOnClickListener(new OnClickListener() 
	        {
	            public void onClick(View v) 
	            {
	            	Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	            	discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	            	startActivity(discoverableIntent);
	            	
	                doDiscovery();
	                v.setVisibility(View.GONE);
	            }
	        });
	
	        // Initialize array adapters. One for already paired devices and
	        // one for newly discovered devices
	        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_device_name);
	        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_device_name);
	
	        // Find and set up the ListView for paired devices
	        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
	        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
	        pairedListView.setOnItemClickListener(mDeviceClickListener);
	
	        // Find and set up the ListView for newly discovered devices
	        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
	        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
	        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
	
	        // Register for broadcasts when a device is discovered
	        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	        this.registerReceiver(mReceiver, filter);
	
	        // Register for broadcasts when discovery has finished
	        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	        this.registerReceiver(mReceiver, filter);
	
	        // Get the local Bluetooth adapter
	        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	
	        // Get a set of currently paired devices
	        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
	
	        // If there are paired devices, add each one to the ArrayAdapter
	        if (pairedDevices.size() > 0) 
	        {
	            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
	            for (BluetoothDevice device : pairedDevices) 
	            {
	                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	            }
	        } 
	        else 
	        {
	            String noDevices = getResources().getText(R.string.bluetooth_none_paired).toString();
	            mPairedDevicesArrayAdapter.add(noDevices);
	        }
    	}
    	catch(Exception ex)
    	{
    	}
    }

    /** ������� �������� */
    @Override  protected void onDestroy() 
    {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) 
        {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /** ��������� ����������� ��������� ����� BluetoothAdapter*/ 
    private void doDiscovery() 
    {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.bluetooth_scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) 
        {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    /** ���������� ������� �� ���������� � ������. ��������� ����� ���������� ���������� � ���������� ����������.*/
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() 
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) 
        {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BluetoothSelectDeviceForm.this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(deviceTypeResourceID), address);
            editor.commit();
            
            finish();
        }
    };

    /** ������������ ������������ ���������� (��������� � ������), ������ ��������� ���� ����� ����� ��������� ��������*/
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) 
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) 
                {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } 
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
            {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.bluetooth_select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) 
                {
                    String noDevices = getResources().getText(R.string.bluetooth_none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
