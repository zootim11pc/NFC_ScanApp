package com.example.nfc_scanapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView host_ip, database_name, table_name, device_name, tagView;
	EditText host_text, database_text, table_text, device_text;

	// 統一字體大小
	float textSize = 40;
	byte[] id;

	// list of NFC technologies detected:
	private final String[][] techList = new String[][] { new String[] {
			NfcA.class.getName(), NfcB.class.getName(), NfcF.class.getName(),
			NfcV.class.getName(), IsoDep.class.getName(),
			MifareClassic.class.getName(), MifareUltralight.class.getName(),
			Ndef.class.getName() } };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		host_ip = (TextView) findViewById(R.id.host_ip);
		database_name = (TextView) findViewById(R.id.database_name);
		table_name = (TextView) findViewById(R.id.table_name);
		device_name = (TextView) findViewById(R.id.device_name);
		tagView = (TextView) findViewById(R.id.tagView);

		host_text = (EditText) findViewById(R.id.host_text);
		database_text = (EditText) findViewById(R.id.database_text);
		table_text = (EditText) findViewById(R.id.table_text);
		device_text = (EditText) findViewById(R.id.device_text);

		host_text.setTextSize(textSize);
		database_text.setTextSize(textSize);
		table_text.setTextSize(textSize);
		device_text.setTextSize(textSize);
		tagView.setTextSize(textSize);

		host_text.setText("120.114.138.168:8888/2013/12/01/index.php");

	}

	// handling ACTION_TAG_DISCOVERED action from intent:
	@Override
	protected void onResume() {
		super.onResume();
		// creating pending intent:
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// creating intent receiver for NFC events:
		IntentFilter filter = new IntentFilter();
		filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
		filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
		// enabling foreground dispatch for getting intent from NFC event:
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		nfcAdapter.enableForegroundDispatch(this, pendingIntent,
				new IntentFilter[] { filter }, this.techList);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// disabling foreground dispatch:
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		nfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

		if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {

			((TextView) findViewById(R.id.tagView))
					.setText("\nImformation \nDecimal: " + getReversed(id));

			new LoadingDataAsyncTask().execute();
		}
	}

	// Decimal
	private long getReversed(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = bytes.length - 1; i >= 0; --i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result;
	}

	public void postData() {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://"
				+ host_text.getText().toString());

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("database", database_text
					.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("table", table_text
					.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("device", device_text
					.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("uid", String
					.valueOf(getReversed(id))));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					HTTP.UTF_8));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

		} catch (IOException e) {

		}
	}

	class LoadingDataAsyncTask extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... param) {
			// getData();
			postData();
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			// showData();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

}
