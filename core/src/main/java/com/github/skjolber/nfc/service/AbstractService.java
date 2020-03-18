package com.github.skjolber.nfc.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.tech.MifareUltralight;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.command.ACRCommands;
import com.github.skjolber.nfc.hce.INFcTagBinder;
import com.github.skjolber.nfc.hce.resolve.TagProxyStore;
import com.github.skjolber.nfc.hce.tech.TagTechnology;
import com.github.skjolber.nfc.hce.tech.mifare.MifareClassicAdapter;
import com.github.skjolber.nfc.hce.tech.mifare.MifareClassicTagFactory;
import com.github.skjolber.nfc.hce.tech.mifare.IsdoDepAdapter;
import com.github.skjolber.nfc.hce.tech.mifare.MifareDesfireTagFactory;
import com.github.skjolber.nfc.hce.tech.mifare.MifareUltralightAdapter;
import com.github.skjolber.nfc.hce.tech.mifare.MifareUltralightTagFactory;
import com.github.skjolber.nfc.hce.tech.mifare.NdefAdapter;
import com.github.skjolber.nfc.hce.tech.mifare.NdefFormattableAdapter;
import com.github.skjolber.nfc.hce.tech.mifare.NfcAAdapter;
import com.github.skjolber.nfc.hce.tech.mifare.PN532NfcAAdapter;
import com.github.skjolber.nfc.NfcReader;
import com.github.skjolber.nfc.NfcService;
import com.github.skjolber.nfc.command.Utils;
import com.github.skjolber.nfc.messages.NfcReaderServiceListener;
import com.github.skjolber.nfc.service.desfire.DesfireReader;

import org.nfctools.NfcException;
import org.nfctools.api.ApduTag;
import org.nfctools.api.TagInfo;
import org.nfctools.api.TagType;
import org.nfctools.mf.MfConstants;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.classic.ClassicHandler;
import org.nfctools.mf.classic.MfClassicConstants;
import org.nfctools.mf.classic.MfClassicNdefOperations;
import org.nfctools.mf.classic.MfClassicReaderWriter;
import org.nfctools.mf.mad.Application;
import org.nfctools.mf.mad.ApplicationDirectory;
import org.nfctools.mf.ndef.AbstractNdefOperations;
import org.nfctools.mf.ul.CapabilityBlock;
import org.nfctools.mf.ul.LockPage;
import org.nfctools.mf.ul.MemoryLayout;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.Type2NdefOperations;
import org.nfctools.mf.ul.UltralightHandler;
import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.spi.acs.AcrMfClassicReaderWriter;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import custom.java.CommandAPDU;


public abstract class AbstractService extends Service {

    public static final String PREFERENCE_AUTO_READ_NDEF = "preference_auto_read_ndef";
    public static final String PREFERENCE_NTAG21X_ULTRALIGHT = "preference_ntag21x_ultralights";
    public static final String PREFERENCE_UID_MODE = "preference_uid_mode";

    private static final String TAG = AbstractService.class.getName();

    protected MifareUltralightTagFactory mifareUltralightTagFactory = new MifareUltralightTagFactory();
    protected MifareClassicTagFactory mifareClassicTagFactory = new MifareClassicTagFactory();
    protected MifareDesfireTagFactory mifareDesfireTagFactory = new MifareDesfireTagFactory();

    protected boolean readNDEF;
    protected boolean ntag21xUltralights;
    protected boolean uidMode;

    protected TagProxyStore store = new TagProxyStore();
    protected INFcTagBinder binder;
    protected boolean started = false;

    protected boolean readerOpen = false;
    protected boolean recieveStatusBroadcasts = false;

    protected NfcReaderServiceListener nfcReaderServiceListener;
    protected int nfcReaderStatusCode = -1;
    protected String nfcReaderStatusMessage = null;

    protected AbstractNdefOperations operations;

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (NfcService.ACTION_SERVICE_STATUS.equals(action)) {
                Log.d(TAG, "Broadcast that service is started");

                nfcReaderServiceListener.onServiceStarted();
            } else if (NfcReader.ACTION_READER_STATUS.equals(action)) {

                if (readerOpen) {
                    nfcReaderServiceListener.onReaderOpen(getReaderCommands(), NfcReader.READER_STATUS_OK);
                } else {
                    synchronized (AbstractService.this) {
                        nfcReaderServiceListener.onReaderClosed(nfcReaderStatusCode, nfcReaderStatusMessage);
                    }
                }

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        startReceivingStatusBroadcasts();

        this.binder = new INFcTagBinder(store); // new INFcTagBinder(store);

        refreshPreferences();
    }

    public void refreshPreferences() {
        readNDEF = isReadNDEF();
        ntag21xUltralights = isNTAG21x();
        uidMode = isUIDMode();
    }

    private boolean isReadNDEF() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AbstractService.this);

        return prefs.getBoolean(PREFERENCE_AUTO_READ_NDEF, false);
    }

    private boolean isNTAG21x() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AbstractService.this);

        return prefs.getBoolean(PREFERENCE_NTAG21X_ULTRALIGHT, false);
    }

    private boolean isUIDMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AbstractService.this);

        return prefs.getBoolean(PREFERENCE_UID_MODE, false);
    }

    protected int getVersion(MfBlock initBlock) {

        switch (initBlock.getData()[2]) {
            case 0x06: {
                if (!ntag21xUltralights) {
                    return -NfcNtagVersion.TYPE_NTAG210; // aka ultralight
                }
                return NfcNtagVersion.TYPE_NTAG210;
            }
            case 0x10: {
                return NfcNtagVersion.TYPE_NTAG212;
            }
            case 0x12: {
                if (!ntag21xUltralights) {
                    return -NfcNtagVersion.TYPE_NTAG213; // aka ultralight c
                }
                return NfcNtagVersion.TYPE_NTAG213;
            }
            case 0x3E: {
                return NfcNtagVersion.TYPE_NTAG215;
            }
            case 0x6D: {
                return NfcNtagVersion.TYPE_NTAG216;
            }
            case 0x6F: {
                return NfcNtagVersion.TYPE_NTAG216F;
            }
        }
        return 0;
    }

    protected void hce(int slotNumber, byte[] atr, IsoDepWrapper wrapper) {
        try {
            List<TagTechnology> technologies = new ArrayList<TagTechnology>();
            technologies.add(new NfcAAdapter(slotNumber, wrapper, true));
            technologies.add(new IsdoDepAdapter(slotNumber, wrapper, true));

            int serviceHandle = store.add(slotNumber, technologies);

            byte[] uid = ServiceUtil.getPcscUid(wrapper);
            if(uid != null) {
                Log.d(TAG, "Read tag UID " + Utils.toHexString(uid));
            }

            Intent intent = mifareDesfireTagFactory.getTag(serviceHandle, slotNumber, atr, null, uid, true, TechnologyType.getHistoricalBytes(atr), binder);

            sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading from tag", e);

            ServiceUtil.sendTechBroadcast(this);
        }
    }

    protected void desfire(int slotNumber, byte[] atr, IsoDepWrapper wrapper) {
        try {
            byte[] uid = ServiceUtil.getPcscUid(wrapper);
            if(uid != null) {
                Log.d(TAG, "Read tag UID " + Utils.toHexString(uid));
            }

            DesfireReader reader = new DesfireReader(null);

            List<TagTechnology> technologies = new ArrayList<TagTechnology>();
            technologies.add(new NfcAAdapter(slotNumber, wrapper, false));
            technologies.add(new IsdoDepAdapter(slotNumber, wrapper, false));

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = mifareDesfireTagFactory.getTag(serviceHandle, slotNumber, atr, null, uid, false, TechnologyType.getHistoricalBytes(atr), binder);

            Log.i(TAG, "Tag technologies " + technologies);

            sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading from tag", e);

            ServiceUtil.sendTechBroadcast(this);
        }
    }

    protected void mifareClassicPlus(int slotNumber, byte[] atr, TagType tagType, ApduTag acsTag, IsoDepWrapper wrapper) {
        boolean canReadBlocks;
        try {
            org.nfctools.mf.classic.MemoryLayout memoryLayout;

            int type = MifareClassicTagFactory.TYPE_CLASSIC;
            int size;

            switch (tagType) {
                case MIFARE_PLUS_SL1_2k:
                case MIFARE_PLUS_SL2_2k: {
                    memoryLayout = org.nfctools.mf.classic.MemoryLayout.CLASSIC_1K;
                    size = MifareClassicTagFactory.SIZE_1K;

                    break;
                }
                default: {
                    memoryLayout = org.nfctools.mf.classic.MemoryLayout.CLASSIC_4K;
                    size = MifareClassicTagFactory.SIZE_4K;
                }
            }

            MfClassicReaderWriter readerWriter = new AcrMfClassicReaderWriter(acsTag, memoryLayout);

            byte[] uid;
            try {
                uid = readerWriter.getTagInfo().getId();

                canReadBlocks = true;

                if (ServiceUtil.isBlank(uid)) {
                    Log.w(TAG, "Unable to read tag UID");
                    uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};
                } else {
                    Log.i(TAG, "Read tag id " + com.github.skjolber.nfc.command.Utils.toHexString(uid));
                }
            } catch (Exception e) {
                Log.w(TAG, "Problem reading tag UID", e);
                uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};

                canReadBlocks = false;
            }

            MfClassicNdefOperations operations = null;
            NdefMessage ndefMessage = null;

            if (canReadBlocks && readNDEF) {
                try {
                    operations = createMifareClassicNdefOperations(readerWriter);

                    if (operations.isFormatted()) {
                        byte[] bytes = operations.readNdefMessageBytes();
                        try {
                            ndefMessage = new NdefMessage(bytes);
                        } catch (Exception e) {
                            Log.w(TAG, "Problem parsing NDEF-message", e);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Problem constructing NDEF operations", e);
                }
            }

            int maxNdefSize;
            Boolean writable;

            if (operations != null) {
                if (operations.isFormatted()) {
                    maxNdefSize = operations.getMaxSize();
                } else {
                    maxNdefSize = -1;
                }
                writable = operations.isWritable();
            } else {
                maxNdefSize = -1;
                writable = null;
            }

            List<TagTechnology> technologies = new ArrayList<TagTechnology>();
            if (TechnologyType.isNFCA(atr)) {
                technologies.add(new NfcAAdapter(slotNumber, wrapper, false));
            }

            technologies.add(new MifareClassicAdapter(slotNumber, readerWriter));

            if (operations != null) {
                if (operations.isFormatted()) {
                    technologies.add(new NdefAdapter(slotNumber, operations));
                } else {
                    technologies.add(new NdefFormattableAdapter(slotNumber, operations));
                }
            }

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = mifareClassicTagFactory.getTag(serviceHandle, slotNumber, type, size, uid, maxNdefSize, ndefMessage, operations != null && operations.isFormatted(), writable, atr, binder);

            sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading from tag", e);

            ServiceUtil.sendTechBroadcast(this);
        }
    }

    protected void infineonMifare(int slotNumber, byte[] atr, TagType tagType, ApduTag acsTag, IsoDepWrapper wrapper) {

        try {
            boolean canReadBlocks;
            org.nfctools.mf.classic.MemoryLayout memoryLayout;

            int size;

            switch (tagType) {
                case INFINEON_MIFARE_SLE_1K: {
                    size = MifareClassicTagFactory.SIZE_1K;
                    memoryLayout = org.nfctools.mf.classic.MemoryLayout.CLASSIC_1K;

                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }

            MfClassicReaderWriter readerWriter = new AcrMfClassicReaderWriter(acsTag, memoryLayout);

            byte[] uid;
            try {
                uid = readerWriter.getTagInfo().getId();

                canReadBlocks = true;

                if (ServiceUtil.isBlank(uid)) {
                    Log.w(TAG, "Unable to read tag UID");
                    uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};
                } else {
                    Log.i(TAG, "Read tag id " + com.github.skjolber.nfc.command.Utils.toHexString(uid));
                }
            } catch (Exception e) {
                Log.w(TAG, "Problem reading tag UID", e);
                uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};

                canReadBlocks = false;
            }

            NdefMessage ndefMessage = null;

            MfClassicNdefOperations operations = null;

            if (canReadBlocks && readNDEF) {
                try {
                    operations = createMifareClassicNdefOperations(readerWriter);

                    if (operations.isFormatted()) {
                        byte[] bytes = operations.readNdefMessageBytes();
                        try {
                            ndefMessage = new NdefMessage(bytes);
                        } catch (Exception e) {
                            Log.w(TAG, "Problem parsing NDEF-message", e);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Problem constructing NDEF operations", e);
                }
            }

            int type = MifareClassicTagFactory.TYPE_CLASSIC;

            int maxNdefSize;
            Boolean writable;

            if (operations != null) {
                if (operations.isFormatted()) {
                    maxNdefSize = operations.getMaxSize();
                } else {
                    maxNdefSize = -1;
                }
                writable = operations.isWritable();
            } else {
                maxNdefSize = -1;
                writable = null;
            }

            List<TagTechnology> technologies = new ArrayList<TagTechnology>();
            if (TechnologyType.isNFCA(atr)) {
                technologies.add(new NfcAAdapter(slotNumber, wrapper, true));
            }

            technologies.add(new MifareClassicAdapter(slotNumber, readerWriter));

            if (operations != null) {
                if (operations.isFormatted()) {
                    technologies.add(new NdefAdapter(slotNumber, operations));
                } else {
                    technologies.add(new NdefFormattableAdapter(slotNumber, operations));
                }
            }

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = mifareClassicTagFactory.getTag(serviceHandle, slotNumber, type, size, uid, maxNdefSize, ndefMessage, operations != null && operations.isFormatted(), writable, atr, binder);

            sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading from tag", e);

            ServiceUtil.sendTechBroadcast(this);
        }
    }

    protected static MfClassicNdefOperations createMifareClassicNdefOperations(MfClassicReaderWriter readerWriter) {

        boolean formatted = false;
        boolean writable = false;

        TagInfo tagInfo = null;
        try {
            tagInfo = readerWriter.getTagInfo();
            if (readerWriter.hasApplicationDirectory()) {
                ApplicationDirectory applicationDirectory = readerWriter.getApplicationDirectory();
                if (applicationDirectory.hasApplication(MfConstants.NDEF_APP_ID)) {
                    formatted = true;
                    Application application = applicationDirectory.openApplication(MfConstants.NDEF_APP_ID);
                    writable = ClassicHandler.isFormattedWritable(application, MfClassicConstants.NDEF_KEY);
                } else {
                    throw new NfcException("Unknown tag contents");
                }
            } else {
                if (ClassicHandler.isBlank(readerWriter)) {
                    writable = true;
                } else {
                    throw new NfcException("Unknown tag contents");
                }
            }
        } catch (IOException e) {
            throw new NfcException(e);
        }
        return new MfClassicNdefOperations(readerWriter, tagInfo, formatted, writable);
    }

    protected Type2NdefOperations createMifareUltralightNdefOperations(MfBlock[] initBlocks, MfUlReaderWriter readerWriter, byte[] uid, int version) {
        MemoryLayout memoryLayout = null;
        boolean formatted = false;
        boolean writable = false;
        try {

            CapabilityBlock capabilityBlock = new CapabilityBlock(initBlocks[3].getData());
            if (UltralightHandler.isBlank(initBlocks)) {
                writable = true;

                memoryLayout = MemoryLayout.getUltralightMemoryLayout(version);
            } else if (UltralightHandler.isFormatted(initBlocks)) {
                formatted = true;

                memoryLayout = MemoryLayout.getUltralightMemoryLayout(version);

                writable = !capabilityBlock.isReadOnly() && !isLocked(readerWriter, memoryLayout);
            } else {
                throw new NfcException("Unknown tag contents");
            }
        } catch (Exception e) {
            throw new NfcException(e);
        }
        // Log.d(TAG, "Memory layout " + memoryLayout + " formatted:" + formatted + " writable:" + writable);

        return new Type2NdefOperations(memoryLayout, readerWriter, formatted, writable, uid);
    }

    private boolean isLocked(MfUlReaderWriter readerWriter, MemoryLayout memoryLayout) throws IOException, ReaderException {
        for (LockPage lockPage : memoryLayout.getLockPages()) {
            MfBlock[] block = readerWriter.readBlock(lockPage.getPage(), 1);
            for (int lockByte : lockPage.getLockBytes()) {
                if (block[0].getData()[lockByte] != 0)
                    return true;
            }
        }
        return false;
    }

    protected void mifareClassic(int slotNumber, byte[] atr, TagType tagType, IsoDepWrapper wrapper, ApduTag acsTag) {
        boolean canReadBlocks;

        try {

            org.nfctools.mf.classic.MemoryLayout memoryLayout;

            int size;

            switch (tagType) {
                case MIFARE_CLASSIC_1K: {
                    size = MifareClassicTagFactory.SIZE_1K;
                    memoryLayout = org.nfctools.mf.classic.MemoryLayout.CLASSIC_1K;

                    break;
                }
                default: {
                    size = MifareClassicTagFactory.SIZE_4K;
                    memoryLayout = org.nfctools.mf.classic.MemoryLayout.CLASSIC_4K;
                }
            }

            MfClassicReaderWriter readerWriter = new AcrMfClassicReaderWriter(acsTag, memoryLayout);

            byte[] uid;
            try {
                uid = readerWriter.getTagInfo().getId();

                canReadBlocks = true;

                if (ServiceUtil.isBlank(uid)) {
                    Log.w(TAG, "Unable to read tag UID");
                    uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};
                } else {
                    Log.i(TAG, "Read tag id " + Utils.toHexString(uid));
                }
            } catch (Exception e) {
                Log.w(TAG, "Problem reading tag UID", e);
                uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};

                canReadBlocks = false;
            }

            NdefMessage ndefMessage = null;

            MfClassicNdefOperations operations = null;

            if (canReadBlocks && readNDEF) {
                try {
                    operations = createMifareClassicNdefOperations(readerWriter);

                    if (operations.isFormatted()) {
                        byte[] bytes = operations.readNdefMessageBytes();
                        try {
                            ndefMessage = new NdefMessage(bytes);
                        } catch (Exception e) {
                            Log.w(TAG, "Problem parsing NDEF-message", e);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Problem constructing NDEF operations", e);
                }
            }

            int type = MifareClassicTagFactory.TYPE_CLASSIC;

            int maxNdefSize;
            Boolean writable;

            if (operations != null) {
                if (operations.isFormatted()) {
                    maxNdefSize = operations.getMaxSize();
                } else {
                    maxNdefSize = -1;
                }
                writable = operations.isWritable();
            } else {
                maxNdefSize = -1;
                writable = null;
            }

            List<TagTechnology> technologies = new ArrayList<TagTechnology>();
            if (TechnologyType.isNFCA(atr)) {
                technologies.add(new NfcAAdapter(slotNumber, wrapper, true));
            }

            technologies.add(new MifareClassicAdapter(slotNumber, readerWriter));

            if (operations != null) {
                if (operations.isFormatted()) {
                    technologies.add(new NdefAdapter(slotNumber, operations));
                } else {
                    technologies.add(new NdefFormattableAdapter(slotNumber, operations));
                }
            }

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = mifareClassicTagFactory.getTag(serviceHandle, slotNumber, type, size, uid, maxNdefSize, ndefMessage, operations != null && operations.isFormatted(), writable, atr, binder);

            sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading from tag", e);

            ServiceUtil.sendTechBroadcast(this);
        }
    }

    protected void mifareUltralight(int slotNumber, byte[] atr, TagType tagType, ApduTag acsTag, IsoDepWrapper wrapper, String readerName) {
        Type2NdefOperations operations = null;

        List<TagTechnology> technologies = new ArrayList<TagTechnology>();

        NdefMessage ndefMessage = null;

        Boolean canReadBlocks = null;
        try {
            // https://github.com/marshmellow42/proxmark3/commit/4745afb647c96a80f3f088f2afebf9686499680d

            MfUlReaderWriter readerWriter;

            Integer version = null;
            MfBlock[] initBlocks = null;
            if (ntag21xUltralights) {
                if (!(readerName.contains("1255") || readerName.contains("1252"))) {
                    // detect via get version
                    NfcNtagVersion ntagVersion = null;

                    NfcNtag ntag = new NfcNtag(wrapper);

                    try {
                        ntagVersion = new NfcNtagVersion(ntag.getVersion());
                        version = ntagVersion.getType();

                        //Log.d(TAG, "Detected version " + version);
                    } catch (MfException e) {
                        Log.d(TAG, "No version for Ultralight tag - non NTAG 21x-tag?");

                        ServiceUtil.sendTechBroadcast(this);

                        return;
                    }
                }
            }

            MfBlock[] capabilityBlock = null;
            if (version == null) {
                //Log.d(TAG, "Detect tag via capability container");

                readerWriter = new AcrMfUlReaderWriter(acsTag);

                // detect via capability container
                // can't really see difference between outdated 203 and 213 tag or ultralight and 210 tag

                try {
                    // capability block at index 3
                    capabilityBlock = readerWriter.readBlock(3, 1);

                    version = getVersion(capabilityBlock[0]);

                    //Log.d(TAG, "Detected version " + version);

                    canReadBlocks = true;
                } catch (Exception e) {
                    Log.w(TAG, "Problem reading tag UID", e);

                    canReadBlocks = false;
                }
            }

            // init reader finally
            if (version != null) {
                if (version > 0) {
                    //readerWriter = new AcrMfUlNTAGReaderWriter(acsTag, new NfcNtag(reader, slotNumber), version);
                    readerWriter = new AcrMfUlReaderWriter(acsTag);

                    tagType = TagType.MIFARE_ULTRALIGHT_C;
                } else {
                    readerWriter = new AcrMfUlReaderWriter(acsTag);
                }
            } else {
                readerWriter = new AcrMfUlReaderWriter(acsTag);
            }

            if (canReadBlocks == null || canReadBlocks) {
                try {
                    if (capabilityBlock == null) {
                        if (readNDEF) {
                            initBlocks = readerWriter.readBlock(0, 5);
                        } else {
                            initBlocks = readerWriter.readBlock(0, 4);
                        }
                    } else {
                        initBlocks = readerWriter.readBlock(0, 3);
                        if (readNDEF) {
                            MfBlock[] ndefBlocks = readerWriter.readBlock(4, 1);
                            initBlocks = new MfBlock[]{initBlocks[0], initBlocks[1], initBlocks[2], capabilityBlock[0], ndefBlocks[0]};
                        } else {
                            initBlocks = new MfBlock[]{initBlocks[0], initBlocks[1], initBlocks[2], capabilityBlock[0]};
                        }
                    }
                    canReadBlocks = true;
                } catch (Exception e) {
                    Log.w(TAG, "Problem reading tag UID", e);

                    canReadBlocks = false;
                }
            }

            // get uid from first two blocks:
            // 3 bytes from index 0
            // 4 bytes from index 1

            byte[] uid;
            if (canReadBlocks) {
                uid = new byte[7];
                System.arraycopy(initBlocks[0].getData(), 0, uid, 0, 3);
                System.arraycopy(initBlocks[1].getData(), 0, uid, 3, 4);
            } else {
                uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};
            }

            if (canReadBlocks && readNDEF && version != null) {
                try {
                    operations = createMifareUltralightNdefOperations(initBlocks, readerWriter, uid, version);

                    if (operations.isFormatted()) {
                        byte[] ndefBytes = operations.readNdefMessageBytes();
                        try {
                            ndefMessage = new NdefMessage(ndefBytes);
                        } catch (Exception e) {
                            Log.w(TAG, "Problem parsing NDEF-message", e);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Problem constructing NDEF operations", e);
                }
            }

            int type;
            int maxNdefSize;
            Boolean writable;

            if (operations != null) {
                type = operations.getMemoryLayout().getType();

                if (operations.isFormatted()) {
                    maxNdefSize = operations.getMaxSize();
                } else {
                    maxNdefSize = -1;
                }
                writable = operations.isWritable();
            } else {
                maxNdefSize = -1;
                type = MifareUltralight.TYPE_UNKNOWN;
                writable = null;
            }

            if (tagType == TagType.MIFARE_ULTRALIGHT_C || !canReadBlocks) {
                type = MifareUltralight.TYPE_ULTRALIGHT_C;
            }

            if (canReadBlocks) {
                technologies.add(new MifareUltralightAdapter(slotNumber, readerWriter));
            }

            if (TechnologyType.isNFCA(atr)) {
                technologies.add(new PN532NfcAAdapter(slotNumber, wrapper, false));
                //technologies.add(new NfcAAdapter(slotNumber, reader, false));
            }

            if (operations != null) {
                if (operations.isFormatted()) {
                    technologies.add(new NdefAdapter(slotNumber, operations));
                } else {
                    technologies.add(new NdefFormattableAdapter(slotNumber, operations));
                }
            }

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = mifareUltralightTagFactory.getTag(serviceHandle, slotNumber, type, version, uid, maxNdefSize, ndefMessage, operations != null && operations.isFormatted(), writable, atr, binder);

            sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading from tag", e);

            ServiceUtil.sendTechBroadcast(this);

        }
    }

    public abstract Object getReaderCommands();

    protected void startReceivingStatusBroadcasts() {
        synchronized (this) {
            if (!recieveStatusBroadcasts) {
                Log.d(TAG, "Start receiving status broadcasts");

                recieveStatusBroadcasts = true;

                // register receiver
                IntentFilter filter = new IntentFilter();
                filter.addAction(NfcService.ACTION_SERVICE_STATUS);
                filter.addAction(NfcReader.ACTION_READER_STATUS);

                registerReceiver(statusReceiver, filter);
            }
        }
    }

    protected void stopReceivingStatusBroadcasts() {
        synchronized (this) {
            if (recieveStatusBroadcasts) {
                Log.d(TAG, "Stop receiving status broadcasts");

                recieveStatusBroadcasts = false;

                try {
                    unregisterReceiver(statusReceiver);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bind for intent " + intent.getAction());

        return new Binder();
    }

    @Override
    public void onDestroy() {
        stopReceivingStatusBroadcasts();

        super.onDestroy();
    }
}
