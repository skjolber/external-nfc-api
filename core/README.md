# core

## Usage

### USB background service
Launch the service __BackgroundUsbService__. If started without a connected reader, the service remains alive and looks for a reader. If there is no USB permission granted already, the app will ask for such a permission whenever a reader is connected. 

For some Android devices the reader permission is not saved via the `request usb permission` operation in the service. To stop the 'permission box' from reappearing, add an activity which triggers on the right filter

```
    <intent-filter>
        <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
    </intent-filter>

    <meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" android:resource="@xml/accessory_filter" />

with accessory_filter

    <resources>
        <usb-device vendor-id="1839" />
    </resources>
```

where 1839 is ACS.

### Bluetooth background service
Launch the service __BluetoothBackgroundService__.

## Configuration options
The service has some configuration options, loaded from shared preferences. These are listed in the top of the service class.

## Bluetooth
There is a basic implementation of bluetooth reader support

Note on bluetooth: See the docs folder for info how to reset the devices. The tool can also be using via a Windows virtual machine.


