package com.mobileproto.lab3;

import android.app.Activity;

/**
 * Created by mingram on 9/22/13.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for reading data from an NDEF Tag.
 *
 */
public class NFCActivity extends Activity {
    public static final String TAG = "Lab 3";
    public static final String DEFAULT_TEXT_MESSAGE = "Hello, NFC World!";
    public static final String DEFAULT_AAR_TEXT_MESSAGE = "AAR detected!";
    public static final String DEFAULT_URL = "http://developer.samsung.com";
    public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";
    private String nfc_text = "";
    private String nfc_url = "";
    private static Tag mTag;
    private NdefRecord mNdefAARRecord = NdefRecord.createApplicationRecord("com.mobileproto.lab3");
    private NdefMessage mDefaultNdefURIMesssage = new NdefMessage(new NdefRecord[]{NdefRecord.createUri(DEFAULT_URL)});
    private NdefMessage mDefaultNdefTextMesssage = new NdefMessage(new NdefRecord[]{new NdefRecord(
            NdefRecord.TNF_MIME_MEDIA, TEXT_PLAIN_MIME_TYPE.getBytes(), new byte[0], DEFAULT_TEXT_MESSAGE.getBytes())});
    private NdefMessage mAARTextMesssage = new NdefMessage(new NdefRecord[]{
            new NdefRecord(NdefRecord.TNF_MIME_MEDIA, TEXT_PLAIN_MIME_TYPE.getBytes(), new byte[0],
                    DEFAULT_AAR_TEXT_MESSAGE.getBytes()), mNdefAARRecord});
    private final Object syncObject = new Object();

    private enum WorkMode {
        MODE_READ, MODE_WRITE
    }
    WorkMode mMode = WorkMode.MODE_READ;

    NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mNdefFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate start");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        setContentView(R.layout.activity_nfc);

        Button writeTextButton = (Button) findViewById(R.id.write_text_button);
        writeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "MainActivity.onCreate - writeTextButton clicked");
                AlertDialog.Builder alert = new AlertDialog.Builder(NFCActivity.this);
                alert.setTitle("Set Text");
                final EditText input = new EditText(NFCActivity.this);
                input.setText(nfc_text);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        nfc_text = value;
                        if (mTag != null) {
                            NdefMessage mDefaultNdefTextMesssage = new NdefMessage(new NdefRecord[]{new NdefRecord(
                                    NdefRecord.TNF_MIME_MEDIA, TEXT_PLAIN_MIME_TYPE.getBytes(), new byte[0], value.getBytes())});
                            writeTagNewThread(mDefaultNdefTextMesssage, mTag);
                        // Do something with value!
                        } else {
                            logAndToast("Text write failed - tag not detected");
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        nfc_text = input.getText().toString();;
                        // Canceled.
                    }
                });
                alert.show();

            }
        });

        Button writeURLButton = (Button) findViewById(R.id.write_url_button);
        writeURLButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "MainActivity.onCreate - writeURLButton clicked.");
                AlertDialog.Builder alert = new AlertDialog.Builder(NFCActivity.this);
                alert.setTitle("Set URL");
                final EditText input = new EditText(NFCActivity.this);
                input.setText(nfc_url);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (value.substring(0,7).equals("http://") || value.substring(0,8).equals("https://")){
                            nfc_url = value;
                        } else {
                            nfc_url = "http://" + value;
                            Toast.makeText(NFCActivity.this.getApplicationContext(), nfc_url, Toast.LENGTH_SHORT).show();
                        }
                        if (mTag != null) {
                            NdefMessage mNdefURIMesssage = new NdefMessage(new NdefRecord[]{NdefRecord.createUri(nfc_url)});
                            writeTagNewThread(mNdefURIMesssage, mTag);
                            // Do something with value!
                        } else {
                            logAndToast("Text write failed - tag not detected");
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (value.substring(0,7).equals("http://") || value.substring(0,8).equals("https://")){
                            nfc_url = value;
                        } else {
                            nfc_url = "http://" + value;
                            Toast.makeText(NFCActivity.this.getApplicationContext(), nfc_url, Toast.LENGTH_SHORT).show();
                        }
                        // Canceled.
                    }
                });
                alert.show();
            }
        });

        Button writeAARButton = (Button) findViewById(R.id.write_aar_button);
        writeAARButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "MainActivity.onCreate - writeAARButton clicked.");
                if (mTag != null) {
                    writeTagNewThread(mAARTextMesssage, mTag);
                } else {
                    logAndToast("AAR write failed - tag not detected");
                }
            }
        });

        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefFilter.addDataType(TEXT_PLAIN_MIME_TYPE);
        } catch (MalformedMimeTypeException e) {
            e.printStackTrace();
            Log.e(TAG, "Error - MalformedMimeTypeException");
        }

        mNdefFilters = new IntentFilter[]{ndefFilter};

        mMode = WorkMode.MODE_READ;
    }

    private void logDetectedTechs(Tag pTag) {
        if (mTag != null) {
            for (String s : mTag.getTechList()) {
                Log.d(TAG, "Detected tech: " + s);
            }
        }
    }

    private void logTagInfo(Ndef pNdef) {
        if (pNdef != null) {
            int size = pNdef.getMaxSize(); // tag size
            boolean writable = pNdef.isWritable(); // is tag writable?
            String type = pNdef.getType(); // tag type
            Log.d(TAG, "Tag size: " + size + " type: " + type + " is writable?: " + writable);
            Log.d(TAG, "Tag can be made readonly: " + pNdef.canMakeReadOnly() + " is connected: " + pNdef.isConnected());
        }
    }

    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "MainActivity onNewIntent, action: " + intent.getAction());
        setIntent(intent);
        if (isNFCIntent(intent)) {
            mMode = WorkMode.MODE_READ;
        }
    }

    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
        Log.i(TAG, "MainActivity onPause");
    }

    private boolean isNFCIntent(Intent pIntent) {
        String action = pIntent.getAction();
        return NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action);
    }

    private void handleNFCTag(Intent pNfcIntent) {
        if (!isNFCIntent(pNfcIntent)) {
            Log.w(TAG, "Non-NDEF action in intent - returning");
            return;
        } else {
            if (mMode == WorkMode.MODE_READ) {
                mTag = pNfcIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            } else {
                Tag newTag = pNfcIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                Log.i(TAG, "MainActivity newTag is null?: " + (newTag == null));

                if (newTag != null) {
                    mTag = newTag;
                }
            }
            logDetectedTechs(mTag);

            if (mTag != null && mMode == WorkMode.MODE_READ) {
                mMode = WorkMode.MODE_WRITE;
                Ndef ndefTag = Ndef.get(mTag);

                if (ndefTag != null) {
                    logTagInfo(ndefTag);
                    NdefMessage ndefMesg = ndefTag.getCachedNdefMessage();
                    if (ndefMesg != null) {
                        displayMessages(ndefMesg.getRecords());
                    } else {
                        Log.w(TAG, "No cached NDEF message");
                    }
                } else {
                    Log.w(TAG, "ndefTag is null");
                }

            }
        }
    }

    public void onResume() {
        super.onResume();
        // mDialog.hide();
        Intent nfcIntent = getIntent();
        Log.i(TAG, "MainActivity onResume, action: " + nfcIntent.getAction());
        setIntent(new Intent()); // consume Intent
        handleNFCTag(nfcIntent);

        String[][] techList = {new String[]{NfcA.class.getName()}, new String[]{MifareClassic.class.getName()}};
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefFilters, techList);
    }

    private void displayMessages(NdefRecord[] ndefRecords) {
        String[] rets = new String[ndefRecords.length];
        int i = 0;

        for (NdefRecord ndr : ndefRecords) {
            rets[i++] = parsePayload(ndr);
        }
        if (rets.length > 0) {
            updateTextField(rets[0]);
        }
    }

    private void updateTextField(final String text) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.nfc_text);
                tv.setText(text);
            }
        });
    }

    private String parseWellKnownPayload(NdefRecord pRecord) throws UnsupportedEncodingException {
        Log.d(TAG, "parseWellKnownPayload start");
        String ret = "";
        byte[] type = pRecord.getType();
        byte[] payload = pRecord.getPayload();

        if (Arrays.equals(type, NdefRecord.RTD_TEXT)) {
            Log.d(TAG, "NdefRecord.RTD_TEXT detected");
            if (payload.length > 0) {
                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                int languageCodeLength = payload[0] & 0077;
                String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                Log.d(TAG, "parseWellKnownPayload encoding: " + textEncoding + " languagecodelength: "
                        + languageCodeLength + " languageCode: " + languageCode);
                ret = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                Log.d(TAG, "parseWellKnownPayload text: " + ret);
            }

            return ret;
        } else {// if (Arrays.equals(type, NdefRecord.RTD_URI)) {
            ret = parseUri(pRecord);

            return ret;
        }
    }

    private String parseUri(NdefRecord pNdefRecord) {
        String ret = "";
        if (isURLRecord(pNdefRecord)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Uri u = pNdefRecord.toUri();
                if (u != null) {
                    Log.d(TAG, "parseWellKnownPayload - URI detected");
                    ret = u.toString();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(u);
                    startActivity(i);
                }
            } else {
                logAndToast("Intelligent URI parsing unsupported - API level 16 required!");
            }
        }

        return ret;
    }

    private boolean isURLRecord(NdefRecord pNdefRecord) {
        short tnf = pNdefRecord.getTnf();
        byte[] type = pNdefRecord.getType();

        if (NdefRecord.TNF_ABSOLUTE_URI == tnf
                || (NdefRecord.TNF_WELL_KNOWN == tnf && (Arrays.equals(NdefRecord.RTD_URI, type) || Arrays.equals(
                NdefRecord.RTD_SMART_POSTER, type)))) {
            return true;
        } else {
            return false;
        }
    }

    private String normalizeMimeType(String pType) {
        if (pType == null) {
            return null;
        } else {
            pType = pType.trim().toLowerCase(Locale.US);
            int semicolonIndex = pType.indexOf(';');
            if (semicolonIndex >= 0) {
                pType = pType.substring(0, semicolonIndex);
            }

            return pType;
        }
    }

    private String detectMimeType(short pTnf, byte[] pType) {
        String ret = "";

        switch (pTnf) {
            case NdefRecord.TNF_WELL_KNOWN :
                if (Arrays.equals(pType, NdefRecord.RTD_TEXT)) {
                    ret = TEXT_PLAIN_MIME_TYPE;
                }
                break;
            case NdefRecord.TNF_MIME_MEDIA :
                String mimeType = new String(pType, Charset.forName("US_ASCII"));
                ret = normalizeMimeType(mimeType);
        }

        return ret;
    }

    private String parseMimeMediaPayload(NdefRecord pRecord) throws UnsupportedEncodingException {
        String ret = "";
        String mimeType = detectMimeType(pRecord.getTnf(), pRecord.getType());
        if (!TEXT_PLAIN_MIME_TYPE.equals(mimeType)) {
            Log.w(TAG, "parseMimeMediaPayload - unsupported mime type detected: " + mimeType);
            return "";
        }

        byte[] payload = pRecord.getPayload();
        Log.d(TAG, "parseMimeMediaPayload mimeType: " + mimeType + " payload length: " + payload.length);

        if (payload.length > 0) {
            ret = new String(payload);
            Log.d(TAG, "parseMimeMediaPayload Text: " + ret);
        }

        return ret;
    }

    private String parsePayload(NdefRecord pRecord) {
        Log.d(TAG, "parsePayload start, TNF: " + pRecord.getTnf() + " type: " + pRecord.getType());
        String ret = "";

        // TNF values supported by this application:
        switch (pRecord.getTnf()) {
            case NdefRecord.TNF_WELL_KNOWN :
                try {
                    ret = parseWellKnownPayload(pRecord);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "parsePayload TNF_WELL_KNOWN - UnsupportedEncodingException");
                    e.printStackTrace();
                }
                break;

            case NdefRecord.TNF_MIME_MEDIA :
                try {
                    ret = parseMimeMediaPayload(pRecord);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "parsePayload TNF_MIME_MEDIA - UnsupportedEncodingException");
                    e.printStackTrace();
                }
                break;

            case NdefRecord.TNF_ABSOLUTE_URI :
                ret = parseUri(pRecord);
                // Log.w(TAG, "Unsupported NdefRecord type detected: " +
                // NdefRecord.TNF_ABSOLUTE_URI);
                break;

            case NdefRecord.TNF_EXTERNAL_TYPE :
                ret = parseUri(pRecord);
                // Log.w(TAG, "Unsupported NdefRecord type detected: " +
                // NdefRecord.TNF_EXTERNAL_TYPE);
                break;
        }

        return ret;
    }

    private void writeTagNewThread(final NdefMessage pMessage, final Tag pTag) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                writeTag(pMessage, pTag);
            }
        }).start();
    }

    private boolean writeTag(final NdefMessage pMessage, final Tag pTag) {
        boolean ret = false;

        if (pTag == null) {
            logAndToast("Write failed - no tag detected");
            return false;
        }
        int messageSize = pMessage.toByteArray().length;

        NdefFormatable nf = NdefFormatable.get(pTag);
        Ndef ndef = Ndef.get(pTag);

        synchronized (syncObject) {
            Log.i(TAG, "writeTag start, message size: " + messageSize);

            try {
                if (nf != null) {
                    Log.d(TAG, "writeTag - NdefFormatable Tag detected");
                    nf.connect();
                    nf.format(pMessage);
                    logAndToast("Write completed");

                    ret = true;
                } else if (ndef != null) {
                    ndef.connect();
                    if (messageSize > ndef.getMaxSize()) {
                        logAndToast("Write failed - message size exceeds tag size");
                        ret = false;
                    }
                    if (!ndef.isWritable()) {
                        logAndToast("Write failed - tag is not writable");
                        ret = false;
                    }

                    ndef.writeNdefMessage(pMessage);
                    logAndToast("Write completed");

                    ret = true;
                } else {
                    logAndToast("Write failed - unsupported tag");
                    ret = false;
                }
            } catch (TagLostException e) {
                mTag = null; // forget the tag
                logAndToast("Write failed - TagLostException: " + e.getMessage());
                e.printStackTrace();

                ret = false;
            } catch (IOException e) {
                mTag = null; // forget the tag
                logAndToast("Write failed - IOException: " + e.getMessage());
                e.printStackTrace();

                ret = false;
            } catch (FormatException e) {
                logAndToast("Write failed - FormatException: " + e.getMessage());
                e.printStackTrace();

                ret = false;
            } finally {
                try {
                    if (nf != null) {
                        nf.close();
                    }
                    if (ndef != null) {
                        ndef.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing");
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    private void logAndToast(final String pMessage) {
        Log.i(TAG, pMessage);
        toast(pMessage);
    }

    private void toast(final String pMessage) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(NFCActivity.this.getApplicationContext(), pMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
