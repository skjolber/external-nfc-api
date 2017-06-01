# External NFC Service API (native style) for Android

This API is for interaction with the [External NFC Service](https://play.google.com/store/apps/details?id=com.skjolberg.nfc.external) app found in Google Play. 

The [External NFC Service](https://play.google.com/store/apps/details?id=com.skjolberg.nfc.external) app provides NFC-functionality along the lines of native Android NFC for external NFC readers connected via USB. 

Projects using this library will benefit from:
 - External NFC reader management and interaction
 - Parallell use of external and/or internal NFC (i.e. in the same activity, both enabled at the same time)
 - Support for both tags and Android devices (Host Card Emulation), simultaneously
 - Use of __using Andriod built-in (native) types__ ([Ndef], [MifareUltralight], [IsoDep], etc). 

As this project very much simplifies implementation for use-cases requiring external NFC readers, it saves a lot of development time (2-8 weeks depending on use-case and previous knowledge).

# Overview
This repository contains source code for 

 * An Android library project (the actual API), and 
 * Demo client apps demonstrating actual usage
  * [Basic client app](externalNFCClient)
  * [NXP API client](externalNFCNxpClient) for [MIFARE SDK](http://www.mifare.net/en/products/mifare-sdk/)
  * [Web Kiosk client](externalNFCWebKiosk) with javascript bindings

There is also a [Host Card Emulation client app](externalNFCHostCardEmulationClient) for use with the [Basic client app](externalNFCClient).

# External NFC reader API
The API defines 
 * broadcast actions
   * service start / stop and status
   * reader open / close and status
   * tag connect / disconnect
 * 'extras' objects for interaction with readers
   * disable beeps
   * display text
   * configure NFC tech types (PICC)
   * enable/disable LEDs
   * run custom commands
   * and more.. 
 * abstract activities for interaction with built-in and external NFC (simultaneously)
  * these currently depend on the [NDEF Tools for Android](https://github.com/skjolber/ndef-tools-for-android) project.
 * Programmatically start and stop the service (see methods startService() and stopService() in the [NfcExternalDetectorActivity](externalNFCAPI/src/main/java/com/skjolberg/nfc/util/activity/NfcExternalDetectorActivity.java) class in for an example).

Note that tag interaction is performed via the native NFC classes and so these are not included in the API itself. These native NFC classes are present even on non-NFC devices.

# Supported readers
Currently the ACS readers

 * [ACR 122U](http://www.acs.com.hk/index.php?pid=product&id=ACR122U) ([API](externalNFCAPI/src/main/java/com/skjolberg/nfc/acs/Acr122UReader.java)) 
 * [ACR 1222L](http://www.acs.com.hk/index.php?pid=product&id=ACR1222L) ([API](externalNFCAPI/src/main/java/com/skjolberg/nfc/acs/Acr1222LReader.java)) 
 * [ACR 1251U](http://www.acs.com.hk/en/products/218/acr1251-usb-nfc-reader-ii/) ([API](externalNFCAPI/src/main/java/com/skjolberg/nfc/acs/Acr1251UReader.java)) 
 * [ACR 1252U](http://www.acs.com.hk/en/products/342/acr1252u-usb-nfc-reader-iii-nfc-forum-certified-reader/) ([API](externalNFCAPI/src/main/java/com/skjolberg/nfc/acs/Acr1252UReader.java)) 
 * [ACR 1255U-J1](http://www.acs.com.hk/en/products/403/acr1255u-j1-secure-bluetooth%C2%AE-nfc-reader/) ([API](externalNFCAPI/src/main/java/com/skjolberg/nfc/acs/Acr1255UReader.java)) - NOTE: No bluetooth support yet.
 * [ACR 1281U-C1](http://www.acs.com.hk/en/products/159/acr1281u-c1-dualboost-ii-usb-dual-interface-reader/) ([API](externalNFCAPI/src/main/java/src/com/skjolberg/nfc/acs/Acr1281UReader.java)) 
 * [ACR 1283L](http://www.acs.com.hk/en/products/226/acr1283l-standalone-contactless-reader/) ([API](externalNFCAPI/src/main/java/com/skjolberg/nfc/acs/Acr1283LReader.java)) 
 
are supported and must be connected to your Android device via an On-The-Go (OTG) USB cable. 

Additional ACR readers might work depending on their command set, however custom reader commands will (like LED, beep etc) will not be available.

# Supported tag technology
The following tags are supported by the service
  * Mifare Ultralight familiy
    * Mifare Ultralight
    * NTAG 21x with FAST READ
  * Mifare Classic and friends
    * Not recommended due to security and compatibility issues
  * Desfire EV1 tags
  * [Host Card Emulation](http://developer.android.com/guide/topics/connectivity/nfc/hce.html) - interaction with Android devices.
  
The readers can for the __most part can be enabled for all tag types at the same time__, including Host Card Emulation.

Please note:
 - Some readers only support a subset of the above tags
 - For ACR 122U the Mifare Classic does not work well.
 - No built-in NDEF support for Desfire EV1 cards (let me know it this is interesting to you).

Configuration options
 - assume all NTAG21x Mifare Ultralight targets. This improves read speed, particullary for the tags which have legacy equivalents, like NTAG 210 and 213
 - read only tag UIDs, ignore other tag data. This improves read speed.
 - read NDEF data automatically
 - read UID for Desfire EV1 targets automatically 

# Troubleshooting
Please report any issues to thomas.skjolberg@gmail.com.

### Reader connection
Note that not all Android devices actually have an USB hub, in which case no USB devices work.

Does the ACR reader not light up when connected to your device, even after the service asks for USB permissions? The ACR reader shuts down if there is not enough battery, so try charging your battery more, or connect external power.

If you are using external power, be aware that the connection order (device, reader, power) might be important. Known symptom: 
 - Seeing an USB permissions window that disappears rather quickly.

### Tag detection
There is quite a few types of tags out there, and if your tag type is not recognized, please let me know. If the tag does not register at all, make sure that auto polling is configured, and that the right protocols are enabled. __Use the below utility apps for tweaking your reader settings__.

# Reader setting utility apps
You might be interested in

 * [ACR 1222L USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr1222) 
 * [ACR 122 USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr122u)
 * [ACR 1251 USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr1251u)
 * [ACR 1252 USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr1252u)
 * [ACR 1281 USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr1281u)

for configuration of your reader. Approximately the same configuration options are available using this API. 

# Feature requests
Please email feature requests to thomas.skjolberg@gmail.com.

# Distribution
Get in touch for bulk and/or offline distribution. A library jar is also available on request, simplifying distribution of your own app(s). Source code is also an option.

# Development
If you need professional, cost-efficient assistance with an NFC project, get in touch. I also do

 * Desfire EV1 tech (with encryption) - [example app](https://play.google.com/store/apps/details?id=com.skjolberg.mifare.desfiretool)
 * WebView-based apps, either visiting ULRs and/or interaction over Javascript.
 * NFC-initiated [wifi connectivity](https://play.google.com/store/apps/details?id=w.i)
 * More advanced Host Card Emulation (HCE) for Android
 * NFC development tools
   * [NFC Developer](https://play.google.com/store/apps/details?id=com.antares.nfc)
   * [NDEF Tools for Android](https://play.google.com/store/apps/details?id=org.ndeftools.boilerplate&hl=no)
   * [Mifare Classic refactor](https://play.google.com/store/apps/details?id=com.skjolberg.nfc.mifareclassic)
 * NFC supplimented with QR codes
 * Smartcard-related workflows and integrations over [ESB](http://camel.apache.org/) or [BPM](https://camunda.com/) modelling
 * Custom binary formats for NDEF or raw tag data - fit your data for __minimal read/write time and best form factor selection__
 * [Apache Cordova] plugins for Android

Feel free to connect with me on [LinkedIn](http://lnkd.in/r7PWDz), see also my [Github page](https://skjolber.github.io).

[Ndef]:                 https://developer.android.com/reference/android/nfc/tech/Ndef.html
[MifareUltralight]:     https://developer.android.com/reference/android/nfc/tech/MifareUltralight.html
[IsoDep]:               https://developer.android.com/reference/android/nfc/tech/IsoDep.html
[Apache Cordova]:       https://cordova.apache.org/
