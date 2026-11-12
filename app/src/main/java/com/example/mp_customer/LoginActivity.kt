package com.example.mp_customer

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // XML 디자인 파일 연결 (activity_login.xml)
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 로그인 화면에 있는 위젯(입력창, 버튼) 찾아오기
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<LinearLayout>(R.id.btnLogin) // XML에서 노란색 로그인 버튼의 태그가 LinearLayout인 경우

        // 2. 로그인 버튼을 눌렀을 때 동작
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            // 3. 유효성 검사 (이메일이나 비밀번호가 비어있으면 토스트 띄우기)
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. 비어있지 않다면 메인 화면(MainActivity)으로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // 5. 로그인 화면은 뒤로 가기 눌렀을 때 나오지 않도록 닫아주기
            finish()
        }
    }
}