package com.example.android_development.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import com.example.android_development.util.PrefsManager;

/**
 * 启动页（Splash）。
 *
 * <p>用于展示启动画面，并在短暂延迟后根据登录态跳转到主页面或登录页。</p>
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5秒

    @Override
    /**
     * Activity 创建：展示启动页并延迟执行登录态检查。
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 延迟跳转
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        }, SPLASH_DELAY);
    }

    /**
     * 检查登录态并跳转。
     *
     * <p>已登录：进入 {@link MainActivity}；未登录：进入 {@link LoginActivity}。</p>
     */
    private void checkLoginStatus() {
        PrefsManager prefsManager = new PrefsManager(this);

        if (prefsManager.isLoggedIn()) {
            // 如果已经登录，直接跳转到主页面
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            // 如果未登录，跳转到登录页面
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        finish(); // 关闭当前Activity
    }
}