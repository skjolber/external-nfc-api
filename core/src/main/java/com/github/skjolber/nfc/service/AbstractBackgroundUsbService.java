package com.github.skjolber.nfc.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.acs.smartcard.ReaderException;
import com.acs.smartcard.RemovedCardException;
import com.acs.smartcard.TlvProperties;
import com.github.skjolber.nfc.hce.DefaultNfcReaderServiceListener;
import com.github.skjolber.nfc.hce.IAcr1222LBinder;
import com.github.skjolber.nfc.hce.IAcr122UBinder;
import com.github.skjolber.nfc.hce.IAcr1251UBinder;
import com.github.skjolber.nfc.hce.IAcr1252UBinder;
import com.github.skjolber.nfc.hce.IAcr1255UBinder;
import com.github.skjolber.nfc.hce.IAcr1281UBinder;
import com.github.skjolber.nfc.hce.IAcr1283Binder;
import com.github.skjolber.nfc.NfcReader;
import com.github.skjolber.nfc.NfcTag;
import com.github.skjolber.nfc.command.ACR1222Commands;
import com.github.skjolber.nfc.command.ACR122Commands;
import com.github.skjolber.nfc.command.ACR1251Commands;
import com.github.skjolber.nfc.command.ACR1252Commands;
import com.github.skjolber.nfc.command.ACR1255UsbCommands;
import com.github.skjolber.nfc.command.ACR1281Commands;
import com.github.skjolber.nfc.command.ACR1283Commands;
import com.github.skjolber.nfc.command.ACRCommands;
import com.github.skjolber.nfc.command.ACRReaderTechnology;
import com.github.skjolber.nfc.command.ReaderWrapper;
import com.github.skjolber.nfc.command.Utils;
import com.github.skjolber.nfc.skjolberg.reader.operations.NdefOperations;

import org.nfctools.api.TagType;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractBackgroundUsbService extends AbstractService {

    public static final String PREFERENCE_AUTO_START_ON_READER_CONNECT = "preference_auto_start_on_reader_connect";
    public static final String PREFERENCE_AUTO_STOP_ON_READER_DISCONNECT = "preference_auto_stop_on_reader_disconnect";
    public static final String PREFERENCE_AUTO_START_ON_RESTART = "preference_auto_start_on_restart";

    public static final String PREFERENCE_AUTO_READ_UID = "preference_auto_read_uid";

    private static final String TAG = AbstractBackgroundUsbService.class.getName();

    private static class Scanner extends Handler {

        private static final long USB_RESCAN_INTERVAL_STANDARD = 1000;
        private static final long USB_RESCAN_INTERVAL_READER_DETECTED = 10000;

        private WeakReference<AbstractBackgroundUsbService> activityReference;

        public Scanner(AbstractBackgroundUsbService activity) {
            this.activityReference = new WeakReference<AbstractBackgroundUsbService>(activity);
        }

        void resume() {
            synchronized (this) {
                if (!hasMessages(0)) {
                    sendEmptyMessage(0);
                }
            }
        }

        void resumeDelayed() {
            synchronized (this) {
                if (!hasMessages(0)) {
                    sendEmptyMessageDelayed(0, USB_RESCAN_INTERVAL_STANDARD);
                }
            }
        }

        void pause() {
            synchronized (this) {
                removeMessages(0);
            }
        }

        @Override
        public void handleMessage(Message message) {
            //Log.v(TAG, "Handle message");

            AbstractBackgroundUsbService activity = activityReference.get();
            if (activity != null) {
                if (activity.isDetectUSBDevice()) {
                    if (activity.detectUSBDevices()) {
                        Log.v(TAG, "Detected USB devices");
                        sendEmptyMessageDelayed(0, USB_RESCAN_INTERVAL_READER_DETECTED);

                    } else {
                        sendEmptyMessageDelayed(0, USB_RESCAN_INTERVAL_STANDARD);
                    }
                }
            }
        }
    }

    private final BroadcastReceiver usbDevicePermissionReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                Log.d(TAG, "Usb permission action with " + intent.getExtras().keySet());

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (device != null) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        Log.d(TAG, "Open reader: " + device.getDeviceName());

                        synchronized (AbstractBackgroundUsbService.this) {
                            openDevices.add(device.getDeviceId());
                        }

                        new OpenTask().execute(device);
                    } else {
                        Log.d(TAG, "Permission denied for device " + device.getDeviceName() + " / " + device.getDeviceId() + ", resume scanning.");

                        synchronized (AbstractBackgroundUsbService.this) {
                            refusedPermissionDevices.add(device.getDeviceId());
                        }

                        readerScanner.resume();
                    }
                } else {
                    Log.d(TAG, "Did not find any device");
                }

            }
        }
    };

    private final BroadcastReceiver usbDeviceDetachedReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                Log.d(TAG, "Usb device detached");

                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(reader.getDevice())) {
                        // Close reader
                        Log.d(TAG, "Closing reader " + reader.getReaderName());

                        new CloseTask().execute();
                    }
                }
            }
        }
    };

    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

        @Override
        protected Exception doInBackground(UsbDevice... params) {

            Exception result = null;

            stopDetectingReader();

            try {

                String name = params[0].getDeviceName();

                Log.d(TAG, "Opening reader " + params[0].getDeviceName());

                reader.open(params[0]);

                readerOpen = true;

                Log.d(TAG, "Opened reader " + name);

                startReceivingUsbDeviceDetachBroadcasts();

                setNfcReaderStatus(NfcReader.READER_STATUS_OK, null);

                ACRCommands acrCommands = getReaderCommands();

                binder.setReaderTechnology(new ACRReaderTechnology(acrCommands));

                int protocol = reader.getProtocol(0);

                Log.d(TAG, "Protocol is " + protocol);

                nfcReaderServiceListener.onReaderOpen(acrCommands, NfcReader.READER_STATUS_OK);

                synchronized (AbstractBackgroundUsbService.this) {
                    requestPermissionDevices.remove(params[0].getDeviceId());
                }

            } catch (Exception e) {
                Log.w(TAG, "Problem opening reader " + params[0].getDeviceName(), e);

                synchronized (AbstractBackgroundUsbService.this) {
                    if (e instanceof IllegalArgumentException && e.getMessage().contains("Cannot claim interface.")) {
                        Log.d(TAG, "Fail USB open, attemp to connect " + params[0].getDeviceId() + " again after a delay");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                        }

                        requestPermissionDevices.remove(params[0].getDeviceId());
                    }

                    openDevices.remove(params[0].getDeviceId());
                }

                result = e;

                startDetectingReader();

                int status;
                if (e instanceof IllegalArgumentException && e.getMessage().contains("Cannot claim interface.")) {
                    status = NfcReader.READER_STATUS_ERROR_UNABLE_TO_CLAIM_USB_INTERFACE;
                } else {
                    status = NfcReader.READER_STATUS_ERROR;
                }

                setNfcReaderStatus(status, result.toString());

                nfcReaderServiceListener.onReaderClosed(nfcReaderStatusCode, nfcReaderStatusMessage);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
            onOpenACR(result == null);
        }
    }

    public static byte[] passthrough(byte[] payload) {
        byte[] cmd = new byte[payload.length + 5];
        cmd[0] = (byte)0xff;
        cmd[1] = 0x0;
        cmd[2] = 0x0;
        cmd[3] = 0x0;
        cmd[4] = (byte)(payload.length & 0xFF);

        System.arraycopy(payload, 0, cmd, 5, payload.length);

        return cmd;
    }

    private class CloseTask extends AsyncTask<Void, Void, Exception> {

        @Override
        protected Exception doInBackground(Void... params) {

            Exception result = null;

            try {
                readerOpen = false;

                UsbDevice device = reader.getDevice();
                if (device != null) {
                    synchronized (AbstractBackgroundUsbService.this) {
                        openDevices.remove(device.getDeviceId());
                    }
                }

                reader.close();
            } catch (Exception e) {
                result = e;
            } finally {
                stopReceivingUsbDeviceDetachBroadcasts();

                setNfcReaderStatus(NfcReader.READER_STATUS_USB_DEVICE_DISCONNECTED, null);

                nfcReaderServiceListener.onReaderClosed(NfcReader.READER_STATUS_USB_DEVICE_DISCONNECTED, null);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
            onCloseACR(result == null);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AbstractBackgroundUsbService.this);

            boolean autoStop = prefs.getBoolean(PREFERENCE_AUTO_STOP_ON_READER_DISCONNECT, false);
            if (autoStop) {
                Log.d(TAG, "Auto stop on reader disconnect");

                stopSelf();
            } else {

                startDetectingReader();
            }
        }

    }

    private static final String ACTION_USB_PERMISSION = AbstractBackgroundUsbService.class.getPackage() + ".USB_PERMISSION";

    protected static final String[] stateStrings = {"Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    protected UsbManager mManager;
    protected ReaderWrapper reader;
    protected PendingIntent mPermissionIntent;

    private Scanner readerScanner;
    private boolean scanningForReader = false;

    private boolean recievingDetachBroadcasts = false;
    protected boolean detectReader = false;


    private Set<Integer> refusedPermissionDevices = new HashSet<Integer>();
    private Set<Integer> requestPermissionDevices = new HashSet<Integer>();
    private Set<Integer> openDevices = new HashSet<Integer>();

    private IAcr122UBinder acr122Binder;
    private IAcr1222LBinder acr1222Binder;
    private IAcr1251UBinder acr1251Binder;
    private IAcr1281UBinder acr1281Binder;
    private IAcr1283Binder acr1283Binder;
    private IAcr1252UBinder acr1252Binder;
    private IAcr1255UBinder acr1255Binder;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Service created");

        this.acr122Binder = new IAcr122UBinder();
        this.acr1222Binder = new IAcr1222LBinder();
        this.acr1251Binder = new IAcr1251UBinder();
        this.acr1281Binder = new IAcr1281UBinder();
        this.acr1283Binder = new IAcr1283Binder();
        this.acr1252Binder = new IAcr1252UBinder();
        this.acr1255Binder = new IAcr1255UBinder();

        nfcReaderServiceListener = new DefaultNfcReaderServiceListener(acr122Binder, acr1222Binder, acr1251Binder, acr1281Binder, acr1283Binder, acr1252Binder, acr1255Binder, this);

        initialize();

        nfcReaderServiceListener.onServiceStarted();
    }

    protected boolean detectUSBDevices() {
        Log.d(TAG, "Detecing USB devices..");

        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (reader.isSupported(device)) {
                //askingForPermission = true;

                Integer deviceId = device.getDeviceId();
                synchronized (this) {
                    if (openDevices.contains(deviceId)) {
                        Log.d(TAG, "Device " + deviceId + " is already open");
                    } else {
                        if (mManager.hasPermission(device)) {
                            Log.d(TAG, "Already has permission for reader: " + device.getDeviceName());

                            openDevices.add(deviceId);

                            new OpenTask().execute(device);

                            return true;
                        } else {
                            if (!requestPermissionDevices.contains(deviceId)) {
                                requestPermissionDevices.add(deviceId);

                                mManager.requestPermission(device, mPermissionIntent);

                                Log.d(TAG, "Detected ACR reader..");

                                return true;
                            } else {
                                Log.d(TAG, "Do not ask for permission for previous device " + device.getDeviceName() + " / " + device.getDeviceId());
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "Reader not supported: " + device.getDeviceName());
            }
        }

        return false;

    }

    public boolean isDetectUSBDevice() {
        return detectReader;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start command");
		
/*
		Calendar calendar = Calendar.getInstance();
		calendar.set(2017, Calendar.APRIL, 1);
		if(System.currentTimeMillis() > calendar.getTime().getTime()) {
			return Service.START_STICKY;
		}
*/
        if (!started) { // the client app might autostart the service, but it might already be running
            started = true;

            startDetectingReader();
        }

        return Service.START_STICKY;
    }

    protected void initialize() {
        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Initialize reader
        reader = new ReaderWrapper(mManager);


        reader.setOnStateChangeListener(new OnStateChangeListener() {

            @Override
            public void onStateChange(int slot, int prevState, int currState) {

                // Log.d(TAG, "From state " + prevState + " to " + currState);

                if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                if (prevState == Reader.CARD_ABSENT && currState == Reader.CARD_PRESENT) {
                    //Log.v(TAG, "Tag present on reader");

                    onTagPresent(slot);
                } else if (currState == Reader.CARD_ABSENT) {
                    //Log.v(TAG, "Tag absent on reader");

                    onTagAbsent(slot);
                } else {
                    Log.d(TAG, "Not action for state transition from " + stateStrings[prevState] + " to " + stateStrings[currState]);
                }

            }
        });

        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        readerScanner = new Scanner(this);
    }

    private class InitTagTask extends AsyncTask<Integer, Void, Exception> {


        @Override
        protected Exception doInBackground(Integer... params) {

            Exception result = null;

            int slotNumber = params[0];

            try {
                //Log.i(TAG, "Init tag at slot " + slotNumber);

                // https://en.wikipedia.org/wiki/Answer_to_reset#General_structure
                // http://smartcard-atr.appspot.com

                byte[] atr = reader.power(slotNumber, Reader.CARD_WARM_RESET);
                if (atr == null) {
                    Log.d(TAG, "No ATR, ignoring");

                    return null;
                }
                final TagType tagType;
                if (atr != null) {
                    tagType = ServiceUtil.identifyTagType(reader.getReaderName(), atr);
                } else {
                    tagType = TagType.UNKNOWN;
                }

                Log.d(TAG, "Tag inited as " + tagType + " for ATR " + Utils.toHexString(atr));

                handleTagInit(slotNumber, atr, tagType);
            } catch (RemovedCardException e) {
                Log.d(TAG, "Tag removed before it could be powered; ignore.", e);
            } catch (Exception e) {
                Log.w(TAG, "Problem initiating tag", e);

                ServiceUtil.sendTechBroadcast(AbstractBackgroundUsbService.this);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
            /*
             * Intent intent = new Intent();
             * intent.setAction("de.vogella.android.mybroadcast");
             * sendBroadcast(intent);
             */
        }
    }

    public abstract void handleTagInit(int slotNumber, byte[] atr, TagType tagType) throws ReaderException;

    public void onCloseACR(boolean success) {
        Log.i(TAG, "onCloseACR");

        binder.setReaderTechnology(null);
    }

    public void onOpenACR(boolean success) {
        Log.i(TAG, "onOpenACR");
    }

    public void onTagPresent(int slot) {
        //Log.d(TAG, "onTagPresent");

        operations = null;

        new InitTagTask().execute(slot);
    }

    public void onTagAbsent(int slot) {
        Log.i(TAG, "onTagAbsent");

        store.removeItem(slot);

        Intent intent = new Intent();
        intent.setAction(NfcTag.ACTION_TAG_LEFT_FIELD);
        intent.putExtra(NfcTag.EXTRA_TAG_SERVICE_HANDLE, slot);
        sendBroadcast(intent);

        operations = null;
    }

    private void startReceivingPermissionBroadcasts(boolean delay) {
        synchronized (this) {
            if (!scanningForReader) {
                Log.d(TAG, "Start scanning for reader");

                scanningForReader = true;

                // register receiver
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_USB_PERMISSION);
                registerReceiver(usbDevicePermissionReceiver, filter);

                if (!delay) {
                    readerScanner.resume();
                } else {
                    readerScanner.resumeDelayed();
                }
            }
        }
    }

    private void stopReceivingPermissionBroadcasts() {
        synchronized (this) {
            if (scanningForReader) {
                Log.d(TAG, "Stop scanning for reader");

                scanningForReader = false;

                readerScanner.pause();

                try {
                    unregisterReceiver(usbDevicePermissionReceiver);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
    }


    protected void startDetectingReader() {
        synchronized (this) {
            if (!detectReader) {
                Log.d(TAG, "Start / resume detecting readers");

                detectReader = true;

                startReceivingPermissionBroadcasts(false);
            }
        }
    }


    protected void stopDetectingReader() {
        synchronized (this) {
            if (detectReader) {
                Log.d(TAG, "Stop / pause detecting readers");

                detectReader = false;

                stopReceivingPermissionBroadcasts();
            }
        }
    }

    private void stopReceivingUsbDeviceDetachBroadcasts() {
        synchronized (this) {
            if (recievingDetachBroadcasts) {
                Log.d(TAG, "Stop recieving USB device detach broadcasts");

                recievingDetachBroadcasts = false;

                // Unregister receiver
                try {
                    unregisterReceiver(usbDeviceDetachedReceiver);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
    }

    private void startReceivingUsbDeviceDetachBroadcasts() {
        synchronized (this) {
            if (!recievingDetachBroadcasts) {
                Log.d(TAG, "Start recieving USB device detach broadcasts");

                recievingDetachBroadcasts = true;

                // register receiver
                IntentFilter filter = new IntentFilter();
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                registerReceiver(usbDeviceDetachedReceiver, filter);
            }
        }
    }

    @Override
    public void onDestroy() {
        detectReader = false;

        stopReceivingPermissionBroadcasts();

        stopReceivingUsbDeviceDetachBroadcasts();

        stopReceivingStatusBroadcasts();

        // Close reader
        if (reader != null) {
            try {
                if (readerOpen) {
                    setNfcReaderStatus(NfcReader.READER_STATUS_SERVICE_STOPPED, null);

                    synchronized (AbstractBackgroundUsbService.this) {
                        nfcReaderServiceListener.onReaderClosed(nfcReaderStatusCode, nfcReaderStatusMessage);
                    }

                    reader.close();
                }
            } catch (Exception e) {
                // ignore
                Log.d(TAG, "Problem closing reader", e);
            } finally {

            }
        }

        nfcReaderServiceListener.onServiceStopped();

        Log.i(TAG, "Service destroyed");

        super.onDestroy();
    }

    public void setNfcReaderStatus(int nfcReaderStatusCode, String nfcReaderStatusMessage) {
        synchronized (AbstractBackgroundUsbService.this) {
            this.nfcReaderStatusCode = nfcReaderStatusCode;
            this.nfcReaderStatusMessage = nfcReaderStatusMessage;
        }
    }

    public NdefOperations getNdefOperations() {
        return operations;
    }

    public ACRCommands getReaderCommands() {
        String name = reader.getReaderName();
        if (name != null) {
            if (name.contains("1222L")) {
                return new ACR1222Commands(name, reader);
            } else if (name.contains("122U")) {
                return new ACR122Commands(name, reader);
            } else if (name.contains("1251")) {
                return new ACR1251Commands(name, reader);
            } else if (name.contains("1281")) {
                return new ACR1281Commands(name, reader);
            } else if (name.contains("1283")) {
                return new ACR1283Commands(name, reader);
            } else if (name.contains("1252")) {
                return new ACR1252Commands(name, reader);
            } else if (name.contains("1255")) {
                return new ACR1255UsbCommands(name, reader);
            } else {
                Log.d(TAG, "No reader control for " + name);
            }
        }
        return new ACRCommands(reader);
    }


}
	

