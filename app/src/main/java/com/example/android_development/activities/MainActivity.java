package com.example.android_development.activities;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
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
    private static final int REQ_NOTIF = 1002;

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

        // 尝试在有权限时启动后台库存监控服务（若无权限，会请求并在回调中启动）
        startMonitorIfAllowed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 可能用户刚刚在系统设置中打开了通知权限，回到页面时检查并启动
        startMonitorIfAllowed();
    }

    private void startMonitorIfAllowed() {
        Intent svcIntent = new Intent(MainActivity.this, com.example.android_development.services.InventoryMonitorService.class);
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // 提前提示并请求权限
                    Toast.makeText(this, "应用需要通知权限以启用库存监控提醒", Toast.LENGTH_LONG).show();
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
                    return;
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(svcIntent);
            } else {
                startService(svcIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Intent svcIntent = new Intent(MainActivity.this, com.example.android_development.services.InventoryMonitorService.class);
            try {
                if (granted) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startForegroundService(svcIntent);
                    } else {
                        startService(svcIntent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 在initViews方法中添加
    private void initViews() {
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewUserInfo = findViewById(R.id.textViewUserInfo);
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewRoleDescription = findViewById(R.id.textViewRoleDescription);
        
        // 获取当前角色
        PrefsManager pm = new PrefsManager(this);
        String role = pm.getUserRole();

        // 商品管理按钮 - 对所有角色显示
        Button buttonProductManage = findViewById(R.id.buttonProductManage);
        if (buttonProductManage != null) {
            buttonProductManage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                    startActivity(intent);
                }
            });
        }
            // 库存盘点入口 - 仅库存管理员/管理员
            Button buttonStockCount = findViewById(R.id.buttonStockCount);
            if (buttonStockCount != null) {
                if (Constants.ROLE_ADMIN.equals(role) || Constants.ROLE_STOCK.equals(role)) {
                    buttonStockCount.setVisibility(View.VISIBLE);
                    buttonStockCount.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, StockCountActivity.class);
                        startActivity(intent);
                    });
                } else {
                    buttonStockCount.setVisibility(View.GONE);
                }
            }

            // 采购管理入口 - 仅采购员/管理员
            Button buttonPurchase = findViewById(R.id.buttonPurchase);
            if (buttonPurchase != null) {
                if (Constants.ROLE_ADMIN.equals(role) || Constants.ROLE_BUYER.equals(role)) {
                    buttonPurchase.setVisibility(View.VISIBLE);
                    buttonPurchase.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, PurchaseListActivity.class);
                        startActivity(intent);
                    });
                } else {
                    buttonPurchase.setVisibility(View.GONE);
                }
            }
            
            // 营收报表入口 - 基于权限控制
            Button buttonRevenue = findViewById(R.id.buttonRevenue);
            if (buttonRevenue != null) {
                boolean canViewRevenue = com.example.android_development.security.Auth.hasPermission(this, com.example.android_development.util.Constants.PERM_VIEW_REVENUE);
                if (canViewRevenue) {
                    buttonRevenue.setVisibility(View.VISIBLE);
                    buttonRevenue.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, RevenueReportActivity.class);
                        startActivity(intent);
                    });
                } else {
                    buttonRevenue.setVisibility(View.GONE);
                }
            }
            
            // 销售收银入口 - 仅收银员/管理员
            Button buttonPOS = findViewById(R.id.buttonPOS);
            if (buttonPOS != null) {
                if (Constants.ROLE_ADMIN.equals(role) || Constants.ROLE_CASHIER.equals(role)) {
                    buttonPOS.setVisibility(View.VISIBLE);
                    buttonPOS.setEnabled(true);
                    buttonPOS.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, com.example.android_development.activities.SaleActivity.class);
                        startActivity(intent);
                    });
                } else {
                    buttonPOS.setVisibility(View.GONE);
                }
            }

            // 收据列表入口 - 仅收银/管理员/财务可见
            Button buttonReceipts = findViewById(R.id.buttonReceipts);
            if (buttonReceipts != null) {
                if (Constants.ROLE_ADMIN.equals(role) || Constants.ROLE_CASHIER.equals(role) || Constants.ROLE_FINANCE.equals(role)) {
                    buttonReceipts.setVisibility(View.VISIBLE);
                    buttonReceipts.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, ReceiptListActivity.class);
                        startActivity(intent);
                    });
                } else {
                    buttonReceipts.setVisibility(View.GONE);
                }
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
            case Constants.ROLE_FINANCE:
                return "财务/出纳";
            case Constants.ROLE_BUYER:
                return "采购员";
            case Constants.ROLE_CASHIER:
                return "收银员";
            case Constants.ROLE_STOCK:
                return "库存管理员";
            default:
                return "未知角色";
        }
    }

    private String getRoleDescription(String role) {
        switch (role) {
            case Constants.ROLE_ADMIN:
                return "系统管理员：拥有系统所有权限，可以管理用户和所有子系统。";
            case Constants.ROLE_FINANCE:
                return "财务/出纳：负责营收报表、对账、退款审核等财务相关工作。";
            case Constants.ROLE_BUYER:
                return "采购员：负责商品采购，管理供应商信息，制定采购计划。";
            case Constants.ROLE_CASHIER:
                return "收银员：负责销售收银，处理顾客结账，管理销售记录。";
            case Constants.ROLE_STOCK:
                return "库存管理员：负责商品库存管理，包括入库、出库、调拨以及盘点等操作。";
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