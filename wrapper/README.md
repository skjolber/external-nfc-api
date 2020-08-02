# wrapper

> NOTE: This submodule is retired in favour of [android-nfc-wrapper](https://github.com/skjolber/android-nfc-wrapper).

Wrapper for NFC types from the Android source code.

As of Android 9, using hidden Android classes is not allowed. Theres is a 'grey-list' of legal classes which can be used (on your own risk), but the android.nfc are not sufficiently represented.

In a nutshell, we're reimplementing + adding wrappers to the android NFC classes, so that a single package can be used both for internal and external NFC objects.
