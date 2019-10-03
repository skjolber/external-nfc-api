package com.skjolberg.nfc.external;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.skjolberg.nfc.NfcReader;
import com.skjolberg.nfc.NfcService;
import com.skjolberg.nfc.NfcTag;
import com.skjolberg.nfc.acs.AcrReader;
import com.skjolberg.service.BackgroundUsbService;
import com.skjolberg.service.BluetoothBackgroundService;


public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean boot = ActivityAliasTools.isBootFilter(this);
        boolean usb = ActivityAliasTools.isUsbDeviceFilter(this);

        if (boot || usb) {
            Editor edit = prefs.edit();

            if (boot) {
                edit.putBoolean(BackgroundUsbService.PREFERENCE_AUTO_START_ON_RESTART, true);
            }
            if (usb) {
                edit.putBoolean(BackgroundUsbService.PREFERENCE_AUTO_START_ON_READER_CONNECT, true);
            }

            edit.commit();
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        startReceivingTagBroadcasts();
        startReceivingReaderBroadcasts();
        startReceivingServiceBroadcasts();
    }

    private static final String TAG = MainActivity.class.getName();

    private ShareActionProvider mShareActionProvider;

    private boolean recieveTagBroadcasts = false;
    private boolean recieveReaderBroadcasts = false;
    private boolean recieveServiceBroadcasts = false;

    private boolean running = false;
    private boolean bluetoothRunning = false;

    private final BroadcastReceiver usbDevicePermissionReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.d(TAG, "Custom broacast receiver");

            setTagPresent(!NfcTag.ACTION_TAG_LEFT_FIELD.equals(action));
        }
    };

    private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.d(TAG, "Custom broacast receiver: " + action);

            if (NfcReader.ACTION_READER_OPENED.equals(action)) {
                Log.d(TAG, "Reader open");

            	/*
            	if(intent.hasExtra(NfcReader.EXTRA_READER_CONTROL)) {
            		AcrReader reader = intent.getParcelableExtra(NfcReader.EXTRA_READER_CONTROL);
            		
            		Log.d(TAG, "Got reader " + reader + " with name " + reader.getName() + " and firmware " + reader.getFirmware() + " and picc " + reader.getPICC());
            	} else {
            		Log.d(TAG, "No reader");
            	}
            	*/

                setReaderOpen(true);
            } else if (NfcReader.ACTION_READER_CLOSED.equals(action)) {
                Log.d(TAG, "Reader closed");
                setReaderOpen(false);
            } else {
                Log.d(TAG, "Ignore action " + action);
            }


        }

    };

    private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.d(TAG, "Custom broacast receiver: " + action);

            if (NfcService.ACTION_SERVICE_STARTED.equals(action)) {
                running = true;
            } else if (NfcService.ACTION_SERVICE_STOPPED.equals(action)) {
                running = false;
            } else {
                Log.d(TAG, "Ignore action " + action);
            }
            setServiceStarted(running);

        }

    };


    public void startService() {
        Intent intent = new Intent();
        intent.setClassName("com.skjolberg.nfc.external", "com.skjolberg.service.BackgroundUsbService");
        startService(intent);
    }


    public void startBluetoothService() {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        stopReceivingTagBroadcasts();
        stopReceivingReaderBroadcasts();
        stopReceivingServiceBroadcasts();

        super.onDestroy();
    }

    private void stopService() {
        Intent intent = new Intent();
        intent.setClassName("com.skjolberg.nfc.external", "com.skjolberg.service.BackgroundUsbService");
        stopService(intent);
    }

    private void stopBluetoothService() {
        Intent intent = new Intent();
        intent.setClassName("com.skjolberg.nfc.external", "com.skjolberg.service.BluetoothBackgroundService");
        stopService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        broadcast(NfcService.ACTION_SERVICE_STATUS);
        broadcast(NfcReader.ACTION_READER_STATUS);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void startReceivingTagBroadcasts() {
        if (!recieveTagBroadcasts) {
            Log.d(TAG, "Start receiving tag broadcasts");

            recieveTagBroadcasts = true;

            // register receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcTag.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcTag.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcTag.ACTION_TECH_DISCOVERED);
            filter.addAction(NfcTag.ACTION_TAG_LEFT_FIELD);

            registerReceiver(usbDevicePermissionReceiver, filter);
        }
    }

    private void stopReceivingTagBroadcasts() {
        if (recieveTagBroadcasts) {
            Log.d(TAG, "Stop receiving tag broadcasts");

            recieveTagBroadcasts = false;

            unregisterReceiver(usbDevicePermissionReceiver);
        }
    }

    private void startReceivingReaderBroadcasts() {
        if (!recieveReaderBroadcasts) {
            Log.d(TAG, "Start receiving reader broadcasts");

            recieveReaderBroadcasts = true;

            // register receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcReader.ACTION_READER_OPENED);
            filter.addAction(NfcReader.ACTION_READER_CLOSED);

            registerReceiver(readerReceiver, filter);
        }
    }

    private void stopReceivingReaderBroadcasts() {
        if (recieveReaderBroadcasts) {
            Log.d(TAG, "Stop receiving broadcasts");

            recieveReaderBroadcasts = false;

            unregisterReceiver(readerReceiver);
        }
    }

    private void startReceivingServiceBroadcasts() {
        if (!recieveServiceBroadcasts) {
            Log.d(TAG, "Start receiving service broadcasts");

            recieveServiceBroadcasts = true;

            // register receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcService.ACTION_SERVICE_STARTED);
            filter.addAction(NfcService.ACTION_SERVICE_STOPPED);

            registerReceiver(serviceReceiver, filter);
        }
    }

    private void stopReceivingServiceBroadcasts() {
        if (recieveServiceBroadcasts) {
            Log.d(TAG, "Stop receiving broadcasts");

            recieveServiceBroadcasts = false;

            unregisterReceiver(serviceReceiver);
        }
    }

    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }


    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            byte b = buffer[i];
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private MainActivity activity;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            this.activity = (MainActivity) activity;
        }
    }

    public void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // do nothing
    }

    public void startReaderService(View view) {

        if (running) {
            Log.d(TAG, "Stop reader service");

            Intent intent = new Intent(this, BackgroundUsbService.class);
            stopService(intent);
        } else {
            Log.d(TAG, "Start reader service");

            Intent intent = new Intent(this, BackgroundUsbService.class);
            startService(intent);
        }
    }

    public void startBluetoothReaderService(View view) {

        if (bluetoothRunning) {
            Log.d(TAG, "Stop bluetooth reader service");

            Intent intent = new Intent(this, BluetoothBackgroundService.class);
            stopService(intent);
        } else {
            Log.d(TAG, "Start bluetooth reader service");

            startBluetoothService();
        }
    }

    public void setServiceStarted(final boolean started) {
        if (started) {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStarted);

            setViewVisibility(R.id.readerStatusRow, View.VISIBLE);
        } else {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStopped);

            setViewVisibility(R.id.readerStatusRow, View.GONE);
        }

        Button start = (Button) findViewById(R.id.startService);
        if (started) {
            start.setText(R.string.stopService);
        } else {
            start.setText(R.string.startService);
        }
    }

    public void setBluetoothServiceStarted(final boolean started) {
        if (started) {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStarted);

            setViewVisibility(R.id.readerStatusRow, View.VISIBLE);
        } else {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStopped);

            setViewVisibility(R.id.readerStatusRow, View.GONE);
        }

        Button start = (Button) findViewById(R.id.startBluetoothService);
        if (started) {
            start.setText(R.string.stopService);
        } else {
            start.setText(R.string.startService);
        }
    }

    public void setReaderOpen(final boolean open) {
        if (open) {
            setTextViewText(R.id.readerStatus, R.string.readerStatusOpen);

            setViewVisibility(R.id.tagStatusRow, View.VISIBLE);
        } else {
            setTextViewText(R.id.readerStatus, R.string.readerStatusClosed);

            setViewVisibility(R.id.tagStatusRow, View.GONE);
        }
    }

    private void setViewVisibility(int id, int visibility) {
        View view = findViewById(id);
        view.setVisibility(visibility);
    }

    public void setTagPresent(final boolean present) {
        if (present) {
            setTextViewText(R.id.tagStatus, R.string.tagStatusPresent);
        } else {
            setTextViewText(R.id.tagStatus, R.string.tagStatusAbsent);
        }
    }

    public void setTextViewText(final int resource, final int string) {
        setTextViewText(resource, getString(string));
    }

    public void setTextViewText(final int resource, final String string) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = (TextView) findViewById(resource);
                textView.setText(string);
                textView.setVisibility(View.VISIBLE);
            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if (android.os.Build.VERSION.SDK_INT >= 14) {
            mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share).getActionProvider();

            // If you use more than one ShareActionProvider, each for a different action,
            // use the following line to specify a unique history file for each one.
            // mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");

            // Set the default share intent
            mShareActionProvider.setShareIntent(getDefaultShareIntent());
        }
        return true;
    }


    private Intent getDefaultShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.skjolberg.nfc.external");
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_preferences: {
                openPreferences(item.getActionView());

                return true;
            }

            case R.id.menu_share: {
                showShare();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void openPreferences(View view) {
        Log.d(TAG, "Show preferences");

        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }

    private void showShare() {
        startActivity(Intent.createChooser(getDefaultShareIntent(), getString(R.string.share)));
    }

}
