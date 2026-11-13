package com.example.mp_customer

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false // 비밀번호 표시 상태 관리

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 최상단 ScrollView(id = main)에 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<ScrollView>(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. XML 위젯 연결
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePassword = findViewById<ImageView>(R.id.btnTogglePassword)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<AppCompatButton>(R.id.btnGoogleLogin)
        val btnSignUp = findViewById<TextView>(R.id.btnSignUp)
        val btnGuest = findViewById<TextView>(R.id.btnGuest)
        val btnFindPassword = findViewById<TextView>(R.id.btnFindPassword)

        // 2. 뒤로가기 버튼 동작
        btnBack.setOnClickListener {
            finish() // 이전 화면으로 돌아가기
        }

        // 3. 비밀번호 눈 모양 아이콘 클릭 시 숨김/표시 전환
        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // 비밀번호 보이게
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel) // 원하시는 아이콘으로 변경 가능
            } else {
                // 비밀번호 숨기기 (* 처리)
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            // 커서가 풀리는 것 방지하기 위해 커서를 맨 뒤로 이동
            etPassword.setSelection(etPassword.text.length)
        }

        // 4. 로그인 버튼 클릭 동작
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 유효성 검사
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 로그인 성공 상태 저장 (SharedPreferences 활용)
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("IS_LOGGED_IN", true)
            editor.putString("USER_EMAIL", email)
            editor.apply()

            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

            // 메인 화면(MainActivity)으로 이동 후 로그인 화면 닫기
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 5. 구글 로그인 버튼
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "구글 로그인 기능은 준비 중이에요.", Toast.LENGTH_SHORT).show()
        }

        // 6. 비밀번호 찾기
        btnFindPassword.setOnClickListener {
            Toast.makeText(this, "비밀번호 찾기 화면 (준비 중)", Toast.LENGTH_SHORT).show()
        }

        // 7. 회원가입 버튼
        btnSignUp.setOnClickListener {
            Toast.makeText(this, "회원가입 화면으로 이동 (준비 중)", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, SignUpActivity::class.java)
            // startActivity(intent)
        }

        // 8. 로그인 없이 둘러보기
        btnGuest.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}