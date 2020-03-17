package com.github.skjolber.nfc.service;


import java.nio.charset.Charset;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr1255uj1Reader.OnBatteryLevelAvailableListener;
import com.acs.bluetooth.Acr1255uj1Reader.OnBatteryLevelChangeListener;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderGattCallback.OnConnectionStateChangeListener;
import com.acs.bluetooth.BluetoothReaderManager;
import com.acs.bluetooth.BluetoothReaderManager.OnReaderDetectionListener;
import com.github.skjolber.nfc.hce.DefaultNfcReaderServiceListener;
import com.github.skjolber.nfc.hce.IAcr1255UBinder;
import com.github.skjolber.nfc.NfcReader;
import com.github.skjolber.nfc.NfcTag;
import com.github.skjolber.nfc.command.*;
import com.github.skjolber.nfc.service.bt.CustomBluetoothReaderManager;
import com.github.skjolber.nfc.service.utils.BluetoothAcsTag;

import org.nfctools.api.TagType;

public class BluetoothBackgroundService extends AbstractService {

    private static final byte[] AUTO_POLLING_START = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x01};
    private static final byte[] AUTO_POLLING_STOP = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x00};

    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private static final byte[] DEFAULT_1255_APDU_COMMAND = new byte[]{(byte) 0xFF, (byte) 0xB0, 0x00, 0x04, 0x01};
    /* Get firmware version escape command. */
    private static final String DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 18 00";

    private static final String TAG = BluetoothBackgroundService.class.getName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    /** please note: device will be bricked when authentication operation fails 5x */
    public static final String EXTRAS_DEVICE_AUTHENTICATION = "DEVICE_AUTHENTICATION";

    private boolean detectReader = false;
    private boolean receivingBondingStateBroadcasts = false;

    private IAcr1255UBinder acr1255Binder;

    private BluetoothAdapter mBluetoothAdapter;

    /* Detected reader. */
    private BluetoothReader mBluetoothReader;
    /* ACS Bluetooth reader library. */
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;

    /* Bluetooth GATT client. */
    private BluetoothGatt mBluetoothGatt;

    /* Reader to be connected. */
    private String mDeviceName;
    private String mDeviceAddress;
    private int mConnectState = BluetoothReader.STATE_DISCONNECTED;

    /* Default master key. */
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";
    private byte[] masterKey;
    /*
     * Listen to Bluetooth bond status change event. And turns on reader's
     * notifications once the card reader is bonded.
     */

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        public void onReceive(Context context, Intent intent) {
            BluetoothAdapter bluetoothAdapter = null;
            BluetoothManager bluetoothManager = null;
            final String action = intent.getAction();

            /*
            if (!(mBluetoothReader instanceof Acr3901us1Reader)) {
                // Only ACR3901U-S1 require bonding.
                return;
            }
            */

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "ACTION_BOND_STATE_CHANGED");

                /* Get bond (pairing) state */
                if (mBluetoothReaderManager == null) {
                    Log.w(TAG, "Unable to initialize BluetoothReaderManager.");
                    return;
                }

                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager == null) {
                    Log.w(TAG, "Unable to initialize BluetoothManager.");
                    return;
                }

                bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null) {
                    Log.w(TAG, "Unable to initialize BluetoothAdapter.");
                    return;
                }

                final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mDeviceAddress);

                if (device == null) {
                    return;
                }

                final int bondState = device.getBondState();

                Log.i(TAG, "BroadcastReceiver - getBondState. state = " + getBondingStatusString(bondState));

                /* Enable notification */
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    if (mBluetoothReader != null) {
                        mBluetoothReader.enableNotification(true);
                    }
                }

                Log.d(TAG, getBondingStatusString(bondState));
            }
        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start command");

        synchronized (this) {
            if (intent.hasExtra(EXTRAS_DEVICE_NAME)) {
                mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
            } else {
                mDeviceName = null;
            }
            if (intent.hasExtra(EXTRAS_DEVICE_ADDRESS)) {
                mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
            } else {
                mDeviceAddress = null;
            }
            if (intent.hasExtra(EXTRAS_DEVICE_AUTHENTICATION)) {
                masterKey = intent.getByteArrayExtra(EXTRAS_DEVICE_AUTHENTICATION);
            } else {
                masterKey = DEFAULT_1255_MASTER_KEY.getBytes(Charset.forName("UTF-8"));
            }
        }

        if (!started) { // the client app might autostart the service, but it might already be running
            started = true;

            startDetectingReader();
        } else {
            connectReader();
        }

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Service created");

        this.acr1255Binder = new IAcr1255UBinder();

        nfcReaderServiceListener = new DefaultNfcReaderServiceListener(null, null, null, null, null, null, acr1255Binder, this);

        nfcReaderServiceListener.onServiceStarted();
    }

    @Override
    public ACR1255BluetoothCommands getReaderCommands() {
        return new ACR1255BluetoothCommands(mDeviceName, mBluetoothReader);
    }

    private void startDetectingReader() {
        synchronized (this) {
            if (!detectReader) {
                Log.d(TAG, "Start / resume detecting readers");

                detectReader = true;

                startReceivingBondingStateBroadcasts();

                /* Initialize BluetoothReaderGattCallback. */
                mGattCallback = new BluetoothReaderGattCallback();

                /* Register BluetoothReaderGattCallback's listeners */
                mGattCallback.setOnConnectionStateChangeListener(new OnConnectionStateChangeListener() {

                    @Override
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                    public void onConnectionStateChange(final BluetoothGatt gatt, final int state, final int newState) {

                        if (state != BluetoothGatt.GATT_SUCCESS) {
                            /*
                             * Show the message on fail to
                             * connect/disconnect.
                             */
                            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);

                            if (newState == BluetoothReader.STATE_CONNECTED) {
                                Log.d(TAG, "Connect  fail with state " + state);
                            } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                                Log.d(TAG, "Disconnect fail with state " + state);
                            } else {
                                Log.d(TAG, "Unknown error state " + state);
                            }

                            readerOpen = false;

                            nfcReaderServiceListener.onReaderClosed(NfcReader.READER_STATUS_USB_DEVICE_DISCONNECTED, null);

                            return;
                        }

                        updateConnectionState(newState);

                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            /* Detect the connected reader. */
                            Log.d(TAG, "Connected");
                            if (mBluetoothReaderManager != null) {
                                mBluetoothReaderManager.detectReader(gatt, mGattCallback);
                            }
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.d(TAG, "Disconnected");
                            mBluetoothReader = null;
                            /*
                             * Release resources occupied by Bluetooth
                             * GATT client.
                             */
                            if (mBluetoothGatt != null) {
                                mBluetoothGatt.close();
                                mBluetoothGatt = null;
                            }

                            readerOpen = false;

                            nfcReaderServiceListener.onReaderClosed(NfcReader.READER_STATUS_USB_DEVICE_DISCONNECTED, null);
                        }
                    }

                });

                /* Initialize mBluetoothReaderManager. */
                mBluetoothReaderManager = new CustomBluetoothReaderManager();

                /* Register BluetoothReaderManager's listeners */
                mBluetoothReaderManager.setOnReaderDetectionListener(new OnReaderDetectionListener() {

                    @Override
                    public void onReaderDetection(BluetoothReader reader) {
                        if (reader instanceof Acr1255uj1Reader) {
                            /* The connected reader is ACR1255U-J1 reader. */
                            Log.d(TAG, "Acr1255uj1Reader detected.");
                        } else {
                            Log.i(TAG, "Reader " + reader.getClass().getSimpleName() + " is not supported");

                            return;
                        }

                        mBluetoothReader = reader;
                        setListener(reader);
                        enableReaderNotifications(reader);
                    }
                });

                /* Connect the reader. */
                connectReader();
            }
        }
    }

    private void setListener(BluetoothReader reader) {
        /* Update status change listener */
        if(mBluetoothReader instanceof Acr1255uj1Reader) {
            Acr1255uj1Reader acr1255uj1Reader = (Acr1255uj1Reader)mBluetoothReader;
            acr1255uj1Reader.setOnBatteryLevelChangeListener(new OnBatteryLevelChangeListener() {

                @Override
                public void onBatteryLevelChange(
                        BluetoothReader bluetoothReader,
                        final int batteryLevel) {
                    Log.i(TAG, "mBatteryLevelListener data: " + getBatteryLevelString(batteryLevel));
                }

            });

            /* Wait for battery level available. */
            acr1255uj1Reader.setOnBatteryLevelAvailableListener(new OnBatteryLevelAvailableListener() {

                @Override
                public void onBatteryLevelAvailable(
                        BluetoothReader bluetoothReader,
                        final int batteryLevel, int status) {
                    Log.i(TAG, "mBatteryLevelListener data: " + getBatteryLevelString(batteryLevel));
                }
            });

        }

        mBluetoothReader.setOnAtrAvailableListener(new BluetoothReader.OnAtrAvailableListener() {
            @Override
            public void onAtrAvailable(BluetoothReader bluetoothReader, final byte[] atr, final int errorCode) {
                Log.d(TAG, "onAtrAvailable: " + com.github.skjolber.nfc.command.Utils.toHexString(atr) + " " + getErrorString(errorCode));

                TagType tagType = ServiceUtil.identifyTagType("Acr1255uj1Reader", atr);

                Log.d(TAG, "Got tag type " + tagType);

                new InitTask(atr).execute(tagType);

            }
        });

        /* Wait for power off response. */
        mBluetoothReader.setOnCardPowerOffCompleteListener(new BluetoothReader.OnCardPowerOffCompleteListener() {

            @Override
            public void onCardPowerOffComplete(
                    BluetoothReader bluetoothReader, final int result) {
                Log.d(TAG, "setOnCardPowerOffCompleteListener: " + getErrorString(result));
            }

        });

        /* Wait for response APDU. */
        mBluetoothReader.setOnResponseApduAvailableListener(new BluetoothReader.OnResponseApduAvailableListener() {

            @Override
            public void onResponseApduAvailable(BluetoothReader bluetoothReader, final byte[] apdu, final int errorCode) {
                Log.d(TAG, "setOnResponseApduAvailableListener: " + getResponseString(apdu, errorCode));
            }

        });

        mBluetoothReader.setOnEscapeResponseAvailableListener(new BluetoothReader.OnEscapeResponseAvailableListener() {

            @Override
            public void onEscapeResponseAvailable(
                    BluetoothReader bluetoothReader,
                    final byte[] response, final int errorCode) {
                Log.d(TAG, "setOnEscapeResponseAvailableListener: " + getResponseString(response, errorCode));
            }

        });

        /* Wait for device info available. */
        mBluetoothReader.setOnDeviceInfoAvailableListener(new BluetoothReader.OnDeviceInfoAvailableListener() {

            @Override
            public void onDeviceInfoAvailable(BluetoothReader bluetoothReader, final int infoId, final Object o, final int status) {
                Log.d(TAG, "onDeviceInfoAvailable");
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Failed to read device info!");
                    return;
                }
                switch (infoId) {
                    case BluetoothReader.DEVICE_INFO_SYSTEM_ID: {
                        Log.i(TAG, "Device system id " + Utils.convertBinToASCII((byte[])o));
                    }
                    break;
                    case BluetoothReader.DEVICE_INFO_MODEL_NUMBER_STRING:
                        Log.i(TAG, "Device model number " + o);
                        break;
                    case BluetoothReader.DEVICE_INFO_SERIAL_NUMBER_STRING:
                        Log.i(TAG, "Device serial number " + o);
                        break;
                    case BluetoothReader.DEVICE_INFO_FIRMWARE_REVISION_STRING:
                        Log.i(TAG, "Device firmware revision " + o);
                        break;
                    case BluetoothReader.DEVICE_INFO_HARDWARE_REVISION_STRING:
                        Log.i(TAG, "Device hardware revision " + o);
                        break;
                    case BluetoothReader.DEVICE_INFO_MANUFACTURER_NAME_STRING:
                        Log.i(TAG, "Device manufacturer name " + o);
                        break;
                    default:
                        break;
                }
            }

        });

        /* Wait for power off response. */
        mBluetoothReader.setOnCardPowerOffCompleteListener(new BluetoothReader.OnCardPowerOffCompleteListener() {
            @Override
            public void onCardPowerOffComplete(
                    BluetoothReader bluetoothReader, final int result) {
                Log.d(TAG, "onCardPowerOffComplete: " + getErrorString(result));
            }

        });

        /* Handle on slot status available. */
        mBluetoothReader.setOnCardStatusAvailableListener(new BluetoothReader.OnCardStatusAvailableListener() {

            @Override
            public void onCardStatusAvailable(
                    BluetoothReader bluetoothReader,
                    final int cardStatus, final int errorCode) {
                if (errorCode != BluetoothReader.ERROR_SUCCESS) {
                    Log.i(TAG, "setOnCardStatusAvailableListener: " + getErrorString(errorCode));

                    onTagAbsent();
                } else {
                    Log.i(TAG, "setOnCardStatusAvailableListener: " + getCardStatusString(cardStatus));

                    if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
                        if (!mBluetoothReader.powerOnCard()) {
                            Log.d(TAG, "Card not ready for power");
                        }
                    } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
                        if (!mBluetoothReader.transmitApdu(DEFAULT_1255_APDU_COMMAND)) {
                            Log.d(TAG, "Card not ready for apdu");
                        }
                    } else if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
                        onTagAbsent();
                    }
                }
            }

        });

        mBluetoothReader.setOnCardStatusChangeListener(new BluetoothReader.OnCardStatusChangeListener() {

            @Override
            public void onCardStatusChange(BluetoothReader bluetoothReader, final int cardStatus) {
                Log.i(TAG, "onCardStatusChange sta: " + getCardStatusString(cardStatus));

                if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
                    if (!mBluetoothReader.powerOnCard()) {
                        Log.d(TAG, "Card not ready for power");
                    } else {
                        Log.d(TAG, "Power on card..");

                        //setListener(bluetoothReader);
                    }
                } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
                    Log.d(TAG, "Card status powered");
                } else if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
                    Log.d(TAG, "Card status absent");
                    onTagAbsent();
                }
            }

        });
    }

    /* Start the process to enable the reader's notifications: Enables the reader's battery status or level, card status and response notifications. */
    private void enableReaderNotifications(BluetoothReader reader) {
        if (reader == null) {
            return;
        }

        if (mBluetoothReader instanceof Acr1255uj1Reader) {
            mBluetoothReader.setOnEnableNotificationCompleteListener(new BluetoothReader.OnEnableNotificationCompleteListener() {

                @Override
                public void onEnableNotificationComplete(BluetoothReader bluetoothReader, final int result) {
                    if (result != BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "The device is unable to set notification!");
                    } else {
                        new AuthTask().execute();
                    }
                }
            });

            if (!mBluetoothReader.enableNotification(true)) {
                Log.d(TAG, "Unable to enable notifications");
            }
        }
    }

    private class AuthTask extends AsyncTask<Void, Void, Exception> {

        @Override
        protected Exception doInBackground(Void... params) {

            Exception result = null;
            Log.i(TAG, "Attempt to authenticate reader");

            mBluetoothReader.setOnAuthenticationCompleteListener(new BluetoothReader.OnAuthenticationCompleteListener() {

                @Override
                public void onAuthenticationComplete(BluetoothReader bluetoothReader, final int errorCode) {
                    if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                        Log.d(TAG, "Authentication success!");


                        ACR1255BluetoothCommands commands = getReaderCommands();

                        binder.setReaderTechnology(new ACRReaderBluetoothTechnology(commands));

                        nfcReaderServiceListener.onReaderOpen(commands, NfcReader.READER_STATUS_OK);

                        readerOpen = true;
                    } else {
                        Log.d(TAG, "Authentication failure. Warning, only six attempts can be made before the reader is permanently locked.");
                    }
                }

            });

            try {

                if (!mBluetoothReader.authenticate(masterKey)) {
                    Log.w(TAG, "Reader unexpectedly was not ready for authenticate");
                } else {
                    Log.d(TAG, "Authenticating...");
                }
            } catch (Exception e) {
                Log.e(TAG, "Problem authenticating. Warning, only six attempts can be made before the reader is permanently locked.", e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
        }
    }

    private class InitTask extends AsyncTask<TagType, Void, Exception> {

        private byte[] atr;

        public InitTask(byte[] atr) {
            this.atr = atr;
        }

        @Override
        protected Exception doInBackground(TagType... params) {

            Exception result = null;

            TagType tagType = params[0];

            if (uidMode) {
                Log.d(TAG, "UID mode");
                handleTagInitUIDMode(tagType);
            } else {
                Log.d(TAG, "Regular mode");
                handleTagInitRegularMode(tagType, atr);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
        }
    }

    private void handleTagInitRegularMode(TagType tagType, byte[] atr) {

        BluetoothAcsTag acsTag = new BluetoothAcsTag(TagType.MIFARE_ULTRALIGHT, new byte[]{}, mBluetoothReader);
        IsoDepWrapper wrapper = new ACSBluetoothIsoDepWrapper(mBluetoothReader);

        if (tagType == TagType.MIFARE_ULTRALIGHT || tagType == TagType.MIFARE_ULTRALIGHT_C) {
            mifareUltralight(0, atr, tagType, acsTag, wrapper, "ACR1255U");
        } else if (
                tagType == TagType.MIFARE_PLUS_SL1_2k ||
                        tagType == TagType.MIFARE_PLUS_SL1_4k ||
                        tagType == TagType.MIFARE_PLUS_SL2_2k ||
                        tagType == TagType.MIFARE_PLUS_SL2_4k
        ) {
            mifareClassicPlus(0, atr, tagType, acsTag, wrapper);
        } else if (
                tagType == TagType.MIFARE_CLASSIC_1K || tagType == TagType.MIFARE_CLASSIC_4K) {
            mifareClassic(0, atr, tagType, wrapper, acsTag);
        } else if (tagType == TagType.INFINEON_MIFARE_SLE_1K) {
            infineonMifare(0, atr, tagType, acsTag, wrapper);
        } else if (tagType == TagType.DESFIRE_EV1) {
            desfire(0, atr, wrapper);
        } else if (tagType == TagType.ISO_14443_TYPE_B_NO_HISTORICAL_BYTES || tagType == TagType.ISO_14443_TYPE_A_NO_HISTORICAL_BYTES) {
            hce(0, atr, wrapper);
        } else {
            ServiceUtil.sendTechBroadcast(this);
        }
    }

    private void handleTagInitUIDMode(TagType tagType) {
        if (tagType == TagType.MIFARE_ULTRALIGHT || tagType == TagType.MIFARE_ULTRALIGHT_C) {
            try {
                BluetoothAcsTag tag = new BluetoothAcsTag(TagType.MIFARE_ULTRALIGHT, new byte[]{}, mBluetoothReader);

                ServiceUtil.ultralight(BluetoothBackgroundService.this, tag);
            } catch (Exception e) {
                Log.d(TAG, "Problem reading from tag", e);

                ServiceUtil.sendTechBroadcast(BluetoothBackgroundService.this);
            }
        } else if (
                tagType == TagType.MIFARE_PLUS_SL1_2k ||
                        tagType == TagType.MIFARE_PLUS_SL1_4k ||
                        tagType == TagType.MIFARE_PLUS_SL2_2k ||
                        tagType == TagType.MIFARE_PLUS_SL2_4k ||
                        tagType == TagType.MIFARE_CLASSIC_1K ||
                        tagType == TagType.MIFARE_CLASSIC_4K ||
                        tagType == TagType.INFINEON_MIFARE_SLE_1K
        ) {

            try {
                BluetoothAcsTag tag = new BluetoothAcsTag(TagType.MIFARE_ULTRALIGHT, new byte[]{}, mBluetoothReader);

                ServiceUtil.mifareClassic(BluetoothBackgroundService.this, tag);
            } catch (Exception e) {
                Log.d(TAG, "Problem reading from tag", e);

                ServiceUtil.sendTechBroadcast(BluetoothBackgroundService.this);
            }

        } else if (tagType == TagType.DESFIRE_EV1) {
            try {
                IsoDepWrapper wrapper = new ACSBluetoothIsoDepWrapper(mBluetoothReader);

                ServiceUtil.desfire(BluetoothBackgroundService.this, wrapper);
            } catch (Exception e) {
                Log.d(TAG, "Problem reading from tag", e);

                ServiceUtil.sendTechBroadcast(BluetoothBackgroundService.this);
            }

        } else if (tagType == TagType.ISO_14443_TYPE_B_NO_HISTORICAL_BYTES || tagType == TagType.ISO_14443_TYPE_A_NO_HISTORICAL_BYTES) {
            // impossible to get tag id, there is just a phone
            ServiceUtil.sendTechBroadcast(BluetoothBackgroundService.this);
        } else {
            ServiceUtil.sendTechBroadcast(BluetoothBackgroundService.this);
        }

    }

    /* Get the Battery level string. */
    private static String getBatteryLevelString(int batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
            return "Unknown.";
        }
        return String.valueOf(batteryLevel) + "%";
    }

    /* Get the Battery status string. */
    private static String getBatteryStatusString(int batteryStatus) {
        if (batteryStatus == BluetoothReader.BATTERY_STATUS_NONE) {
            return "No battery.";
        } else if (batteryStatus == BluetoothReader.BATTERY_STATUS_FULL) {
            return "The battery is full.";
        } else if (batteryStatus == BluetoothReader.BATTERY_STATUS_USB_PLUGGED) {
            return "The USB is plugged.";
        }
        return "The battery is low.";
    }

    /* Get the Bonding status string. */
    private static String getBondingStatusString(int bondingStatus) {
        if (bondingStatus == BluetoothDevice.BOND_BONDED) {
            return "BOND BONDED";
        } else if (bondingStatus == BluetoothDevice.BOND_NONE) {
            return "BOND NONE";
        } else if (bondingStatus == BluetoothDevice.BOND_BONDING) {
            return "BOND BONDING";
        }
        return "BOND UNKNOWN.";
    }

    /* Get the Card status string. */
    private static String getCardStatusString(int cardStatus) {
        if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
            return "Absent.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
            return "Present.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
            return "Powered.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWER_SAVING_MODE) {
            return "Power saving mode.";
        }
        return "The card status is unknown.";
    }

    /* Get the Error string. */
    private static String getErrorString(int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            return "";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
            return "The checksum is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
            return "The data length is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
            return "The command is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
            return "The command ID is unknown.";
        } else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
            return "The card operation failed.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
            return "Authentication is required.";
        } else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
            return "The battery is low.";
        } else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
            return "Error characteristic is not found.";
        } else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
            return "Write command to reader is failed.";
        } else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
            return "Timeout.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
            return "Authentication is failed.";
        } else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
            return "Undefined error.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
            return "Received data error.";
        } else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED) {
            return "The command failed.";
        }
        return "Unknown error.";
    }

    /* Get the Response string. */
    public static String getResponseString(byte[] response, int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            StringBuilder builder = new StringBuilder();
            if (response != null && response.length > 0) {
                builder.append(ACRCommands.toHexString(response));
                builder.append(' ');
            }
            builder.append("Success");

            return builder.toString();
        }
        return getErrorString(errorCode);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");

        disconnectReader();

        stopReceivingBondingStateBroadcasts();

        nfcReaderServiceListener.onServiceStopped();

        super.onDestroy();
    }

    private void startReceivingBondingStateBroadcasts() {
        synchronized (this) {
            if (!receivingBondingStateBroadcasts) {
                receivingBondingStateBroadcasts = true;
                Log.d(TAG, "Start receiving bonding state broadcasts");

                final IntentFilter intentFilter = new IntentFilter();

                intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver, intentFilter);
            }
        }
    }

    private void stopReceivingBondingStateBroadcasts() {
        synchronized (this) {
            if (receivingBondingStateBroadcasts) {
                Log.d(TAG, "Stop receiving bonding state broadcasts");

                receivingBondingStateBroadcasts = false;

                try {
                    unregisterReceiver(mBroadcastReceiver);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
    }

    /*
     * Create a GATT connection with the reader. And detect the connected reader
     * once service list is available.
     */
    private boolean connectReader() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.w(TAG, "Unable to initialize BluetoothManager.");
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return false;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Unable to obtain a BluetoothAdapter.");
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return false;
        }

        /*
         * Connect Device.
         */
        /* Clear old GATT connection. */
        if (mBluetoothGatt != null) {
            Log.i(TAG, "Clear old GATT connection");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mDeviceAddress == null) {
            Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {

                for (BluetoothDevice mDevice : mPairedDevices) {
                    Log.i(TAG, "Connect bonded bluetooth device " + mDevice.getName() + " " + mDevice.getAddress());

                    mDeviceAddress = mDevice.getAddress();
                    mDeviceName = mDevice.getName();
                }
            } else {
                Log.i(TAG, "No connected bluetooth devices and no provided address.");

                return false;
            }
        } else {
            Log.i(TAG, "Connect bluetooth device " + mDeviceName + " " + mDeviceAddress);
        }

        /* Create a new connection. */
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mDeviceAddress);

        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        /* Connect to GATT internal_invoker. */
        updateConnectionState(BluetoothReader.STATE_CONNECTING);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);

            // https://stackoverflow.com/questions/53726759/xamarin-bluetooth-data-receive-delay
            // https://stackoverflow.com/questions/31742817/delay-between-writecharacteristic-and-callback-oncharacteristicwrite?noredirect=1&lq=1
            // https://punchthrough.com/maximizing-ble-throughput-part-2-use-larger-att-mtu-2/

            // When using larger ATT_MTU, the throughput is increased about 0-15% as we eliminate transferring ATT layer overhead bytes and replacing them with data.
            // Using ATT_MTU sizes that are multiples of 23 bytes or (Link Layer Data Field – L2CAP Header Size(4 bytes)) is ideal.

            mBluetoothGatt.requestMtu(138);
        }
        return true;
    }

    /* Disconnects an established connection. */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void disconnectReader() {
        if (mBluetoothGatt == null) {
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return;
        }
        updateConnectionState(BluetoothReader.STATE_DISCONNECTING);
        mBluetoothGatt.disconnect();
    }

    /* Update the display of Connection status string. */
    private void updateConnectionState(final int connectState) {
        mConnectState = connectState;

        if (connectState == BluetoothReader.STATE_CONNECTING) {
            Log.d(TAG, "Connecting");
        } else if (connectState == BluetoothReader.STATE_CONNECTED) {
            Log.d(TAG, "Connected");
        } else if (connectState == BluetoothReader.STATE_DISCONNECTING) {
            Log.d(TAG, "Disconnecting");
        } else {
            Log.d(TAG, "Disconnected");
        }
    }

    public void onTagAbsent() {
        Log.i(TAG, "onTagAbsent");

        store.removeItem(0);

        Intent intent = new Intent();
        intent.setAction(NfcTag.ACTION_TAG_LEFT_FIELD);
        sendBroadcast(intent);

        operations = null;
    }


}
