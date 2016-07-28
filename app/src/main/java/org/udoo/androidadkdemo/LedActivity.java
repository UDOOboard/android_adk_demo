package org.udoo.androidadkdemo;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import me.palazzetti.adktoolkit.AdkManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LedActivity extends AppCompatActivity {

    private AdkManager mAdkManager;
    private UsbManager usbManager;

    private static final String ACTION_USB_PERMISSION = "org.udoo.androidadkdemo.action.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);
        copyAssets();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mAdkManager = new AdkManager(usbManager);

        registerReceiver(mAdkManager.getUsbReceiver(), mAdkManager.getDetachedFilter());
        Log.i("AdkManager", "available: " + mAdkManager.serialAvailable());
        checkAccessory();

        Switch onOffSwitch = (Switch) findViewById(R.id.ledSwitch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // writeSerial() allows you to write a single char or a String object.
                    mAdkManager.write("1");
                } else {
                    mAdkManager.write("0");
                }
            }
        });
    }

    private boolean checkAccessory() {
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories == null) {
            new AlertDialog.Builder(this)
                .setTitle("ADK Manager")
                .setMessage("No accessory connected! Enable ADK communication (in Settings -> UDOO) or flash the Arduino sketch.")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface d, int arg1) {
                        d.cancel();
                    };
                })
                .show();

            Log.e("ADK", "No accessory connected! Enable ADK communication (in Settings -> UDOO) or flash the Arduino sketch.");
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mAdkManager.open();
        } catch (java.lang.SecurityException e) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(usbManager.getAccessoryList()[0], pendingIntent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdkManager.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAdkManager.getUsbReceiver());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.flashSketch:
                File sketch = new File(getExternalFilesDir(null), "UDOOArduinoADKDemoV2.bin");
                Log.w("TAG", sketch.getAbsolutePath());
                new FlashSketchTask(LedActivity.this).execute(sketch.getAbsolutePath());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        String filename = "UDOOArduinoADKDemoV2.bin";
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(getExternalFilesDir(null), filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + filename, e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
