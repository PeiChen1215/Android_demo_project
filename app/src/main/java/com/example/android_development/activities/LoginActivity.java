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


/**
 * 登录页面。
 *
 * <p>负责：用户名/密码校验、调用 {@link UserDAO} 进行鉴权、保存登录态到 {@link PrefsManager}，
 * 并在登录成功后跳转到 {@link MainActivity}。</p>
 */
public class LoginActivity extends AppCompatActivity {
    // 调试开关：true 时在 Logcat 打印数据库内容（仅开发调试使用，发布版本建议保持 false）
    final boolean ischeckuserdb = false;
    final boolean ischeckproductdb = false;

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;

    private UserDAO userDAO;
    private PrefsManager prefsManager;

    @Override
    /**
     * Activity 创建：初始化控件、数据库、点击事件，并检查是否已登录。
     */
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

    /**
     * 初始化页面控件引用。
     */
    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
    }

    /**
     * 初始化数据库访问与本地登录态存储。
     *
     * <p>注意：这里会打开 {@link UserDAO} 的数据库连接，并在 {@link #onDestroy()} 关闭。</p>
     */
    private void initDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);
        userDAO.open();

        prefsManager = new PrefsManager(this);

        // 调试：输出 users 表内容
        if(ischeckuserdb) debugPrintAllUsers();

        // 调试：输出 products 表内容
        if(ischeckproductdb) debugPrintAllProducts();
    }

    /**
     * 调试：输出 users 表全部用户信息（仅开发调试使用）。
     */
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

    /**
     * 调试：输出 products 表全部商品信息（仅开发调试使用）。
     */
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

    /**
     * 绑定按钮点击事件。
     */
    private void setupClickListeners() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    /**
     * 若已存在登录态，则直接跳转主页面。
     */
    private void checkIfAlreadyLoggedIn() {
        if (prefsManager.isLoggedIn()) {
            // 如果已经登录，直接跳转到主页面
            goToMainActivity();
        }
    }

    /**
     * 执行登录流程：读取输入、校验、鉴权、处理成功/失败。
     */
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

    /**
     * 校验登录输入。
     *
     * @param username 用户名
     * @param password 密码
     * @return 校验通过返回 true
     */
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

    /**
     * 登录成功：保存登录态并跳转主页面。
     */
    private void loginSuccess(User user) {
        // 保存登录信息到 SharedPreferences（登录态 / userId / role）
        prefsManager.saveLoginStatus(true);
        prefsManager.saveUserId(user.getId());
        prefsManager.saveUserRole(user.getRole());

        // 显示登录成功消息
        Toast.makeText(this, "登录成功！欢迎 " + user.getFullName(), Toast.LENGTH_SHORT).show();

        // 跳转到主页面
        goToMainActivity();
    }

    /**
     * 登录失败：提示用户并清空密码框。
     */
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

    /**
     * 跳转到主页面并关闭登录页，避免返回键回到登录。
     */
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // 关闭登录页面，防止用户按返回键回到登录页
    }

    @Override
    /**
     * Activity 销毁：关闭数据库连接。
     */
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接
        if (userDAO != null) {
            userDAO.close();
        }
    }

}
