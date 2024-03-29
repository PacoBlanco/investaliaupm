	package gsi.investalia.android.app;

import gsi.investalia.android.jade.JadeAdapter;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Main extends TabActivity {

	// Jade
	private JadeAdapter jadeAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);

		Resources res = getResources();
		TabHost host = getTabHost();

		// An intent to activate each activity
		Intent home = new Intent(this, Home.class);
		Intent messages = new Intent(this, MessageList.class);
		Intent compose = new Intent(this, Compose.class);
		Intent profile = new Intent(this, Profile.class);

		// Link each activity to a tab
		host.addTab(host.newTabSpec("home")
				.setIndicator(this.getString(R.string.home),
						res.getDrawable(R.drawable.home)).setContent(home));

		host.addTab(host.newTabSpec("messages").setIndicator(
				this.getString(R.string.messages),
				res.getDrawable(R.drawable.messages)).setContent(messages));

		host.addTab(host.newTabSpec("compose").setIndicator(
				this.getString(R.string.compose),
				res.getDrawable(R.drawable.compose)).setContent(compose));

		host.addTab(host.newTabSpec("profile").setIndicator(
				this.getString(R.string.profile),
				res.getDrawable(R.drawable.profile)).setContent(profile));
		
	}
	
	public JadeAdapter getJadeAdapter() {
		return jadeAdapter;
	}
	
	public void setJadeAdapter(JadeAdapter jadeAdapter) {
		this.jadeAdapter = jadeAdapter;
	}
}
