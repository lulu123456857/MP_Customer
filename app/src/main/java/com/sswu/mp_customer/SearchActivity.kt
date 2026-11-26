package com.sswu.mp_customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// 최근 검색어 목록 (앱을 끄면 사라짐 → 나중에 SQLite로 바꿀 예정)
val recentSearches = ArrayList<String>()

class SearchActivity : AppCompatActivity() {
    // ===== 1. 위젯 변수 선언 =====
    lateinit var imgBack : ImageView
    lateinit var imgSearch : ImageView
    lateinit var editSearch : EditText
    lateinit var imgClear : ImageView
    lateinit var btnClearRecent : TextView
    lateinit var layoutRecent : LinearLayout
    lateinit var txtNoRecent : TextView
    lateinit var layoutPopular : LinearLayout
    lateinit var txtResultTitle : TextView
    lateinit var txtResultCount : TextView
    lateinit var layoutResults : LinearLayout
    lateinit var txtNoResult : TextView

    // 인기 재료 (고정)
    val popularIngredients = arrayOf("흑당펄", "참깨소스", "치즈폼", "딸기", "땅콩소스")
    var keyword = ""   // 지금 검색한 단어

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ===== 2. 위젯 대입 =====
        imgBack = findViewById<ImageView>(R.id.imgBack)
        imgSearch = findViewById<ImageView>(R.id.imgSearch)
        editSearch = findViewById<EditText>(R.id.editSearch)
        imgClear = findViewById<ImageView>(R.id.imgClear)
        btnClearRecent = findViewById<TextView>(R.id.btnClearRecent)
        layoutRecent = findViewById<LinearLayout>(R.id.layoutRecent)
        txtNoRecent = findViewById<TextView>(R.id.txtNoRecent)
        layoutPopular = findViewById<LinearLayout>(R.id.layoutPopular)
        txtResultTitle = findViewById<TextView>(R.id.txtResultTitle)
        txtResultCount = findViewById<TextView>(R.id.txtResultCount)
        layoutResults = findViewById<LinearLayout>(R.id.layoutResults)
        txtNoResult = findViewById<TextView>(R.id.txtNoResult)

        // ===== 3. 클릭 동작 =====
        imgBack.setOnClickListener {
            finish()
        }

        // 돋보기 누르면 검색
        imgSearch.setOnClickListener {
            search(editSearch.text.toString())
        }

        // 키보드의 '검색' 키를 눌러도 검색
        editSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(editSearch.text.toString())
                true    // 이 키 입력은 처리했다는 뜻
            } else {
                false
            }
        }

        // X 누르면 입력 지우고 전체 보기
        imgClear.setOnClickListener {
            editSearch.setText("")
            search("")
        }

        // 최근 검색어 전체 삭제 (7장 대화상자)
        btnClearRecent.setOnClickListener {
            if (recentSearches.size == 0) {
                Toast.makeText(applicationContext, "지울 검색어가 없어요", Toast.LENGTH_SHORT).show()
            } else {
                val dlg = AlertDialog.Builder(this@SearchActivity)
                dlg.setTitle("최근 검색어")
                dlg.setMessage("최근 검색어를 모두 지울까요?")
                dlg.setPositiveButton("삭제") { dialog, which ->
                    recentSearches.clear()
                    showRecent()
                }
                dlg.setNegativeButton("취소", null)
                dlg.show()
            }
        }

        // 처음 화면: 최근 검색어, 인기 재료, 전체 레시피 보여주기
        showRecent()
        showPopular()
        search("")
    }

    // 다른 화면(상세)에서 저장을 바꾸고 돌아오면 북마크 모양 맞추기 (10장 생명주기)
    override fun onResume() {
        super.onResume()
        showResults()
    }

    // ===== 검색하기 =====
    fun search(word: String) {
        keyword = word.trim()      // 앞뒤 공백 제거
        editSearch.setText(keyword)
        editSearch.setSelection(keyword.length)   // 커서를 글자 끝으로

        // 검색어가 있으면 최근 검색어 맨 앞에 추가 (같은 단어는 중복 없이, 최대 5개)
        if (keyword.isNotEmpty()) {
            recentSearches.remove(keyword)
            recentSearches.add(0, keyword)
            if (recentSearches.size > 5) {
                recentSearches.removeAt(recentSearches.size - 1)
            }
        }

        // 키보드 내리기
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editSearch.windowToken, 0)

        showRecent()
        showPopular()
        showResults()
    }

    // 레시피가 검색어에 맞는지 확인: 이름 또는 재료 중 하나라도 검색어를 포함하면 OK
    fun isMatch(recipe: Recipe): Boolean {
        if (keyword.isEmpty()) return true
        if (recipe.name.contains(keyword)) return true
        for (item in recipe.ingredients) {
            if (item.contains(keyword)) return true
        }
        return false
    }

    // ===== 검색 결과 카드 보여주기 =====
    fun showResults() {
        layoutResults.removeAllViews()

        // 기본 레시피 + 내가 등록한 레시피를 모두 검사
        val allRecipes = ArrayList<Recipe>()
        for (r in recipeList) allRecipes.add(r)
        for (r in userRecipes) allRecipes.add(r)

        var count = 0
        for (r in allRecipes) {
            if (isMatch(r)) {
                addCard(r)
                count = count + 1
            }
        }

        if (keyword.isEmpty()) {
            txtResultTitle.text = "전체 조합"
        } else {
            txtResultTitle.text = "'" + keyword + "' 검색 결과"
        }
        txtResultCount.text = count.toString() + "개 조합"

        if (count == 0) {
            txtNoResult.visibility = View.VISIBLE
        } else {
            txtNoResult.visibility = View.GONE
        }
    }

    // 카드 XML(item_recipe_card.xml) 불러와서 붙이기 (7장 View.inflate)
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
            if (!isLoggedIn()) {
                Toast.makeText(applicationContext, "로그인 후 사용 가능합니다", Toast.LENGTH_SHORT).show()
            } else if (savedRecipes.contains(recipe)) {
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

        layoutResults.addView(card)
    }

    // ===== 최근 검색어 칩 만들기 (5장: Kotlin 코드로 화면 만들기) =====
    // [흑당펄  ✕] 모양: 단어를 누르면 다시 검색, ✕를 누르면 그 검색어만 삭제
    fun showRecent() {
        layoutRecent.removeAllViews()

        for (word in recentSearches) {
            val chip = LinearLayout(this)
            chip.orientation = LinearLayout.HORIZONTAL
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setPadding(dp(14), dp(8), dp(10), dp(8))
            val chipParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            chipParams.rightMargin = dp(8)
            chip.layoutParams = chipParams

            val txtWord = TextView(this)
            txtWord.text = word
            txtWord.textSize = 14f
            txtWord.setTextColor(Color.parseColor("#555555"))
            txtWord.setOnClickListener {
                search(word)
            }

            val txtDelete = TextView(this)
            txtDelete.text = "  ✕"
            txtDelete.textSize = 13f
            txtDelete.setTextColor(Color.parseColor("#999999"))
            txtDelete.setOnClickListener {
                recentSearches.remove(word)
                showRecent()
            }

            chip.addView(txtWord)
            chip.addView(txtDelete)
            layoutRecent.addView(chip)
        }

        // 최근 검색어가 없으면 안내 문구
        if (recentSearches.size == 0) {
            txtNoRecent.visibility = View.VISIBLE
        } else {
            txtNoRecent.visibility = View.GONE
        }
    }

    // ===== 인기 재료 칩 만들기: 누르면 그 재료로 검색 =====
    fun showPopular() {
        layoutPopular.removeAllViews()

        for (item in popularIngredients) {
            val chip = TextView(this)
            chip.textSize = 14f
            chip.setPadding(dp(16), dp(8), dp(16), dp(8))
            val chipParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            chipParams.rightMargin = dp(8)
            chip.layoutParams = chipParams

            // 지금 검색어와 같은 재료면 선택된 모양
            if (item == keyword) {
                chip.text = "✓ " + item
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(Color.parseColor("#7A5200"))
            } else {
                chip.text = item
                chip.setBackgroundResource(R.drawable.bg_chip)
                chip.setTextColor(Color.parseColor("#555555"))
            }

            chip.setOnClickListener {
                search(item)
            }
            layoutPopular.addView(chip)
        }
    }

    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
