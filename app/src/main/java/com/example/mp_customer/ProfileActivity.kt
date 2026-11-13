package com.example.mp_customer

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        // 마이페이지에서 넘겨준 닉네임/소개 표시
        val nickname = intent.getStringExtra("nickname") ?: "소스장인"
        tvProfileNickname.text = nickname
        tvProfileNicknameInfo.text = nickname
        tvProfileIntro.text = intent.getStringExtra("message") ?: "고소한 조합을 사랑하는 사람"

        // 뒤로가기 → 마이페이지로 돌아가기
        btnBack.setOnClickListener {
            finish()
        }

        btnEditProfileFull.setOnClickListener {
            Toast.makeText(this, "프로필 수정 기능은 준비 중이에요", Toast.LENGTH_SHORT).show()
        }

        btnMyPosts.setOnClickListener {
            Toast.makeText(this, "내가 올린 조합 2개 보기 (준비 중)", Toast.LENGTH_SHORT).show()
        }
    }
}