package org.buland.testingapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ReceiverActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver);

        String incoming_msg = getIntent().getStringExtra(TestingAppActivity.EXTRA_MESSAGE);

        final TextView output = (TextView) findViewById(R.id.receiver);
        output.setText(incoming_msg);
    }
}
