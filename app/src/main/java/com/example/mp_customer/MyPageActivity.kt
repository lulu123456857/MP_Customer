package com.example.mp_customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class MyPageActivity : AppCompatActivity() {

    // 위젯 전역 변수 선언
    lateinit var btnSetting: TextView
    lateinit var tvNickname: TextView
    lateinit var tvProfileMsg: TextView
    lateinit var btnEditProfile: TextView
    lateinit var tvCountPost: TextView
    lateinit var tvCountLike: TextView
    lateinit var tvCountSave: TextView
    lateinit var tabMyPost: TextView
    lateinit var tabLikedPost: TextView
    lateinit var tvListHeader: TextView
    lateinit var recyclerRecipe: RecyclerView

    // 하단 메뉴
    lateinit var navHome: LinearLayout
    lateinit var navSearch: LinearLayout
    lateinit var navAdd: LinearLayout
    lateinit var navSave: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mypage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. XML 위젯 연결 (findViewById)
        btnSetting = findViewById(R.id.btnSetting)
        tvNickname = findViewById(R.id.tvNickname)
        tvProfileMsg = findViewById(R.id.tvProfileMsg)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        tvCountPost = findViewById(R.id.tvCountPost)
        tvCountLike = findViewById(R.id.tvCountLike)
        tvCountSave = findViewById(R.id.tvCountSave)
        tabMyPost = findViewById(R.id.tabMyPost)
        tabLikedPost = findViewById(R.id.tabLikedPost)
        tvListHeader = findViewById(R.id.tvListHeader)
        recyclerRecipe = findViewById(R.id.recyclerRecipe)

        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navAdd = findViewById(R.id.navAdd)
        navSave = findViewById(R.id.navSave)

        // 2. 설정 아이콘 클릭 → 아래에 '로그아웃' 칸 표시
        btnSetting.setOnClickListener {
            val popup = PopupMenu(this, btnSetting)
            popup.menu.add("로그아웃")
            popup.setOnMenuItemClickListener {
                logout()
                true
            }
            popup.show()
        }

        // 3. 프로필 보기 클릭 → 내 프로필 화면으로 이동
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("nickname", tvNickname.text.toString())
            intent.putExtra("message", tvProfileMsg.text.toString())
            startActivity(intent)
        }

        // 4. 상단 탭 메뉴 전환 (내가 올린 조합 / 좋아요한 조합)
        tabMyPost.setOnClickListener {
            tabMyPost.setTextColor(Color.parseColor("#222222"))
            tabMyPost.paint.isFakeBoldText = true

            tabLikedPost.setTextColor(Color.parseColor("#888888"))
            tabLikedPost.paint.isFakeBoldText = false

            tvListHeader.text = "내가 공유한 꿀조합 2개"
            Toast.makeText(this, "내가 올린 조합 탭", Toast.LENGTH_SHORT).show()
        }

        tabLikedPost.setOnClickListener {
            tabLikedPost.setTextColor(Color.parseColor("#222222"))
            tabLikedPost.paint.isFakeBoldText = true

            tabMyPost.setTextColor(Color.parseColor("#888888"))
            tabMyPost.paint.isFakeBoldText = false

            tvListHeader.text = "좋아요한 꿀조합 목록"
            Toast.makeText(this, "좋아요한 조합 탭", Toast.LENGTH_SHORT).show()
        }

        // 5. 하단 메뉴 (마이는 현재 화면이라 동작 없음)
        navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        navSearch.setOnClickListener {
            Toast.makeText(this, "검색 화면 (준비 중)", Toast.LENGTH_SHORT).show()
        }
        navAdd.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        navSave.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
    }

    // 로그아웃: 로그인 상태 지우고 홈으로 이동
    private fun logout() {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit {
            putBoolean("IS_LOGGED_IN", false)
            remove("USER_EMAIL")
        }
        Toast.makeText(this, "로그아웃 되었어요", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}