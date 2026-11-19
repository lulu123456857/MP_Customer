package com.sswu.mp_customer

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MyPageActivity : AppCompatActivity() {

    // 위젯 전역 변수 선언
    lateinit var btnSetting: TextView
    lateinit var tvNickname: TextView
    lateinit var tvProfileMsg: TextView
    lateinit var btnEditProfile: TextView
    lateinit var tvCountPost: TextView
    lateinit var tvCountLike: TextView
    lateinit var tvCountSave: TextView
    lateinit var statSave: LinearLayout
    lateinit var tabMyPost: TextView
    lateinit var tabLikedPost: TextView
    lateinit var tvListHeader: TextView
    lateinit var recyclerRecipe: RecyclerView

    // 리스트 어댑터
    lateinit var adapter: RecipeAdapter

    // 하단 메뉴
    lateinit var navHome: LinearLayout
    lateinit var navSearch: LinearLayout
    lateinit var navAdd: LinearLayout
    lateinit var navSave: LinearLayout

    // [수정] 지금 목록에 보이는 레시피들 (카드를 누르면 여기서 찾아서 상세 화면으로 보냄)
    var currentList = ArrayList<Recipe>()
    var showingMyPosts = true   // 지금 '내가 올린 조합' 탭인지

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
        statSave = findViewById(R.id.statSave)
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
            Toast.makeText(this, "설정 눌림", Toast.LENGTH_SHORT).show()   // 확인용 (나중에 지워도 됨)
            val popup = PopupMenu(this, btnSetting)
            popup.menu.add("로그아웃")
            popup.setOnMenuItemClickListener {
                logout()
                true
            }
            popup.show()
        }

        // 3. 프로필 보기 클릭 → 내 프로필 화면으로 이동 (수정은 프로필 화면에서)
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("nickname", tvNickname.text.toString())
            intent.putExtra("message", tvProfileMsg.text.toString())
            startActivity(intent)
        }

        // 4. 리스트 연결 (RecyclerView + 어댑터 + 레이아웃 매니저)
        // [수정] 카드를 누르면, 카드 번호(id)로 실제 레시피를 찾아 상세 화면으로 이동
        adapter = RecipeAdapter(emptyList()) { card ->
            openDetail(currentList[card.id])
        }
        recyclerRecipe.layoutManager = LinearLayoutManager(this)
        recyclerRecipe.adapter = adapter

        // 5. 상단 탭 메뉴 전환 (내가 올린 조합 / 좋아요한 조합)
        tabMyPost.setOnClickListener {
            showingMyPosts = true
            showMyPosts()
        }
        tabLikedPost.setOnClickListener {
            showingMyPosts = false
            showLikedPosts()
        }

        // 6. 통계의 '저장한 조합' 클릭 → 저장(보관함) 화면으로 이동
        statSave.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        // 7. 하단 메뉴 (마이는 현재 화면이라 동작 없음)
        navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        navSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))   // [수정] 검색 화면 연결
        }
        navAdd.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        navSave.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
    }

    // [수정] 화면이 다시 보일 때마다 프로필·숫자·목록을 새로 그리기 (10장 생명주기)
    // 예: 보관함에서 저장을 바꾸고 돌아오면 '저장한 조합' 숫자가 바로 바뀜
    override fun onResume() {
        super.onResume()
        loadProfile()
        updateStats()
        if (showingMyPosts) {
            showMyPosts()
        } else {
            showLikedPosts()
        }
    }

    // 저장된 닉네임/소개글을 화면에 표시 (없으면 기본값 유지)
    private fun loadProfile() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        tvNickname.text = prefs.getString("NICKNAME", "소스장인")
        tvProfileMsg.text = prefs.getString("PROFILE_MSG", "고소한 조합을 사랑하는 사람")
    }

    // [추가] 올린 조합 / 받은 좋아요 / 저장한 조합 숫자를 실제 데이터로 계산
    private fun updateStats() {
        // 올린 조합 = 등록 화면에서 내가 등록한 레시피 수
        tvCountPost.text = userRecipes.size.toString()

        // 받은 좋아요 = 내가 올린 레시피들의 좋아요 수를 모두 더한 값
        var totalLikes = 0
        for (r in userRecipes) {
            totalLikes = totalLikes + r.likes
        }
        tvCountLike.text = totalLikes.toString()

        // 저장한 조합 = 보관함에 넣은 레시피 수
        tvCountSave.text = savedRecipes.size.toString()
    }

    // '내가 올린 조합' 탭 표시
    private fun showMyPosts() {
        tabMyPost.setTextColor(Color.parseColor("#222222"))
        tabMyPost.paint.isFakeBoldText = true
        tabLikedPost.setTextColor(Color.parseColor("#888888"))
        tabLikedPost.paint.isFakeBoldText = false

        // [수정] 가짜 데이터 대신 내가 등록한 레시피 (최근 것부터)
        currentList = ArrayList()
        for (i in userRecipes.indices.reversed()) {
            currentList.add(userRecipes[i])
        }
        tvListHeader.text =
            if (currentList.isEmpty()) "아직 공유한 꿀조합이 없어요" else "내가 공유한 꿀조합 ${currentList.size}개"
        adapter.submit(toCards(currentList))
    }

    // '좋아요한 조합' 탭 표시
    private fun showLikedPosts() {
        tabLikedPost.setTextColor(Color.parseColor("#222222"))
        tabLikedPost.paint.isFakeBoldText = true
        tabMyPost.setTextColor(Color.parseColor("#888888"))
        tabMyPost.paint.isFakeBoldText = false

        // [수정] 상세 화면에서 좋아요를 누른 레시피 (최근 것부터)
        currentList = ArrayList()
        for (i in likedRecipes.indices.reversed()) {
            currentList.add(likedRecipes[i])
        }
        tvListHeader.text =
            if (currentList.isEmpty()) "아직 좋아요한 꿀조합이 없어요" else "좋아요한 꿀조합 ${currentList.size}개"
        adapter.submit(toCards(currentList))
    }

    // [추가] Recipe 목록을 어댑터용 RecipeCard 목록으로 바꾸기
    // id에는 목록 안의 순서(0, 1, 2...)를 넣어서, 카드를 누르면 currentList에서 다시 찾을 수 있게 함
    private fun toCards(list: List<Recipe>): List<RecipeCard> {
        val cards = ArrayList<RecipeCard>()
        for (i in list.indices) {
            val r = list[i]
            cards.add(RecipeCard(i, r.name, r.brand, r.tastes, r.likes, savedRecipes.contains(r)))
        }
        return cards
    }

    // [추가] 레시피가 기본 레시피인지, 내가 등록한 레시피인지 찾아서 상세 화면 열기
    private fun openDetail(recipe: Recipe) {
        val intent = Intent(this, DetailActivity::class.java)
        val baseIndex = recipeList.indexOf(recipe)
        if (baseIndex >= 0) {
            intent.putExtra("index", baseIndex)
            intent.putExtra("isUser", false)
        } else {
            intent.putExtra("index", userRecipes.indexOf(recipe))
            intent.putExtra("isUser", true)
        }
        startActivity(intent)
    }

    // 로그아웃: 로그인 상태 지우고 홈으로 이동
    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit {
            putBoolean("IS_LOGGED_IN", false)
            remove("USER_EMAIL")
        }
        // [추가] 이전 사용자의 기록 지우기 (다음 사람이 로그인하면 0부터 시작)
        savedRecipes.clear()
        likedRecipes.clear()
        userRecipes.clear()

        Toast.makeText(this, "로그아웃 되었어요", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}