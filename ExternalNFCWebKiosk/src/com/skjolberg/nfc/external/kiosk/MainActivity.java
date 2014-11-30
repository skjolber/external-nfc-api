package com.skjolberg.nfc.external.kiosk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.skjolberg.nfc.util.activity.NfcExternalDetectorActivity;


public class MainActivity extends NfcExternalDetectorActivity {

	private static final String TAG = MainActivity.class.getName();
	
	protected WebView webView;
	protected Boolean service = null;

	class MyWebChromeClient extends WebChromeClient
	{
	    @Override
	    public boolean onConsoleMessage(ConsoleMessage cm)
	    {
	        Log.d(TAG, "Console: " + String.format("%s @ %d: %s", 
	                    cm.message(), cm.lineNumber(), cm.sourceId()));
	        return true;
	    }
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webView = (WebView)this.findViewById(R.id.myWebView);
        webView.setWebViewClient(new WebViewClient() {

        	   public void onPageFinished(WebView view, String url) {
        		   if(!url.startsWith("javascript:")) {
        			   initializeExternalNfc();
        		   }
        	    }
        	});        
        webView.setWebChromeClient(new MyWebChromeClient());
        
		WebSettings webSettings = webView.getSettings();
		//
		webSettings.setDatabaseEnabled(true);
		webSettings.setGeolocationEnabled(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportMultipleWindows(false);

      	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       	
    	String url = prefs.getString(PreferencesActivity.PREFERENCE_URL, null);
        if(url == null || url.length() == 0) {
        	Log.d(TAG, "No URL, show preferences");
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
        } else {
        	Log.d(TAG, "Start from URL " + url);
        	webView.loadUrl(url);
        }
        
        setDetecting(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if(id == R.id.action_start_service) {
	        Intent intent = new Intent();
			intent.setClassName("com.skjolberg.nfc.external", "com.skjolberg.service.BackgroundUsbService");
	        startService(intent);
		} else if(id == R.id.action_stop_service) {
	        Intent intent = new Intent();
			intent.setClassName("com.skjolberg.nfc.external", "com.skjolberg.service.BackgroundUsbService");
	        stopService(intent);
		} else if(id == R.id.action_preferences) {
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		for(int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			if(item.getItemId() == R.id.action_start_service) {
				item.setVisible(service == null || !service);
			} else if(item.getItemId() == R.id.action_stop_service) {
				item.setVisible(service != null && service);
			}
		}
		
		return super.onPrepareOptionsMenu(menu);
	}    
    
	@Override
	protected void onExternalNfcServiceStopped(Intent intent) {
		setServiceStarted(false);

		runOnUiThread(new Runnable() {
			public void run() {
				call("javascript:nfcServiceStopped()");
			}
		});
	}

	@Override
	protected void onExternalNfcServiceStarted(Intent intent) {
		setServiceStarted(true);
		
		runOnUiThread(new Runnable() {
			public void run() {
				call("javascript:nfcServiceStarted()");
			}
		});
	}
	
	public void setServiceStarted(final boolean started) {
		this.service = started;
		
		invalidateOptionsMenu();
	}	

	@Override
	protected void onExternalNfcReaderOpened(Intent intent) {
		runOnUiThread(new Runnable() {
			public void run() {
				call("javascript:nfcReaderOpen()");
			}
		});
	}

	@Override
	protected void onExternalNfcReaderClosed(Intent intent) {
		runOnUiThread(new Runnable() {
			public void run() {
				call("javascript:nfcReaderClosed()");
			}
		});
	}

	protected void onExternalNfcTagLost(Intent intent) {
		// default to same as native NFC
		onNfcTagLost(intent);
	}
	
	protected void onExternalNfcIntentDetected(Intent intent, String action) {
		// default to same as native NFC
		onNfcIntentDetected(intent, action);
	}

	 /**
     * 
     * NFC feature was found and is currently enabled
     * 
     */
	
	@Override
	protected void onNfcStateEnabled() {
		toast(getString(R.string.nfcAvailableEnabled));
	}

    /**
     * 
     * NFC feature was found but is currently disabled
     * 
     */
	
	@Override
	protected void onNfcStateDisabled() {
		toast(getString(R.string.nfcAvailableDisabled));
	}

	/**
     * 
     * NFC setting changed since last check. For example, the user enabled NFC in the wireless settings.
     * 
     */
	
	@Override
	protected void onNfcStateChange(boolean enabled) {
		if(enabled) {
			toast(getString(R.string.nfcAvailableEnabled));
		} else {
			toast(getString(R.string.nfcAvailableDisabled));
		}
	}

	/**
	 * 
	 * This device does not have NFC hardware
	 * 
	 */
	
	@Override
	protected void onNfcFeatureNotFound() {
		toast(getString(R.string.noNfcMessage));
	}

	@Override
	protected void onNfcIntentDetected(Intent intent, String action) {
		if(intent.hasExtra(NfcAdapter.EXTRA_ID)) {
			byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
			Log.d(TAG, "Tag id " + toHexString(id));
			
			setTagId(id);
		} else {
			Log.d(TAG, "No tag id");
			
			setTagId(null);
		}
	}

	private void setTagId(final byte[] uid) {
		runOnUiThread(new Runnable() {
			public void run() {
				
				if(uid != null) {
					call("javascript:nfcTagPresent('" + toHexString(uid) + "')");
				} else {
					call("javascript:nfcTagPresent(null)");
				}
			}
		});
	}

	@Override
	protected void onNfcTagLost(Intent intent) {
		runOnUiThread(new Runnable() {
			public void run() {
				call("javascript:nfcTagLost()");
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if(webView.getUrl() == null) {
	      	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	       	
	    	String url = prefs.getString(PreferencesActivity.PREFERENCE_URL, null);
	        if(url != null && url.length() != 0) {
	        	Log.d(TAG, "Resume from URL " + url);
	        	webView.loadUrl(url);
	        } else {
	        	try {
					webView.loadData(getStringResource(R.raw.index), "text/html", "utf-8");
				} catch (IOException e) {
					throw new RuntimeException();
				}
	        }
		}
	}
	
	private void call(final String url) {
		Log.d(TAG, url);
		
		 webView.post(new Runnable() {
            @Override
            public void run() { 
        		webView.loadUrl(url);
            }
        }); 
		 
	}
	
	@Override
	public void onBackPressed()
	{
	    if(webView.canGoBack()) {
	        webView.goBack();
	    } else {
	        super.onBackPressed();
	    }
	}
	
	public void toast(int id) {
		toast(getString(id));
	}
	
	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}	

	/**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
		return toHexString(buffer, 0, buffer.length);
    }
 
	
	/**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer, int offset, int length) {
		StringBuilder sb = new StringBuilder();
		for(int i = offset; i < offset + length; i++) {
			byte b = buffer[i];
			sb.append(String.format("%02x", b&0xff));
		}
		return sb.toString().toUpperCase();
    }
    
 // reads resources regardless of their size
    public byte[] getResource(int id, Context context) throws IOException {
        Resources resources = context.getResources();
        InputStream is = resources.openRawResource(id);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        byte[] readBuffer = new byte[4 * 1024];

        try {
            int read;
            do {
                read = is.read(readBuffer, 0, readBuffer.length);
                if(read == -1) {
                    break;
                }
                bout.write(readBuffer, 0, read);
            } while(true);

            return bout.toByteArray();
        } finally {
            is.close();
        }
    }

        // reads a string resource
    public String getStringResource(int id, Charset encoding) throws IOException {
        return new String(getResource(id, this), encoding);
    }

        // reads an UTF-8 string resource
    public String getStringResource(int id) throws IOException {
        return new String(getResource(id, this), Charset.forName("UTF-8"));
    }
    
}
