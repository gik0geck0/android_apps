package org.buland.testingapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TestingAppActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // View binding
        final TextView receiver = (TextView) findViewById(R.id.receiver);
        final EditText sender = (EditText) findViewById(R.id.sender);
        final Button doer = (Button) findViewById(R.id.doer);

        doer.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                receiver.setText(sender.getText());
            }
        });

    }
}
