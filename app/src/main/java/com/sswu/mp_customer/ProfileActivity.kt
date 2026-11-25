package com.sswu.mp_customer

import android.os.Bundle
import android.view.View
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
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    lateinit var btnBack: TextView
    lateinit var btnEditProfileFull: TextView
    lateinit var tvProfileNickname: TextView
    lateinit var tvProfileNicknameInfo: TextView
    lateinit var tvProfileIntro: TextView
    lateinit var btnMyPosts: TextView

    // [추가] 통계 숫자 + 취향 칩
    lateinit var tvProfileCountPost: TextView
    lateinit var tvProfileCountLike: TextView
    lateinit var tvProfileCountSave: TextView
    lateinit var tvTaste1: TextView
    lateinit var tvTaste2: TextView
    lateinit var tvTaste3: TextView

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
        tvProfileCountPost = findViewById(R.id.tvProfileCountPost)
        tvProfileCountLike = findViewById(R.id.tvProfileCountLike)
        tvProfileCountSave = findViewById(R.id.tvProfileCountSave)
        tvTaste1 = findViewById(R.id.tvTaste1)
        tvTaste2 = findViewById(R.id.tvTaste2)
        tvTaste3 = findViewById(R.id.tvTaste3)

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

        // [수정] 내가 올린 조합 보기 → 마이페이지로 돌아가기 (마이페이지 '내가 올린 조합' 탭에서 목록 확인)
        btnMyPosts.setOnClickListener {
            finish()
        }
    }

    // [추가] 화면이 보일 때마다 숫자·취향 새로 계산 (10장 생명주기)
    override fun onResume() {
        super.onResume()
        updateStats()
    }

    // [추가] 마이페이지와 같은 방식으로 올린 조합 / 받은 좋아요 / 저장한 조합 계산
    private fun updateStats() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val myRecipes = ArrayList<Recipe>()
        for (r in userRecipes) {
            if (r.authorUid == currentUid) myRecipes.add(r)
        }

        tvProfileCountPost.text = myRecipes.size.toString()
        var totalLikes = 0
        for (r in myRecipes) {
            totalLikes = totalLikes + r.likes
        }
        tvProfileCountLike.text = totalLikes.toString()
        tvProfileCountSave.text = savedRecipes.size.toString()

        btnMyPosts.text = "내가 올린 조합 " + myRecipes.size + "개 보기   >"
        showTastes(myRecipes)
    }

    // [추가] 내가 올린 레시피에서 많이 나온 맛 태그 최대 3개를 칩으로 보여주기
    private fun showTastes(myRecipes: ArrayList<Recipe>) {
        val tasteNames = arrayOf("달달한 맛", "상큼한 맛", "고소한 맛", "매운맛", "초코")
        val counts = IntArray(tasteNames.size)
        for (r in myRecipes) {
            for (i in tasteNames.indices) {
                if (r.tastes.contains(tasteNames[i])) counts[i] = counts[i] + 1
            }
        }

        val chips = arrayOf(tvTaste1, tvTaste2, tvTaste3)
        for (chip in chips) chip.visibility = View.GONE

        // 가장 많이 나온 맛부터 하나씩 골라서 칩에 넣기
        var shown = 0
        while (shown < chips.size) {
            var best = -1
            for (i in tasteNames.indices) {
                if (counts[i] > 0 && (best == -1 || counts[i] > counts[best])) best = i
            }
            if (best == -1) break
            chips[shown].text = tasteNames[best]
            chips[shown].visibility = View.VISIBLE
            counts[best] = 0      // 다음 번엔 다른 맛을 고르도록
            shown = shown + 1
        }

        // 올린 레시피가 없으면 안내 문구 하나만
        if (shown == 0) {
            tvTaste1.text = "아직 공유한 조합이 없어요"
            tvTaste1.visibility = View.VISIBLE
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