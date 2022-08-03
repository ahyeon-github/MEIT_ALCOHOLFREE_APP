package com.example.qrscanner;

import android.Manifest;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class StartMeasureActivity extends AppCompatActivity {
    ImageView startBtn;
    TextView startTv;
    private BluetoothSPP bt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_start);

        // 객체 생성 후 미리 선언한 변수에 넣음
        bt = new BluetoothSPP(this); //Initializing

        startBtn = (ImageView) findViewById(R.id.measure_start_iv);
        startTv = (TextView) findViewById(R.id.measure_start_tv);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTv.setText("측정 중..."); // 측정 중 표시

                if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가라면
                    // 사용불가라고 토스트 띄워줌
                    Toast.makeText(getApplicationContext()
                            , "Bluetooth is not available"
                            , Toast.LENGTH_SHORT).show();
                    // 화면 종료
                    finish();
                }

                // 데이터를 받았는지 감지하는 리스너
                bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                    //데이터 수신되면
                    public void onDataReceived(byte[] data, String message) {
                        Toast.makeText(StartMeasureActivity.this, message, Toast.LENGTH_SHORT).show(); // 토스트로 데이터 띄움
                    }
                });
                // 블루투스가 잘 연결이 되었는지 감지하는 리스너
                bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
                    public void onDeviceConnected(String name, String address) {
                        Toast.makeText(getApplicationContext()
                                , "Connected to " + name + "\n" + address
                                , Toast.LENGTH_SHORT).show();
                    }

                    public void onDeviceDisconnected() { //연결해제
                        Toast.makeText(getApplicationContext()
                                , "Connection lost", Toast.LENGTH_SHORT).show();
                    }

                    public void onDeviceConnectionFailed() { //연결실패
                        Toast.makeText(getApplicationContext()
                                , "Unable to connect", Toast.LENGTH_SHORT).show();
                    }
                });

                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) { // 현재 버튼의 상태에 따라 연결이 되어있으면 끊고, 반대면 연결
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }

//                // 버튼 클릭하면
//                btnConnect.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) { // 현재 버튼의 상태에 따라 연결이 되어있으면 끊고, 반대면 연결
//                            bt.disconnect();
//                        } else {
//                            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
//                            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
//                        }
//                    }
//                });

                // 10초동안 측정 후 측정완료 화면으로 전환
                measured();
            }
        });
    }

    public void measured() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), CompleteMeasureActivity.class);
                startActivity(intent);
            }
        }, 10000);
    }


    // 앱 중단시 (액티비티 나가거나, 특정 사유로 중단시)
    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    // 앱이 시작하면
    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { // 앱의 상태를 보고 블루투스 사용 가능하면
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 새로운 액티비티 띄워줌, 거기에 현재 가능한 블루투스 정보 intent로 넘겨
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) { // 블루투스 사용 불가
                // setupService() 실행하도록
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기끼리
                // 셋팅 후 연결되면 setup()으로
                setup();
            }
        }
    }
    // 블루투스 사용 - 데이터 전송
    public void setup() {
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.send("1", true);
            }
        });
    }
    // 새로운 액티비티 (현재 액티비티의 반환 액티비티?)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 아까 응답의 코드에 따라 연결 가능한 디바이스와 연결 시도 후 ok 뜨면 데이터 전송
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) { // 연결시도
            if (resultCode == Activity.RESULT_OK) // 연결됨
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) { // 연결 가능
            if (resultCode == Activity.RESULT_OK) { // 연결됨
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else { // 사용불가
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}