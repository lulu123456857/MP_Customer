package com.example.mp_customer

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etSignUpEmail)
        val etNickname = findViewById<EditText>(R.id.etNickname)
        val etSignUpPassword = findViewById<EditText>(R.id.etSignUpPassword)
        val etSignUpPasswordConfirm = findViewById<EditText>(R.id.etSignUpPasswordConfirm)
        val btnToggleSignUpPassword = findViewById<ImageView>(R.id.btnToggleSignUpPassword)
        val btnToggleSignUpPasswordConfirm = findViewById<ImageView>(R.id.btnToggleSignUpPasswordConfirm)
        val btnSignUpSubmit = findViewById<AppCompatButton>(R.id.btnSignUpSubmit)

        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 비밀번호 / 비밀번호 확인 눈 버튼 설정
        setupPasswordToggle(etSignUpPassword, btnToggleSignUpPassword)
        setupPasswordToggle(etSignUpPasswordConfirm, btnToggleSignUpPasswordConfirm)

        // 가입하기
        btnSignUpSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val nickname = etNickname.text.toString().trim()
            val password = etSignUpPassword.text.toString().trim()
            val passwordConfirm = etSignUpPasswordConfirm.text.toString().trim()

            // 유효성 검사
            if (email.isEmpty() || nickname.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않아요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 이메일별로 닉네임 저장 (로그인할 때 불러와서 마이페이지에 반영)
            getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .edit()
                .putString("NICKNAME_$email", nickname)
                .apply()

            Toast.makeText(this, "회원가입이 완료되었어요! 로그인해주세요.", Toast.LENGTH_SHORT).show()

            // 가입 화면을 닫으면 로그인 화면이 다시 보여요
            finish()
        }
    }

    // 눈 아이콘을 누르면 비밀번호 표시/숨김 전환
    private fun setupPasswordToggle(editText: EditText, toggle: ImageView) {
        var isVisible = false
        toggle.setOnClickListener {
            isVisible = !isVisible
            if (isVisible) {
                editText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                editText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggle.setImageResource(android.R.drawable.ic_menu_view)
            }
            // 커서를 맨 뒤로 이동
            editText.setSelection(editText.text.length)
        }
    }
}