package com.github.skjolber.nfc.external;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.skjolber.nfc.NfcReader;
import com.github.skjolber.nfc.NfcService;
import com.github.skjolber.nfc.NfcTag;
import com.github.skjolber.nfc.acs.Acr1255UReader;
import com.github.skjolber.nfc.acs.AcrAutomaticPICCPolling;
import com.github.skjolber.nfc.acs.AcrPICC;
import com.github.skjolber.nfc.acs.AcrReader;
import com.github.skjolber.nfc.service.BackgroundUsbService;
import com.github.skjolber.nfc.service.BluetoothBackgroundService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends Activity {

    protected static final String PREFERENCE_MODE = "mode";
    protected static final String PREFERENCE_BLUETOOTH_RESULTS = "bluetoothResults";

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
            getFragmentManager().beginTransaction().add(R.id.container, fragment = new PlaceholderFragment()).commit();
        }

    }

    private static final String TAG = MainActivity.class.getName();

    private ShareActionProvider mShareActionProvider;
    private PlaceholderFragment fragment;
    private boolean recieveTagBroadcasts = false;
    private boolean recieveReaderBroadcasts = false;
    private boolean recieveServiceBroadcasts = false;

    private boolean usbRunning = false;
    private boolean bluetoothRunning = false;

    private final BroadcastReceiver tagReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.d(TAG, "Custom broacast receiver");

            setTagPresent(!NfcTag.ACTION_TAG_LEFT_FIELD.equals(action));
        }
    };

    private class InitReaderTask extends AsyncTask<AcrReader, Void, Exception> {

        private AcrReader reader;

        public InitReaderTask(AcrReader reader) {
            this.reader = reader;
        }

        @Override
        protected Exception doInBackground(AcrReader... params) {

            Exception result = null;

            try {
                Log.d(TAG, "Got reader " + reader + " with name " + reader.getName() + " and firmware " + reader.getFirmware() + " and picc " + reader.getPICC());

                if (reader instanceof Acr1255UReader) {
                    Acr1255UReader bluetoothReader = (Acr1255UReader) reader;

                    // Log.d(TAG, "Battery level is " + bluetoothReader.getBatteryLevel() + "%");

                    bluetoothReader.setPICC(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B);

                    bluetoothReader.setAutomaticPICCPolling(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4, AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_1000);
                    bluetoothReader.setAutomaticPolling(true);
                }
            } catch(Exception e) {
                Log.e(TAG, "Problem initializing reader", e);

                result = e;
            }


            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
        }
    }

    private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.d(TAG, "Custom broacast receiver: " + action);

            if (NfcReader.ACTION_READER_OPENED.equals(action)) {
                setReaderOpen(true);

                Log.d(TAG, "Reader open");

            	if(intent.hasExtra(NfcReader.EXTRA_READER_CONTROL)) {

                    AcrReader reader = intent.getParcelableExtra(NfcReader.EXTRA_READER_CONTROL);

                    new InitReaderTask(reader).execute();
            	} else {
            		Log.d(TAG, "No reader");
            	}

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
                if(fragment.isUsbMode()) {
                    setUsbServiceStarted(true);
                } else {
                    setBluetoothServiceStarted(true);
                }
            } else if (NfcService.ACTION_SERVICE_STOPPED.equals(action)) {
                if(fragment.isUsbMode()) {
                    setUsbServiceStarted(false);
                } else {
                    setBluetoothServiceStarted(false);
                }
            } else {
                Log.d(TAG, "Ignore action " + action);
            }
        }

    };


    public void startUsbService() {
        Intent intent = new Intent();
        intent.setClassName("com.github.skjolber.nfc.external", "com.github.skjolber.nfc.service.BackgroundUsbService");
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        stopReceivingTagBroadcasts();
        stopReceivingReaderBroadcasts();
        stopReceivingServiceBroadcasts();

        super.onDestroy();
    }

    private void stopUsbService() {
        Intent intent = new Intent();
        intent.setClassName("com.github.skjolber.nfc.external", "com.github.skjolber.nfc.service.BackgroundUsbService");
        stopService(intent);
    }

    private void stopBluetoothService() {
        Intent intent = new Intent();
        intent.setClassName("com.github.skjolber.nfc.external", "com.github.skjolber.nfc.service.BluetoothBackgroundService");
        stopService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        startReceivingTagBroadcasts();
        startReceivingReaderBroadcasts();
        startReceivingServiceBroadcasts();

        broadcast(NfcService.ACTION_SERVICE_STATUS);
        broadcast(NfcReader.ACTION_READER_STATUS);
    }

    @Override
    public void onPause() {
        stopReceivingTagBroadcasts();
        stopReceivingReaderBroadcasts();
        stopReceivingServiceBroadcasts();

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

            registerReceiver(tagReceiver, filter);
        }
    }

    private void stopReceivingTagBroadcasts() {
        if (recieveTagBroadcasts) {
            Log.d(TAG, "Stop receiving tag broadcasts");

            recieveTagBroadcasts = false;

            unregisterReceiver(tagReceiver);
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

        private List<BluetoothResult> bluetoothResults = new ArrayList<>();
        private Spinner bluetoothResultsSpinner;
        private View rootView;

        private Spinner modeSpinner;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);

            createModeSpinner(rootView);
            createBluetoothDeviceSpinner(rootView);

            return rootView;
        }

        private void createBluetoothDeviceSpinner(View rootView) {

            loadBluetoothResultsFromPreferences();

            this.bluetoothResultsSpinner = (Spinner) rootView.findViewById(R.id.bluetooth_device_spinner);
            //spinner.setOnItemSelectedListener(new ModeListener(activity, rootView));

            refreshBluetoothSpinner();
        }

        private void refreshBluetoothSpinner() {
            List<String> names = new ArrayList<>();
            for(BluetoothResult result : bluetoothResults) {
                names.add(result.getName());
            }

            if(names.isEmpty()) {
                activity.getString(R.string.bluetooth_device_none);
            }

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, names.toArray(new String[names.size()]));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            bluetoothResultsSpinner.setAdapter(adapter);

            for(int i = 0; i < bluetoothResults.size(); i++) {
                if(bluetoothResults.get(i).isSelected()) {
                    bluetoothResultsSpinner.setSelection(i);

                    break;
                }
            }
        }

        private void createModeSpinner(View rootView) {
            modeSpinner = (Spinner) rootView.findViewById(R.id.mode_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.service_modes, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modeSpinner.setAdapter(adapter);

            ModeListener modeListener = new ModeListener(activity, rootView);
            modeSpinner.setOnItemSelectedListener(modeListener);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            int selection = prefs.getInt("mode", 0);
            modeSpinner.setSelection(selection);
            modeListener.displayItem(selection);
        }

        public BluetoothResult getSelectedBluetoothResult() {
            if(bluetoothResults.isEmpty()) {
                return null;
            }

            return bluetoothResults.get(bluetoothResultsSpinner.getSelectedItemPosition());
        }

        public void addBluetoothDevice(BluetoothResult result) {
            for(BluetoothResult r : bluetoothResults) {
                r.setSelected(false);
            }

            int index = -1;
            for(int i = 0; i < bluetoothResults.size(); i++) {
                BluetoothResult r = bluetoothResults.get(i);
                if(result.equals(r)) {
                    index = i;
                    break;
                }
            }

            if(index == -1) {
                index = bluetoothResults.size();
                bluetoothResults.add(index, result);
                saveBluetoothResultsToPreferences();

                refreshBluetoothSpinner();
            } else {
                bluetoothResultsSpinner.setSelection(index);
            }
        }

        public void loadBluetoothResultsFromPreferences() {
            Log.d(TAG, "Load bluetooth results from preferences");
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String string = prefs.getString(PREFERENCE_BLUETOOTH_RESULTS, null);
            if(string != null) {
                Type type = new TypeToken<List<BluetoothResult>>(){}.getType();
                Gson gson = new Gson();
                List<BluetoothResult> list = gson.fromJson(string, type);

                bluetoothResults.addAll(list);
            }
        }

        public void saveBluetoothResultsToPreferences() {
            Log.d(TAG, "Save bluetooth results to preferences");
            Type type = new TypeToken<List<BluetoothResult>>(){}.getType();
            Gson gson = new Gson();
            String string = gson.toJson(bluetoothResults, type);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            Editor edit = prefs.edit();

            edit.putString(PREFERENCE_BLUETOOTH_RESULTS, string);

            edit.commit();
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

        public boolean isUsbMode() {
            return modeSpinner.getSelectedItemPosition() == 0;
        }
    }

    public static class ModeListener implements AdapterView.OnItemSelectedListener {

        private Activity activity;
        private View view;

        public ModeListener(Activity activity, View view) {
            this.activity = activity;
            this.view = view;
        }

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            displayItem(pos);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            Editor edit = prefs.edit();

            edit.putInt(PREFERENCE_MODE, pos);

            edit.commit();
        }

        public void displayItem(int pos) {
            View bluetooth = view.findViewById(R.id.bluetoothModeLayout);
            View usb = view.findViewById(R.id.usbModeLayout);
            if(pos == 0) {
                // usb
                usb.setVisibility(View.VISIBLE);
                bluetooth.setVisibility(View.GONE);
            } else {
                usb.setVisibility(View.GONE);
                bluetooth.setVisibility(View.VISIBLE);
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
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

        if (usbRunning) {
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
            stopBluetoothReaderService();
        } else {
            BluetoothResult result = fragment.getSelectedBluetoothResult();
            if(result != null) {
                startBluetoothService(result);
            } else {
                addBluetoothDevice(null);
            }
        }
    }

    private void stopBluetoothReaderService() {
        Log.d(TAG, "Stop bluetooth reader service");

        Intent intent = new Intent(this, BluetoothBackgroundService.class);
        stopService(intent);
    }

    public void setUsbServiceStarted(final boolean started) {
        this.usbRunning = started;

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
        this.bluetoothRunning = started;
        if (started) {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStarted);

            setViewVisibility(R.id.readerStatusRow, View.VISIBLE);
        } else {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStopped);

            setViewVisibility(R.id.readerStatusRow, View.GONE);
        }

        Button start = (Button) findViewById(R.id.startBluetoothService);
        if (started) {
            start.setText(R.string.bluetooth_service_stop);
        } else {
            start.setText(R.string.bluetooth_service_start);
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

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void openPreferences(View view) {
        Log.d(TAG, "Show preferences");

        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK){
            BluetoothResult result = new BluetoothResult(data.getStringExtra(BluetoothBackgroundService.EXTRAS_DEVICE_NAME), data.getStringExtra(BluetoothBackgroundService.EXTRAS_DEVICE_ADDRESS), true);
            fragment.addBluetoothDevice(result);

            startBluetoothService(result);
        }
    }

    public void addBluetoothDevice(View view) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivityForResult(intent, 0);
    }

    private void startBluetoothService(BluetoothResult result) {
        Intent intent = new Intent(this, BluetoothBackgroundService.class);
        intent.putExtra(BluetoothBackgroundService.EXTRAS_DEVICE_NAME, result.getName());
        intent.putExtra(BluetoothBackgroundService.EXTRAS_DEVICE_ADDRESS, result.getAddress());

        startService(intent);
    }

    public static class BluetoothResult {
        protected String name;
        protected String address;
        protected boolean selected;

        public BluetoothResult(String name, String address, boolean selected) {
            this.name = name;
            this.address = address;
            this.selected = selected;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String device) {
            this.address = device;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public BluetoothResult() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BluetoothResult that = (BluetoothResult) o;
            return name.equals(that.name) &&
                    address.equals(that.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, address);
        }
    }
}
