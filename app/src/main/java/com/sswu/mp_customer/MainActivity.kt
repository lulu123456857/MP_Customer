package com.sswu.mp_customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

private var isAppStarted = false

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
    var selectedBrand = "전체"
    var sortType = "인기순"
    val tasteNames = arrayOf("달달한 맛", "상큼한 맛", "고소한 맛", "매운맛", "초코")
    val tasteSelected = BooleanArray(5)

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

        if (!isAppStarted) {
            isAppStarted = true
            val startPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val keepLogin = startPrefs.getBoolean("KEEP_LOGIN", false)
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (!keepLogin || firebaseUser == null) {
                FirebaseAuth.getInstance().signOut()
                startPrefs.edit {
                    putBoolean("IS_LOGGED_IN", false)
                    putBoolean("KEEP_LOGIN", false)
                    remove("USER_EMAIL")
                }
            }
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ===== 2. 변수에 위젯 대입 (findViewById) =====
        imgBell = findViewById(R.id.imgBell)
        bannerRandom = findViewById(R.id.bannerRandom)

        chipAll = findViewById(R.id.chipAll)
        chipAmasvin = findViewById(R.id.chipAmasvin)
        chipGongcha = findViewById(R.id.chipGongcha)
        chipHaidilao = findViewById(R.id.chipHaidilao)
        chipMore = findViewById(R.id.chipMore)

        chipSweet = findViewById(R.id.chipSweet)
        chipSour = findViewById(R.id.chipSour)
        chipNutty = findViewById(R.id.chipNutty)
        chipSpicy = findViewById(R.id.chipSpicy)
        chipChoco = findViewById(R.id.chipChoco)

        tabPopular = findViewById(R.id.tabPopular)
        tabRecent = findViewById(R.id.tabRecent)

        layoutHomeList = findViewById(R.id.layoutHomeList)
        txtHomeEmpty = findViewById(R.id.txtHomeEmpty)

        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navAdd = findViewById(R.id.navAdd)
        navSave = findViewById(R.id.navSave)
        navMy = findViewById(R.id.navMy)

        // ===== 3. 클릭했을 때 동작 =====
        imgBell.setOnClickListener {
            Toast.makeText(applicationContext, "새 알림이 없어요", Toast.LENGTH_SHORT).show()
        }
        // 랜덤 추천: 전체 레시피 중 하나를 무작위로 골라 상세 화면으로
        bannerRandom.setOnClickListener {
            val allRecipes = ArrayList<Recipe>()
            for (r in recipeList) allRecipes.add(r)
            for (r in userRecipes) allRecipes.add(r)

            if (allRecipes.size == 0) {
                Toast.makeText(applicationContext, "추천할 레시피가 없어요", Toast.LENGTH_SHORT).show()
            } else {
                // 0 이상 1 미만의 난수 × 레시피 수 → 0 ~ (개수-1) 중 하나
                val pick = allRecipes[(Math.random() * allRecipes.size).toInt()]
                Toast.makeText(applicationContext, "오늘의 꿀조합: " + pick.name, Toast.LENGTH_SHORT).show()

                val intent = Intent(applicationContext, DetailActivity::class.java)
                val baseIndex = recipeList.indexOf(pick)
                if (baseIndex >= 0) {
                    intent.putExtra("index", baseIndex)
                    intent.putExtra("isUser", false)
                } else {
                    intent.putExtra("index", userRecipes.indexOf(pick))
                    intent.putExtra("isUser", true)
                }
                startActivity(intent)
            }
        }

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

        val tasteChips = arrayOf(chipSweet, chipSour, chipNutty, chipSpicy, chipChoco)
        for (i in tasteChips.indices) {
            tasteChips[i].setOnClickListener {
                tasteSelected[i] = !tasteSelected[i]
                setChipSelected(tasteChips[i], tasteSelected[i])
                showHomeList()
            }
        }

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

        navSearch.setOnClickListener {
            startActivity(Intent(applicationContext, SearchActivity::class.java))
        }
        navAdd.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
        }
        navSave.setOnClickListener {
            startActivity(Intent(applicationContext, SavedActivity::class.java))
        }

        navMy.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

            if (isLoggedIn) {
                startActivity(Intent(this, MyPageActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    // [변경] 홈 화면 진입·복귀 시 Firestore에서 최신 레시피와 내 북마크/좋아요 동기화 후 화면 그리기
    override fun onResume() {
        super.onResume()
        showHomeList()
        syncFirestoreData {
            showHomeList()
        }
    }

    fun isMatch(recipe: Recipe): Boolean {
        if (selectedBrand != "전체" && recipe.brand != selectedBrand) {
            return false
        }
        val text = recipe.tastes + " " + recipe.name
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

    fun showHomeList() {
        layoutHomeList.removeAllViews()

        val showList = ArrayList<Recipe>()
        for (i in userRecipes.indices.reversed()) {
            if (isMatch(userRecipes[i])) showList.add(userRecipes[i])
        }
        for (i in recipeList.indices.reversed()) {
            if (isMatch(recipeList[i])) showList.add(recipeList[i])
        }

        if (sortType == "인기순") {
            showList.sortByDescending { it.likes }
        }

        for (recipe in showList) {
            addCard(recipe)
        }

        if (showList.size == 0) {
            txtHomeEmpty.visibility = View.VISIBLE
        } else {
            txtHomeEmpty.visibility = View.GONE
        }
    }

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

        // [변경] 북마크 토글 시 로컬 목록과 함께 Firestore users/{uid}에도 저장
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
                updateBookmarkInFirestore(recipe, false)
            } else {
                savedRecipes.add(recipe)
                imgCardSave.setImageResource(R.drawable.ic_bookmark)
                updateBookmarkInFirestore(recipe, true)
                Toast.makeText(applicationContext, "보관함에 저장했어요", Toast.LENGTH_SHORT).show()
            }
        }

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

    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

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