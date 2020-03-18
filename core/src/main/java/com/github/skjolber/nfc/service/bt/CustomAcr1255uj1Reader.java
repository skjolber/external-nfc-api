package com.github.skjolber.nfc.service.bt;

import android.bluetooth.BluetoothGattDescriptor;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.b;

import java.nio.ByteBuffer;

public class CustomAcr1255uj1Reader extends Acr1255uj1Reader {

    public void d() {
        if (this.D != null) {
            (new StringBuilder("mBtQueue size:")).append(this.mBtQueue.size());
            com.acs.bluetooth.b var1;
            if ((var1 = (com.acs.bluetooth.b)this.mBtQueue.peek()) != null) {
                switch(var1.e()) {
                    case 0:
                        synchronized(this) {
                            if (!this.mReaderBusy) {
                                this.mReaderBusy = true;
                                if (var1.a() != null && !this.D.readCharacteristic(var1.a())) {
                                    if (this.L != null) {
                                        this.L.onDeviceInfoAvailable(this.E, this.a(var1.a().getUuid()), "", 1004);
                                    }

                                    this.c();
                                }
                            }
                            break;
                        }
                    case 1:
                        synchronized(this) {
                            if (!this.mReaderBusy) {
                                this.mReaderBusy = true;
                                if (var1.a() != null) {
                                    if (!this.D.setCharacteristicNotification(var1.a(), var1.d())) {
                                        if (this.M != null) {
                                            this.M.onEnableNotificationComplete(this.E, 1003);
                                        }

                                        this.c();
                                    } else {
                                        BluetoothGattDescriptor var14;
                                        if ((var14 = var1.a().getDescriptor(b)) == null) {
                                            if (this.M != null) {
                                                this.M.onEnableNotificationComplete(this.E, 1000);
                                            }

                                            this.c();
                                        }

                                        if (var1.d()) {
                                            var14.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        } else {
                                            var14.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                                        }

                                        this.D.writeDescriptor(var14);
                                    }
                                }
                            }
                            break;
                        }
                    case 2:
                        synchronized(this) {
                            if (!this.mReaderBusy) {
                                this.mReaderBusy = true;
                            }
                        }

                        ByteBuffer var10;
                        if ((var10 = var1.b()) == null || var10.remaining() <= 0 || this.D == null || this.u == null) {
                            return;
                        }

                        // https://punchthrough.com/maximizing-ble-throughput-part-2-use-larger-att-mtu-2/
                        int var10000 = var10.remaining() > CustomBluetoothReaderManager.MTU_SIZE ? CustomBluetoothReaderManager.MTU_SIZE : var10.remaining();
                        int var9 = var10000;
                        byte[] var8 = new byte[var10000];
                        var10.get(var8, 0, var9);
                        this.u.setValue(var8);
                        this.D.writeCharacteristic(this.u);
                }

            }
        }
    }
}
