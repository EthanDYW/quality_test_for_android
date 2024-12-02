package com.example.qualitytestforandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var backButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        // 初始化视图
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        backButton = findViewById(R.id.backButton)

        // 设置登录按钮点击事件
        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString() ?: ""
            val password = passwordInput.text?.toString() ?: ""

            if (validateCredentials(username, password)) {
                // 登录成功，跳转到管理员主界面
                val intent = Intent(this, AdminMainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // 登录失败，显示错误信息
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
            }
        }

        // 设置返回按钮点击事件
        backButton.setOnClickListener {
            // 返回到正常登录界面
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun validateCredentials(username: String, password: String): Boolean {
        // 简单的验证逻辑，实际应用中应该使用更安全的方式
        return username == "admin" && password == "admin"
    }

    override fun onBackPressed() {
        // 处理返回键点击事件
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
