package com.sswu.mp_customer

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Firebase Auth 및 Firestore 인스턴스 초기화
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
            if (password.length < 6) {
                Toast.makeText(this, "비밀번호는 6자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않아요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 클릭 방지
            btnSignUpSubmit.isEnabled = false

            // 1. Firebase Authentication에 이메일/비밀번호로 회원 생성
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""

                        // 2. Firestore 'users' 컬렉션에 사용자 추가 정보(닉네임, 이메일) 저장
                        val userMap = hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "nickname" to nickname,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        db.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                // 기존 팀원 코드(마이페이지 호환용)도 함께 유지
                                getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("NICKNAME_$email", nickname)
                                    .apply()

                                // 가입 직후 자동 로그인 상태가 되므로, 로그인 화면에서 다시 로그인하게 하려면 로그아웃 처리
                                auth.signOut()

                                Toast.makeText(this, "회원가입이 완료되었어요! 로그인해주세요.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                btnSignUpSubmit.isEnabled = true
                                Toast.makeText(this, "회원 정보 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        btnSignUpSubmit.isEnabled = true
                        val errorMsg = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> "이미 가입된 이메일입니다."
                            is FirebaseAuthWeakPasswordException -> "비밀번호가 너무 취약합니다 (6자 이상)."
                            else -> "회원가입 실패: ${task.exception?.localizedMessage}"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
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