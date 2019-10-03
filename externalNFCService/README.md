# External NFC Service example
Gradle / Android-studio based project. 

## Building
Import projects ExternalNFCCore, NFCToolsForJavaAndroid, ExternalNFCAPI and ndeftools into your workspace.

Then add all projects except ndeftools as library projects to your application.

## Usage
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

## Configuration options
The service has some configuration options, loaded from shared preferences. These are listed in the top of the service class.

## Bluetooth
There is a partial implementation of bluetooth reader support, this is currently not working acceptable because the devices are put in a bad state when setting the 'sleep mode'. The devices must then be reset using a obscure tool which restores its state - see the docs folder. Note: The tool can also be using via a Windows virtual machine.


