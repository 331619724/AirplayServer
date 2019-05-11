/*
 * MIT License
 *
 * Copyright (c) 2019 dsafa22, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fang.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    public static String TAG = "MainActivity";

    private AirPlayServer mAirPlayServer;
    private RaopServer mRaopServer;

    private SurfaceView mSurfaceView;
    private Button mBtnControl;
    private TextView mTxtDevice;
    private boolean mIsStart = false;
    private byte[] mMacAddress;

    private String mDeviceName;
    private int mDeviceTail = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSystemService(Context.NSD_SERVICE);
        mBtnControl = findViewById(R.id.btn_control);
        mTxtDevice = findViewById(R.id.txt_device);
        mBtnControl.setOnClickListener(this);
        mSurfaceView = findViewById(R.id.surface);
        mAirPlayServer = new AirPlayServer();
        mRaopServer = new RaopServer(mSurfaceView);
        mMacAddress = NetUtils.getLocalMacAddress2();
    }

    public void changeDeviceName() {
        mDeviceName = "t" + mDeviceTail++;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_control: {
                if (!mIsStart) {
                    startServer();
                    mTxtDevice.setText("设备名称:" + mDeviceName);
                } else {
                    stopServer();
                    mTxtDevice.setText("未启动");
                }
                mIsStart = !mIsStart;
                mBtnControl.setText(mIsStart ? "结束" : "开始");
                break;
            }
        }
    }

    private void startServer() {
        changeDeviceName();
        mAirPlayServer.startServer();
        int airplayPort = mAirPlayServer.getPort();
        if (airplayPort == 0) {
            Toast.makeText(this.getApplicationContext(), "启动airplay服务失败", Toast.LENGTH_SHORT).show();
        }
        mRaopServer.startServer(mDeviceName, mMacAddress, airplayPort);
        int raopPort = mRaopServer.getPort();
        if (raopPort == 0) {
            Toast.makeText(this.getApplicationContext(), "启动raop服务失败", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "airplayPort = " + airplayPort + ", raopPort = " + raopPort);

    }

    private void stopServer() {
        mAirPlayServer.stopServer();
        mRaopServer.stopServer();
    }

}


