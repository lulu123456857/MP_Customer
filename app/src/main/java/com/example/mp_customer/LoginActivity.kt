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
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// [추가] Firebase Authentication(인증) 및 Firestore(데이터베이스) 사용을 위한 라이브러리 import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false // 비밀번호 표시 상태 관리
    // [추가] Firebase 인증 객체와 Firestore DB 객체를 담을 변수 선언
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // [추가] Firebase Auth 및 Firestore 인스턴스 초기화 (서버와 연결 준비)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                // 비밀번호 숨기기 (* 처리)
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            // 커서를 맨 뒤로 이동
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

            // [추가] 서버 응답 대기 중 로그인 버튼 연속 클릭 방지
            btnLogin.isEnabled = false

            // [변경] 기존에는 검사 없이 바로 통과시켰으나, Firebase Auth 서버에 이메일/비밀번호 일치 여부 검증 요청
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // [추가] 로그인 성공 시 현재 로그인된 사용자의 고유 번호(UID) 가져오기
                        val uid = auth.currentUser?.uid ?: ""

                        // [추가] Firestore 'users' 컬렉션에서 해당 UID의 회원 문서(닉네임 등) 조회
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                val firestoreNickname = document.getString("nickname")
                                // [변경] 조회한 닉네임을 로컬 저장소에 반영하고 메인 화면으로 이동
                                completeLoginAndMove(email, firestoreNickname)
                            }
                            .addOnFailureListener {
                                // [추가] DB 조회에 일시 실패하더라도 로그인 자체는 정상 완료 처리
                                completeLoginAndMove(email, null)
                            }
                    } else {
                        // [추가] 로그인 실패 시 버튼 다시 활성화 및 원인별 안내 메시지 출력
                        btnLogin.isEnabled = true
                        val errorMsg = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "등록되지 않은 이메일입니다."
                            is FirebaseAuthInvalidCredentialsException -> "이메일 또는 비밀번호가 올바르지 않습니다."
                            else -> "로그인 실패: ${task.exception?.localizedMessage}"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // 5. 구글 로그인 버튼
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "구글 로그인 기능은 준비 중이에요.", Toast.LENGTH_SHORT).show()
        }

        // 6. 비밀번호 찾기
        btnFindPassword.setOnClickListener {
            Toast.makeText(this, "비밀번호 찾기 화면 (준비 중)", Toast.LENGTH_SHORT).show()
        }

        // 7. 회원가입 버튼 → 회원가입 화면으로 이동
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 8. 로그인 없이 둘러보기
        btnGuest.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // [추가] 기존 로그인 성공 처리 로직(SharedPreferences 저장 + MainActivity 이동)을 별도 함수로 분리
    // Firestore에서 가져온 닉네임을 우선 적용하고, 없으면 기존 로컬 저장소 닉네임을 유지해 마이페이지와 호환됨
    private fun completeLoginAndMove(email: String, firestoreNickname: String?) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val localSavedNickname = sharedPreferences.getString("NICKNAME_$email", null)
        val finalNickname = firestoreNickname ?: localSavedNickname

        sharedPreferences.edit {
            putBoolean("IS_LOGGED_IN", true)
            putString("USER_EMAIL", email)
            if (finalNickname != null) {
                putString("NICKNAME", finalNickname)
                putString("NICKNAME_$email", finalNickname)
            }
        }

        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}