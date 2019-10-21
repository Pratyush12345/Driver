package com.example.driver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Zone extends AppCompatActivity {


    private Button button;
    public EditText zone;
    public EditText ward;
    public EditText subward;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone);
        button=(Button)findViewById(R.id.Enter);
        zone=(EditText)findViewById(R.id.zone);
        ward=(EditText)findViewById(R.id.ward);
        subward=(EditText)findViewById(R.id.subward);

        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String value1=zone.getText().toString().trim();
                String value2=ward.getText().toString().trim();
                String value3=subward.getText().toString().trim();


                    Intent intent = new Intent(Zone.this, MapsActivity.class);
                    intent.putExtra("Zone", value1);
                    intent.putExtra("Ward", value2);
                    intent.putExtra("Subward", value3);
                    startActivity(intent);
                    //Zone.this.finish();
            }
        } );

    }

}
