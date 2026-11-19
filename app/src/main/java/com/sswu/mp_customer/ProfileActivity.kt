package com.sswu.mp_customer

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {

    lateinit var btnBack: TextView
    lateinit var btnEditProfileFull: TextView
    lateinit var tvProfileNickname: TextView
    lateinit var tvProfileNicknameInfo: TextView
    lateinit var tvProfileIntro: TextView
    lateinit var btnMyPosts: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 위젯 연결
        btnBack = findViewById(R.id.btnBack)
        btnEditProfileFull = findViewById(R.id.btnEditProfileFull)
        tvProfileNickname = findViewById(R.id.tvProfileNickname)
        tvProfileNicknameInfo = findViewById(R.id.tvProfileNicknameInfo)
        tvProfileIntro = findViewById(R.id.tvProfileIntro)
        btnMyPosts = findViewById(R.id.btnMyPosts)

        // 저장된 닉네임/소개 표시 (없으면 기본값)
        loadProfile()

        // 뒤로가기 → 마이페이지로 돌아가기
        btnBack.setOnClickListener {
            finish()
        }

        // 프로필 수정 → 팝업 표시
        btnEditProfileFull.setOnClickListener {
            showEditDialog()
        }

        btnMyPosts.setOnClickListener {
            Toast.makeText(this, "내가 올린 조합 2개 보기 (준비 중)", Toast.LENGTH_SHORT).show()
        }
    }

    // 저장된 값을 화면에 표시
    private fun loadProfile() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val nickname = prefs.getString("NICKNAME", "소스장인") ?: "소스장인"
        val intro = prefs.getString("PROFILE_MSG", "고소한 조합을 사랑하는 사람") ?: ""
        showProfile(nickname, intro)
    }

    // 화면의 글자 갱신
    private fun showProfile(nickname: String, intro: String) {
        tvProfileNickname.text = nickname
        tvProfileNicknameInfo.text = nickname
        tvProfileIntro.text = intro
    }

    // 프로필 수정 팝업
    private fun showEditDialog() {
        val dp = resources.displayMetrics.density
        val pad = (20 * dp).toInt()

        val etNickname = EditText(this).apply {
            hint = "닉네임"
            setText(tvProfileNicknameInfo.text)
            setSingleLine()
        }
        val etIntro = EditText(this).apply {
            hint = "한 줄 소개"
            setText(tvProfileIntro.text)
            setSingleLine()
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
            addView(etNickname)
            addView(etIntro)
        }

        AlertDialog.Builder(this)
            .setTitle("프로필 수정")
            .setView(box)
            .setPositiveButton("저장") { _, _ ->
                val nickname = etNickname.text.toString().trim()
                val intro = etIntro.text.toString().trim()

                if (nickname.isEmpty()) {
                    Toast.makeText(this, "닉네임을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                getSharedPreferences("AppPrefs", MODE_PRIVATE).edit {
                    putString("NICKNAME", nickname)
                    putString("PROFILE_MSG", intro)
                }
                showProfile(nickname, intro)
                Toast.makeText(this, "프로필이 수정되었어요", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}