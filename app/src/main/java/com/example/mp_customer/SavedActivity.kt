package com.example.mp_customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// 저장한 레시피 목록 (홈·상세 화면의 저장 버튼이 여기에 넣고 뺌)
// 앱을 끄면 사라짐 → 나중에 SQLite로 바꿀 예정
val savedRecipes = ArrayList<Recipe>()

class SavedActivity : AppCompatActivity() {
    // ===== 1. 위젯 변수 선언 =====
    lateinit var imgBack : ImageView
    lateinit var imgMore : ImageView
    lateinit var txtSavedCount : TextView
    lateinit var chipAll : TextView
    lateinit var chipGongcha : TextView
    lateinit var chipAmasvin : TextView
    lateinit var chipHaidilao : TextView
    lateinit var layoutSavedList : LinearLayout
    lateinit var txtEmpty : TextView

    var filterBrand = "전체"   // 지금 선택한 브랜드 필터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ===== 2. 위젯 대입 =====
        imgBack = findViewById<ImageView>(R.id.imgBack)
        imgMore = findViewById<ImageView>(R.id.imgMore)
        txtSavedCount = findViewById<TextView>(R.id.txtSavedCount)
        chipAll = findViewById<TextView>(R.id.chipAll)
        chipGongcha = findViewById<TextView>(R.id.chipGongcha)
        chipAmasvin = findViewById<TextView>(R.id.chipAmasvin)
        chipHaidilao = findViewById<TextView>(R.id.chipHaidilao)
        layoutSavedList = findViewById<LinearLayout>(R.id.layoutSavedList)
        txtEmpty = findViewById<TextView>(R.id.txtEmpty)

        // ===== 3. 클릭 동작 =====
        imgBack.setOnClickListener {
            finish()
        }

        // ⋮ 버튼: 컨텍스트 메뉴 (7장)
        registerForContextMenu(imgMore)
        imgMore.setOnClickListener {
            openContextMenu(imgMore)
        }

        // 브랜드 필터: 누른 브랜드의 레시피만 보여주기
        val brandChips = arrayOf(chipAll, chipGongcha, chipAmasvin, chipHaidilao)
        val brandNames = arrayOf("전체", "공차", "아마스빈", "하이디라오")
        for (i in brandChips.indices) {
            brandChips[i].setOnClickListener {
                filterBrand = brandNames[i]
                for (c in brandChips) {
                    setChipSelected(c, false)
                }
                setChipSelected(brandChips[i], true)
                showList()
            }
        }
    }

    // 화면이 다시 보일 때마다 목록 새로 그리기 (10장 액티비티 생명주기)
    // 예: 상세 화면에서 저장을 취소하고 돌아오면 목록에서도 빠져 있어야 하니까
    override fun onResume() {
        super.onResume()
        showList()
    }

    // 저장한 레시피를 카드로 만들어서 보여주기
    fun showList() {
        layoutSavedList.removeAllViews()
        txtSavedCount.text = "저장한 레시피 " + savedRecipes.size + "개"

        var shownCount = 0
        // 최근에 저장한 것(목록의 뒤쪽)부터 보여주기
        for (i in savedRecipes.indices.reversed()) {
            val recipe = savedRecipes[i]
            if (filterBrand != "전체" && recipe.brand != filterBrand) {
                continue   // 선택한 브랜드가 아니면 건너뛰기
            }
            addCard(recipe)
            shownCount = shownCount + 1
        }

        // 보여줄 카드가 없으면 안내 문구 보이기
        if (shownCount == 0) {
            if (savedRecipes.size == 0) {
                txtEmpty.text = "아직 저장한 레시피가 없어요\n홈이나 상세 화면에서 북마크를 눌러보세요"
            } else {
                txtEmpty.text = filterBrand + " 레시피는 아직 저장하지 않았어요"
            }
            txtEmpty.visibility = View.VISIBLE
        } else {
            txtEmpty.visibility = View.GONE
        }
    }

    // 카드 XML(item_recipe_card.xml)을 불러와서 내용 채우고 붙이기 (7장: View.inflate)
    fun addCard(recipe: Recipe) {
        val card = View.inflate(this, R.layout.item_recipe_card, null)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = dp(12)
        card.layoutParams = params

        card.findViewById<TextView>(R.id.txtCardBrand).text = recipe.brand
        card.findViewById<TextView>(R.id.txtCardName).text = recipe.name
        card.findViewById<TextView>(R.id.txtCardTastes).text = recipe.tastes
        card.findViewById<TextView>(R.id.txtCardLikes).text = recipe.likes.toString()

        // 북마크 누르면 보관함에서 빼기
        val imgCardSave = card.findViewById<ImageView>(R.id.imgCardSave)
        imgCardSave.setOnClickListener {
            savedRecipes.remove(recipe)
            Toast.makeText(applicationContext, "보관함에서 뺐어요", Toast.LENGTH_SHORT).show()
            showList()
        }

        // 카드 누르면 상세 화면으로
        card.setOnClickListener {
            openDetail(recipe)
        }

        layoutSavedList.addView(card)
    }

    // 레시피가 기본 레시피인지, 내가 등록한 레시피인지 찾아서 상세 화면 열기
    fun openDetail(recipe: Recipe) {
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

    fun setChipSelected(chip : TextView, selected : Boolean) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(Color.parseColor("#7A5200"))
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(Color.parseColor("#555555"))
        }
    }

    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // ===== 컨텍스트 메뉴: 전체 삭제 (7장) =====
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_saved, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemClearAll -> {
                val dlg = AlertDialog.Builder(this@SavedActivity)
                dlg.setTitle("전체 삭제")
                dlg.setMessage("저장한 레시피를 모두 지울까요?")
                dlg.setPositiveButton("삭제") { dialog, which ->
                    savedRecipes.clear()
                    showList()
                }
                dlg.setNegativeButton("취소", null)
                dlg.show()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }
}
