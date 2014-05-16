External NFC Service API (native style) for Android BETA
==================================

This API is for interacting with the [External NFC Service](https://play.google.com/store/apps/details?id=com.skjolberg.nfc.external) app found in Google Play. 

Background
========
The [External NFC Service](https://play.google.com/store/apps/details?id=com.skjolberg.nfc.external) app offers NFC functionality similar to the built-in Android NFC API - for external NFC readers:
 - External NFC reader management and interaction
 - NFC Tag access <b>using Andriod built-in (native) types </b> (Ndef, MifareUltralight, IsoDep, etc). 

This very much simplifies implementation in use-cases where external NFC readers is required.

Overview
=================
This repository contains source code for 
 * An Android library project (the actual API), and 
 * a demo client app demonstrating actual usage

API
===
The API defines 
 * broadcast actions
  * service start / stop and state
  * reader open / close and status
  * tag connect / disconnect
 * 'extras' objects for interaction with readers
 * abstract activities for interaction with built-in and external NFC (in parallell)
  * these currently depend on the [NDEF Tools for Android](https://code.google.com/p/ndef-tools-for-android/) project.

Note that tag interaction is performed via the native NFC classes and so these are not included.

Demo client
==============
The demo client keeps track of the service, reader and tag states. 

Supported readers
=================
Currently the ACS readers
 * [ACR 122U](http://www.acs.com.hk/index.php?pid=product&id=ACR122U) 
 * [ACR 1222L](http://www.acs.com.hk/index.php?pid=product&id=ACR1222L)
 * [ACR 1251U](http://www.acs.com.hk/en/products/218/acr1251-usb-nfc-reader-ii/)
 
are supported and must be connected to your Android device via an On-The-Go (OTG) USB cable.

Supported tag technology
========================
Mifare Ultralight and Mifare Classic (including NTAG203) tags are supported. I recommend Mifare Ultralights / NTAG203. Desfire EV1 tags are supported but without NDEF support. Host card emulation targets are also supported.

Please note:
 - ACR 122U the Mifare Classic support is experimental.
 - ACR 1251U support is in beta.

Troubleshooting
===========
Note that not all Android devices actually have an USB hub.

Does the ACR reader not light up when connected to your device, even after the service asks for USB permissions? The ACR reader shuts down if there is not enough battery, so try charging your battery more. 

Please report any issues to skjolber@gmail.com.

Related apps
============
You might be interested in
 * [ACR 1222L USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr1222) 
 * [ACR 122 USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr122u)
 * [ACR 1251 USB NFC Reader Utils](https://play.google.com/store/apps/details?id=com.skjolberg.acr1251u)

for configuration of your reader. Approximately the same configuration options are available using this API. 

Need help?
===========
If you need professional assistance with an NFC project, get in touch. I also do

 * Host Card Emulation for Android
 * Desfire EV1 tags (with encryption)
 * Smart card related workflows and integrations

Check me out and connect with me on [LinkedIn](http://lnkd.in/r7PWDz).
