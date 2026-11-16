package com.example.mp_customer

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
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
            // TODO: 회원가입 처리 (서버/Firebase 연동 등)
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