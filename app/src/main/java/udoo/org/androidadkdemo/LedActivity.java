package udoo.org.androidadkdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class LedActivity extends AppCompatActivity {

    private AdkManager mAdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);

        mAdkManager = new AdkManager((UsbManager) getSystemService(Context.USB_SERVICE));
        registerReceiver(mAdkManager.getUsbReceiver(), mAdkManager.getDetachedFilter());
        Log.i("AdkManager", "available: " + mAdkManager.serialAvailable());

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories == null) {
            new AlertDialog.Builder(this)
                .setTitle("ADK Manager")
                .setMessage("No accessory connected! Enable ADK communication in Settings -> UDOO.")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface d, int arg1) {
                        d.cancel();
                    };
                })
                .show();

            Log.e("ADK", "No accessory connected! Enable ADK communication in Settings -> UDOO.");
            return;
        }

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

    @Override
    public void onResume() {
        super.onResume();
        mAdkManager.open();
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
                flashSketch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // @TODO: implement me!
    private void flashSketch() {
    }
}
