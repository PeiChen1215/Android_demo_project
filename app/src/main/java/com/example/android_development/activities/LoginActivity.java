package com.example.android_development.activities;


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
import com.example.android_development.util.PrefsManager;

public class LoginActivity extends AppCompatActivity {

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
        // 显示登录失败消息
        Toast.makeText(this, "登录失败！用户名或密码错误", Toast.LENGTH_SHORT).show();

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
