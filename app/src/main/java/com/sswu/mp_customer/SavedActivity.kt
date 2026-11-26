package com.sswu.mp_customer

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

val savedRecipes = ArrayList<Recipe>()

class SavedActivity : AppCompatActivity() {
    lateinit var imgBack : ImageView
    lateinit var imgMore : ImageView
    lateinit var txtSavedCount : TextView
    lateinit var chipAll : TextView
    lateinit var chipGongcha : TextView
    lateinit var chipAmasvin : TextView
    lateinit var chipHaidilao : TextView
    lateinit var chipEtc : TextView
    lateinit var layoutSavedList : LinearLayout
    lateinit var txtEmpty : TextView

    var filterBrand = "전체"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgBack = findViewById(R.id.imgBack)
        imgMore = findViewById(R.id.imgMore)
        txtSavedCount = findViewById(R.id.txtSavedCount)
        chipAll = findViewById(R.id.chipAll)
        chipGongcha = findViewById(R.id.chipGongcha)
        chipAmasvin = findViewById(R.id.chipAmasvin)
        chipHaidilao = findViewById(R.id.chipHaidilao)
        chipEtc = findViewById<TextView>(R.id.chipEtc)
        layoutSavedList = findViewById(R.id.layoutSavedList)
        txtEmpty = findViewById(R.id.txtEmpty)

        imgBack.setOnClickListener { finish() }

        registerForContextMenu(imgMore)
        imgMore.setOnClickListener { openContextMenu(imgMore) }

        val brandChips = arrayOf(chipAll, chipGongcha, chipAmasvin, chipHaidilao, chipEtc)
        val brandNames = arrayOf("전체", "공차", "아마스빈", "하이디라오", "기타")
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

    override fun onResume() {
        super.onResume()
        showList()
    }

    fun showList() {
        layoutSavedList.removeAllViews()
        txtSavedCount.text = "저장한 레시피 " + savedRecipes.size + "개"

        var shownCount = 0
        for (i in savedRecipes.indices.reversed()) {
            val recipe = savedRecipes[i]
            if (filterBrand != "전체" && recipe.brand != filterBrand) {
                continue
            }
            addCard(recipe)
            shownCount = shownCount + 1
        }

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

        // [변경] 북마크 해제 시 Firestore에서도 제거
        val imgCardSave = card.findViewById<ImageView>(R.id.imgCardSave)
        imgCardSave.setOnClickListener {
            savedRecipes.remove(recipe)
            updateBookmarkInFirestore(recipe, false)
            Toast.makeText(applicationContext, "보관함에서 뺐어요", Toast.LENGTH_SHORT).show()
            showList()
        }

        card.setOnClickListener { openDetail(recipe) }

        layoutSavedList.addView(card)
    }

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
                dlg.setPositiveButton("삭제") { _, _ ->
                    savedRecipes.clear()
                    // [변경] Firestore 보관함도 전체 비우기
                    clearBookmarksInFirestore()
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