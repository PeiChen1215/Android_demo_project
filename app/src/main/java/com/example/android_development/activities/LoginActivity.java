package com.example.android_development.activities;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_development.R;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.android_development.database.DatabaseHelper;
import com.example.android_development.database.UserDAO;
import com.example.android_development.model.User;
import com.example.android_development.util.Constants;
import com.example.android_development.util.PrefsManager;


public class LoginActivity extends AppCompatActivity {
    final boolean ischeckuserdb = false;
    final boolean ischeckproductdb = false;

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;

    private UserDAO userDAO;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        initViews();

        // 初始化数据库和PrefsManager
        initDatabase();

        // 设置点击事件
        setupClickListeners();

        // 检查是否已经登录
        checkIfAlreadyLoggedIn();
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
    }

    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);
        userDAO.open();

        prefsManager = new PrefsManager(this);

        //debug输出所有的用户数据库成员
        if(ischeckuserdb) debugPrintAllUsers();

        //debug输出检测商品数据库
        if(ischeckproductdb) debugPrintAllProducts();
    }

    //debug输出所有的用户数据库成员
    private void debugPrintAllUsers() {
        try {
            // 直接查询数据库
            SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();
            Cursor cursor = db.query(
                    Constants.TABLE_USERS,
                    null, // 所有列
                    null, // 无筛选条件
                    null, // 无参数
                    null, // 无分组
                    null, // 无过滤
                    null  // 无排序
            );

            android.util.Log.d("DEBUG", "=== 数据库用户总数: " + cursor.getCount() + " ===");

            if (cursor.moveToFirst()) {
                do {
                    String userId = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_ID));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USERNAME));
                    String password = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PASSWORD));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ROLE));
                    String fullName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_FULL_NAME));

                    android.util.Log.d("DEBUG", "用户: " + username +
                            " | 密码: " + password +
                            " | 角色: " + role +
                            " | 姓名: " + fullName);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            android.util.Log.e("DEBUG", "查询数据库错误: " + e.getMessage());
        }
    }

    //debug打印所有商品
    private void debugPrintAllProducts() {
        try {
            SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();

            Cursor cursor = db.query(
                    Constants.TABLE_PRODUCTS,
                    null, // 所有列
                    null, // 无筛选条件
                    null, // 无参数
                    null, // 无分组
                    null, // 无过滤
                    null  // 无排序
            );

            android.util.Log.d("DEBUG", "=== 商品总数: " + cursor.getCount() + " ===");

            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PRODUCT_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PRODUCT_NAME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.COLUMN_PRICE));
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_STOCK));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_CATEGORY));

                    android.util.Log.d("DEBUG", "商品: " + name +
                            " | 价格: " + price +
                            " | 库存: " + stock +
                            " | 分类: " + category);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            android.util.Log.e("DEBUG", "查询商品表错误: " + e.getMessage());
        }
    }
    private void setupClickListeners() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void checkIfAlreadyLoggedIn() {
        if (prefsManager.isLoggedIn()) {
            // 如果已经登录，直接跳转到主页面
            goToMainActivity();
        }
    }

    private void attemptLogin() {
        // 获取输入的用户名和密码
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // 验证输入
        if (validateInput(username, password)) {
            // 验证用户
            User user = userDAO.authenticateUser(username, password);

            if (user != null) {
                // 登录成功
                loginSuccess(user);
            } else {
                // 登录失败
                loginFailed();
            }
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            editTextUsername.setError("请输入用户名");
            editTextUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("请输入密码");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginSuccess(User user) {
        // 保存登录信息到SharedPreferences
        prefsManager.saveLoginStatus(true);
        prefsManager.saveUserId(user.getId());
        prefsManager.saveUserRole(user.getRole());

        // 显示登录成功消息
        Toast.makeText(this, "登录成功！欢迎 " + user.getFullName(), Toast.LENGTH_SHORT).show();

        // 跳转到主页面
        goToMainActivity();
    }

    private void loginFailed() {
        String username = editTextUsername.getText().toString().trim();

        // 检查用户名是否存在
        if (userDAO.isUsernameExists(username)) {
            Toast.makeText(this, "密码错误！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "用户名不存在！", Toast.LENGTH_SHORT).show();
        }

        // 清空密码框
        editTextPassword.setText("");
        editTextPassword.requestFocus();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // 关闭登录页面，防止用户按返回键回到登录页
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
