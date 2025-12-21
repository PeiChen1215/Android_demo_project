package com.example.android_development.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.UserDAO;
import com.example.android_development.model.User;
import com.example.android_development.util.PrefsManager;
import com.example.android_development.util.Constants;

public class MainActivity extends AppCompatActivity {

    private TextView textViewWelcome;
    private TextView textViewUserInfo;
    private TextView textViewRoleDescription;
    private Button buttonLogout;

    private PrefsManager prefsManager;
    private UserDAO userDAO;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        initViews();

        // 初始化数据
        initData();

        // 显示用户信息
        displayUserInfo();

        // 设置点击事件
        setupClickListeners();
    }

    // 在initViews方法中添加
    private void initViews() {
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewUserInfo = findViewById(R.id.textViewUserInfo);
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewRoleDescription = findViewById(R.id.textViewRoleDescription);
        // 商品管理按钮 - 对所有角色显示
        Button buttonProductManage = findViewById(R.id.buttonProductManage);
        if (buttonProductManage != null) {
            buttonProductManage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 所有角色都跳转到同一个ProductListActivity
                    // 在ProductListActivity内部根据角色控制权限
                    Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void initData() {
        prefsManager = new PrefsManager(this);

        // 检查是否已登录
        if (!prefsManager.isLoggedIn()) {
            // 如果未登录，跳转到登录页面
            goToLoginActivity();
            return;
        }

        // 获取当前用户信息
        String userId = prefsManager.getUserId();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);
        userDAO.open();

        currentUser = userDAO.getUserById(userId);

        if (currentUser == null) {
            // 如果用户不存在，清除登录状态并跳转到登录页面
            prefsManager.clearLoginInfo();
            goToLoginActivity();
        }
    }

    private void displayUserInfo() {
        if (currentUser != null) {
            // 欢迎信息
            String welcomeText = "欢迎，" + currentUser.getFullName() + "！";
            textViewWelcome.setText(welcomeText);

            // 用户详细信息
            String userInfo = "用户名: " + currentUser.getUsername() + "\n" +
                    "角 色: " + getRoleName(currentUser.getRole()) + "\n" +
                    "登录时间: " + getCurrentTime();
            textViewUserInfo.setText(userInfo);

            // 角色描述
            String roleDescription = getRoleDescription(currentUser.getRole());
            textViewRoleDescription.setText(roleDescription);
        }
    }

    private String getRoleName(String role) {
        switch (role) {
            case Constants.ROLE_ADMIN:
                return "系统管理员";
            case Constants.ROLE_BUYER:
                return "采购员";
            case Constants.ROLE_CASHIER:
                return "收银员";
            case Constants.ROLE_STOCK:
                return "库存管理员";
            case Constants.ROLE_INVENTORY:
                return "盘点员";
            default:
                return "未知角色";
        }
    }

    private String getRoleDescription(String role) {
        switch (role) {
            case Constants.ROLE_ADMIN:
                return "系统管理员：拥有系统所有权限，可以管理用户和所有子系统。";
            case Constants.ROLE_BUYER:
                return "采购员：负责商品采购，管理供应商信息，制定采购计划。";
            case Constants.ROLE_CASHIER:
                return "收银员：负责销售收银，处理顾客结账，管理销售记录。";
            case Constants.ROLE_STOCK:
                return "库存管理员：负责商品库存管理，包括入库、出库、调拨等操作。";
            case Constants.ROLE_INVENTORY:
                return "盘点员：负责库存盘点，核对实际库存与系统记录，生成盘点报告。";
            default:
                return "用户：可以浏览商品信息，查看个人消费记录。";
        }
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date());
    }

    private void setupClickListeners() {
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        // 清除登录信息
        prefsManager.clearLoginInfo();

        // 关闭数据库连接
        if (userDAO != null) {
            userDAO.close();
        }

        // 显示退出登录消息
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();

        // 跳转到登录页面
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // 关闭当前Activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接
        if (userDAO != null) {
            userDAO.close();
        }
    }
}