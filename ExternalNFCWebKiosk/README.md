# NFC Web Kiosk API

The following javascript methods are called by the application:

    function nfcServiceStarted(){
	}

	function nfcServiceStopped(){
    }

	function nfcReaderOpen(){
	}

	function nfcReaderClosed(){
	}

	function nfcTagPresent(uid){
    }

	function nfcTagLost(){
	}

The methods `nfcTagPresent(..)` and `nfcTagLost()` work both for external and built-in NFC.

See the embedded [index.html] file for a simple, working example.
[index.html]:https://github.com/skjolber/external-nfc-api/tree/develop/ExternalNFCWebKiosk/res/raw/index.html

