package org.udoo.androidadkdemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import org.udoo.bossacjni.BossacManager;

import java.io.File;

class FlashSketchTask extends AsyncTask<String, Void, Integer> {

    private ProgressDialog pd;
    private LedActivity context;

    FlashSketchTask(LedActivity context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(context);
        pd.setTitle("Arduino");
        pd.setMessage("Flashing sketch. Please wait...");
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected Integer doInBackground(String... params) {
        BossacManager dd = new BossacManager();
        return dd.BossacWriteImage(params[0], true);
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (pd.isShowing()) pd.dismiss();

        String message;
        if (result == 0) {
            message = "Sketch flashed successfully!";
        } else {
            message = "Error during flashing sketch!";
        }

        new AlertDialog.Builder(context)
                .setTitle("ADK Manager")
                .setMessage(message)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface d, int arg1) {
                        d.cancel();
                    };
                })
                .show();
    }

}