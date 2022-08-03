package com.example.qrscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class GuideActivity3 extends AppCompatActivity {
    public void onCreate (Bundle savedInstanceState){
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_guide3);

        Button imageButton = (Button) findViewById(R.id.status_gonext3_btn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),StartMeasureActivity.class);
                startActivity(intent);
            }
        });


    }


}


