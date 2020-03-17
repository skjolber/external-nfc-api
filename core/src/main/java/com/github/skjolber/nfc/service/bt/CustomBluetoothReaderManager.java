package com.github.skjolber.nfc.service.bt;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.BluetoothReaderManager;
import com.acs.bluetooth.a;

public class CustomBluetoothReaderManager extends BluetoothReaderManager {

    public static final int MTU_SIZE = 138;

    public CustomBluetoothReaderManager() {
        this.c.clear();

        this.c.add(new CustomAcr1255uj1Reader());
        this.c.add(new CustomA());

    }
}
