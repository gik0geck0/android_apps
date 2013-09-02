package org.buland.testingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.buland.testingapp.ReceiverActivity;

public class TestingAppActivity extends Activity
{
    public static final String EXTRA_MESSAGE = "org.buland.testingapp.MESSAGE";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        final Button doer = (Button) findViewById(R.id.doer);
        doer.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // View binding
                final EditText sender = (EditText) findViewById(R.id.sender);

                Intent intent = new Intent(TestingAppActivity.this, ReceiverActivity.class);
                intent.putExtra(EXTRA_MESSAGE, sender.getText().toString());
                startActivity(intent);
            }
        });
        
        // Make some buttons
        Button left = new Button(this);
        left.setText("Left");
        Button right = new Button(this);
        right.setText("Right");

        // Show them
        final LinearLayout ll = (LinearLayout) findViewById(R.id.mainlayout);
        ll.addView(left);
        ll.addView(right);
        final Context mContext = (Context) this;

        // Add clickListeners
        Button.OnClickListener toaster = new Button.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(mContext, ((Button) v).getText(), 500).show();
            }
        };
        left.setOnClickListener(toaster);
        right.setOnClickListener(toaster);
    }
}
