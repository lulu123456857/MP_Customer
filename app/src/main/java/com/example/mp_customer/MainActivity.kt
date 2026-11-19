package com.example.mp_customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.view.View
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    // ===== 1. 위젯 변수를 전역변수로 선언 (lateinit) =====
    lateinit var imgBell : ImageView
    lateinit var bannerRandom : LinearLayout

    // 브랜드 필터
    lateinit var chipAll : TextView
    lateinit var chipAmasvin : TextView
    lateinit var chipGongcha : TextView
    lateinit var chipHaidilao : TextView
    lateinit var chipMore : TextView

    // 맛 필터
    lateinit var chipSweet : TextView
    lateinit var chipSour : TextView
    lateinit var chipNutty : TextView
    lateinit var chipSpicy : TextView
    lateinit var chipChoco : TextView

    // 정렬
    lateinit var tabPopular : TextView
    lateinit var tabRecent : TextView

    // 레시피 목록이 들어갈 칸
    lateinit var layoutHomeList : LinearLayout
    lateinit var txtHomeEmpty : TextView

    // 지금 선택된 필터·정렬 상태
    var selectedBrand = "전체"          // 선택한 브랜드
    var sortType = "인기순"              // "인기순" 또는 "최신순"
    val tasteNames = arrayOf("달달한 맛", "상큼한 맛", "고소한 맛", "매운맛", "초코")
    val tasteSelected = BooleanArray(5)     // 맛 필터마다 켜져 있는지

    // 맛 버튼마다 "같은 뜻으로 볼 단어들"
    val tasteKeywords = arrayOf(
        arrayOf("달달", "달콤"),
        arrayOf("상큼", "새콤", "산뜻"),
        arrayOf("고소"),
        arrayOf("매운", "매콤", "마라", "얼얼"),
        arrayOf("초코", "초콜릿")
    )

    // 하단 메뉴
    lateinit var navHome : LinearLayout
    lateinit var navSearch : LinearLayout
    lateinit var navAdd : LinearLayout
    lateinit var navSave : LinearLayout
    lateinit var navMy : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ===== 2. 변수에 위젯 대입 (findViewById) =====
        imgBell = findViewById<ImageView>(R.id.imgBell)
        bannerRandom = findViewById<LinearLayout>(R.id.bannerRandom)

        chipAll = findViewById<TextView>(R.id.chipAll)
        chipAmasvin = findViewById<TextView>(R.id.chipAmasvin)
        chipGongcha = findViewById<TextView>(R.id.chipGongcha)
        chipHaidilao = findViewById<TextView>(R.id.chipHaidilao)
        chipMore = findViewById<TextView>(R.id.chipMore)

        chipSweet = findViewById<TextView>(R.id.chipSweet)
        chipSour = findViewById<TextView>(R.id.chipSour)
        chipNutty = findViewById<TextView>(R.id.chipNutty)
        chipSpicy = findViewById<TextView>(R.id.chipSpicy)
        chipChoco = findViewById<TextView>(R.id.chipChoco)

        tabPopular = findViewById<TextView>(R.id.tabPopular)
        tabRecent = findViewById<TextView>(R.id.tabRecent)

        layoutHomeList = findViewById<LinearLayout>(R.id.layoutHomeList)
        txtHomeEmpty = findViewById<TextView>(R.id.txtHomeEmpty)

        navHome = findViewById<LinearLayout>(R.id.navHome)
        navSearch = findViewById<LinearLayout>(R.id.navSearch)
        navAdd = findViewById<LinearLayout>(R.id.navAdd)
        navSave = findViewById<LinearLayout>(R.id.navSave)
        navMy = findViewById<LinearLayout>(R.id.navMy)

        // ===== 3. 클릭했을 때 동작 (setOnClickListener 람다식) =====

        // 알림, 랜덤 추천: 아직 기능이 없으니 토스트만
        imgBell.setOnClickListener {
            Toast.makeText(applicationContext, "새 알림이 없어요", Toast.LENGTH_SHORT).show()
        }
        bannerRandom.setOnClickListener {
            Toast.makeText(applicationContext, "랜덤 추천은 준비 중이에요", Toast.LENGTH_SHORT).show()
        }

        // 브랜드 필터: 하나만 선택, 고른 브랜드로 목록 다시 그리기
        val brandChips = arrayOf(chipAll, chipAmasvin, chipGongcha, chipHaidilao, chipMore)
        val brandNames = arrayOf("전체", "아마스빈", "공차", "하이디라오", "더보기")
        for (i in brandChips.indices) {
            brandChips[i].setOnClickListener {
                if (brandNames[i] == "더보기") {
                    Toast.makeText(applicationContext, "다른 브랜드는 준비 중이에요", Toast.LENGTH_SHORT).show()
                } else {
                    for (c in brandChips) {
                        setChipSelected(c, false)
                    }
                    setChipSelected(brandChips[i], true)
                    selectedBrand = brandNames[i]
                    showHomeList()
                }
            }
        }

        // 맛 필터: 여러 개 선택, 누를 때마다 목록 다시 그리기
        val tasteChips = arrayOf(chipSweet, chipSour, chipNutty, chipSpicy, chipChoco)
        for (i in tasteChips.indices) {
            tasteChips[i].setOnClickListener {
                tasteSelected[i] = !tasteSelected[i]
                setChipSelected(tasteChips[i], tasteSelected[i])
                showHomeList()
            }
        }

        // 정렬 탭: 고른 쪽을 진하게, 그 순서로 목록 다시 그리기
        tabPopular.setOnClickListener {
            tabPopular.setTextColor(Color.parseColor("#222222"))
            tabRecent.setTextColor(Color.parseColor("#888888"))
            sortType = "인기순"
            showHomeList()
        }
        tabRecent.setOnClickListener {
            tabRecent.setTextColor(Color.parseColor("#222222"))
            tabPopular.setTextColor(Color.parseColor("#888888"))
            sortType = "최신순"
            showHomeList()
        }

        // 하단 메뉴: 화면 전환 연결
        navSearch.setOnClickListener {
            val intent = Intent(applicationContext, SearchActivity::class.java)
            startActivity(intent)
        }
        navAdd.setOnClickListener {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
        }
        navSave.setOnClickListener {
            val intent = Intent(applicationContext, SavedActivity::class.java)
            startActivity(intent)
        }

        // [마이페이지 / 로그인 화면 분기 처리]
        navMy.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

            if (isLoggedIn) {
                // 로그인된 상태라면 마이페이지(MyPageActivity)로 이동
                val intent = Intent(this, MyPageActivity::class.java)
                startActivity(intent)
            } else {
                // 로그인되지 않은 상태라면 로그인 화면(LoginActivity)으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 화면이 다시 보일 때마다 목록 새로 그리기 (10장 생명주기)
    // 예: 상세 화면에서 좋아요·저장을 바꾸고 돌아오면 바로 반영됨
    override fun onResume() {
        super.onResume()
        showHomeList()
    }

    // 레시피가 지금 고른 필터에 맞는지 검사
    fun isMatch(recipe: Recipe): Boolean {
        // 1. 브랜드: "전체"가 아니면 같은 브랜드만 통과
        if (selectedBrand != "전체" && recipe.brand != selectedBrand) {
            return false
        }
        // 2. 맛: 하나도 안 골랐으면 통과, 골랐으면 그중 하나라도 비슷한 말이 있어야 통과
        val text = recipe.tastes + " " + recipe.name   // 이름에 초콜릿이 든 음료도 잡기 위해 이름도 같이 검사
        var anySelected = false
        for (i in tasteNames.indices) {
            if (tasteSelected[i]) {
                anySelected = true
                for (word in tasteKeywords[i]) {
                    if (text.contains(word)) {
                        return true
                    }
                }
            }
        }
        return !anySelected
    }

    // 홈 레시피 목록 그리기
    fun showHomeList() {
        layoutHomeList.removeAllViews()   // 전에 붙인 카드 지우기

        // 최신순 목록 만들기: 내가 등록한 레시피(최근 것부터) → 기본 레시피(뒤에 추가된 것부터)
        val showList = ArrayList<Recipe>()
        for (i in userRecipes.indices.reversed()) {
            if (isMatch(userRecipes[i])) showList.add(userRecipes[i])
        }
        for (i in recipeList.indices.reversed()) {
            if (isMatch(recipeList[i])) showList.add(recipeList[i])
        }

        // 인기순이면 좋아요 많은 순으로 다시 정렬
        if (sortType == "인기순") {
            showList.sortByDescending { it.likes }
        }

        for (recipe in showList) {
            addCard(recipe)
        }

        // 보여줄 레시피가 없으면 안내 문구
        if (showList.size == 0) {
            txtHomeEmpty.visibility = View.VISIBLE
        } else {
            txtHomeEmpty.visibility = View.GONE
        }
    }

    // 카드 XML(item_recipe_card.xml) 불러와서 내용 채우고 붙이기 (7장 View.inflate)
    fun addCard(recipe: Recipe) {
        val card = View.inflate(this, R.layout.item_recipe_card, null)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = dp(12)
        card.layoutParams = params
        setRecipeImage(card.findViewById(R.id.imgCard), recipe)

        card.findViewById<TextView>(R.id.txtCardBrand).text = recipe.brand
        card.findViewById<TextView>(R.id.txtCardName).text = recipe.name
        card.findViewById<TextView>(R.id.txtCardTastes).text = recipe.tastes
        card.findViewById<TextView>(R.id.txtCardLikes).text = recipe.likes.toString()

        // 북마크: 저장 상태에 맞게 보여주고, 누르면 보관함에 넣거나 빼기
        val imgCardSave = card.findViewById<ImageView>(R.id.imgCardSave)
        if (savedRecipes.contains(recipe)) {
            imgCardSave.setImageResource(R.drawable.ic_bookmark)
        } else {
            imgCardSave.setImageResource(R.drawable.ic_bookmark_border)
        }
        imgCardSave.setOnClickListener {
            if (savedRecipes.contains(recipe)) {
                savedRecipes.remove(recipe)
                imgCardSave.setImageResource(R.drawable.ic_bookmark_border)
            } else {
                savedRecipes.add(recipe)
                imgCardSave.setImageResource(R.drawable.ic_bookmark)
                Toast.makeText(applicationContext, "보관함에 저장했어요", Toast.LENGTH_SHORT).show()
            }
        }

        // 카드 누르면 상세 화면으로 (10장 인텐트)
        card.setOnClickListener {
            val intent = Intent(applicationContext, DetailActivity::class.java)
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

        layoutHomeList.addView(card)
    }

    // dp → 픽셀
    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // 칩(필터 버튼)의 선택/해제 모양을 바꿔주는 함수
    fun setChipSelected(chip : TextView, selected : Boolean) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(Color.parseColor("#7A5200"))
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(Color.parseColor("#555555"))
        }
    }
}