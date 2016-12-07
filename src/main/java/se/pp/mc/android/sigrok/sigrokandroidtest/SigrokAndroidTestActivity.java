
package se.pp.mc.android.sigrok.sigrokandroidtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Vector;

import org.sigrok.androidutils.SigrokApplication;
import org.sigrok.androidutils.UsbSupplicant;

import org.sigrok.core.classes.Context;
import org.sigrok.core.classes.Driver;
import org.sigrok.core.classes.HardwareDevice;
import org.sigrok.core.classes.LogLevel;
import org.sigrok.core.interfaces.LogCallback;

public class SigrokAndroidTestActivity extends Activity
{
    private Context context;
    private UsbSupplicant supplicant;

    private Button scanButton;

    private class ScanTask extends AsyncTask<Void, Void, HardwareDevice[]>
    {
	private ProgressDialog pd;

	@Override
	protected void onPreExecute()
	{
	    pd = new ProgressDialog(SigrokAndroidTestActivity.this);
	    scanButton.setEnabled(false);
	    pd.setTitle("Scanning...");
	    pd.setMessage("Please wait.");
	    pd.setCancelable(false);
	    pd.setIndeterminate(true);
	    pd.show();
	}
	
	@Override
	protected HardwareDevice[] doInBackground(Void... arg0) {
	    Vector<HardwareDevice> devices = new Vector<HardwareDevice>();
	    for (Driver driver : context.drivers().values())
	    {
		devices.addAll(driver.scan());
	    }
	    return devices.toArray(new HardwareDevice[0]);
	}

	@Override
        protected void onPostExecute(HardwareDevice[] devices) {
	    if (pd!=null) {
		pd.dismiss();
		scanButton.setEnabled(true);
	    }
	    ((TextView)findViewById(R.id.log)).append("Found "+devices.length+" devices:\n");
	    for (HardwareDevice device : devices) {
		((TextView)findViewById(R.id.log)).append(device.driver().name()+" - with "+device.channels().size()+" channels"+"\n");
	    }
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

	supplicant = SigrokApplication.createUsbSupplicant(getApplicationContext());

	Button button = (Button)findViewById(R.id.button1);
	button.setText(Context.lib_version());
	button = (Button)findViewById(R.id.button2);
	button.setText(Context.package_version());

	final TextView log = (TextView)findViewById(R.id.log);

	log.setMovementMethod(new ScrollingMovementMethod());

	context = Context.create();
	context.add_log_callback(new LogCallback() {
		@Override
		public void run(final LogLevel level, final String message) {
		    if (level.id() < LogLevel.getDBG().id())
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
				    log.append(message+"\n");
				}
			    });
		}
	    });
	context.set_log_level(LogLevel.getINFO());

	scanButton = (Button)findViewById(R.id.scan);
	scanButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
		    new ScanTask().execute();
		}
	    });
    }

    @Override
    protected void onStart()
    {
	super.onStart();
	supplicant.start();
    }

    @Override
    protected void onStop()
    {
	supplicant.stop();
	super.onStop();
    }
}

