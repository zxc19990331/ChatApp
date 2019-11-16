package com.stellaris.stchat.activity;

import android.Manifest;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.stellaris.stchat.R;

import java.util.ArrayList;
import java.util.List;

import com.stellaris.stchat.controller.MainController;
import com.stellaris.stchat.view.MainView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import cn.jiguang.api.JCoreInterface;

public class MainActivity extends FragmentActivity {

    private List<String> permissions;
    private MainController mMainController;
    private MainView mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        mMainView = (MainView) findViewById(R.id.main_view);
        mMainView.initModule();
        mMainController = new MainController(mMainView, this);

        mMainView.setOnClickListener(mMainController);
        mMainView.setOnPageChangeListener(mMainController);
        //设置沉浸式用户栏
        //StatusBarUtil.setTransparent(this);
        StatusBarUtil.setColor(this,getResources().getColor(R.color.jmui_jpush_blue));
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // Successfully.
            if(requestCode == 100) {
                Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // Failure.

        }
    };

    private void requestPermission() {
        permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        if (AndPermission.hasPermission(this, permissions)) {
            return;
        }else {
            AndPermission.with(this)
                    .requestCode(100)
                    .permission(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.RECORD_AUDIO)
                    .callback(listener)
                    .start();
        }
    }

    @Override
    protected void onPause() {
        JCoreInterface.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        JCoreInterface.onResume(this);
        //mMainController.sortConvList();
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }
}
