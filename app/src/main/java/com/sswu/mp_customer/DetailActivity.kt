package com.sswu.mp_customer

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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

// 좋아요 누른 레시피 목록 (마이페이지 '좋아요한 조합'에서 사용)
val likedRecipes = ArrayList<Recipe>()

class DetailActivity : AppCompatActivity() {
    // ===== 1. 위젯 변수 선언 =====
    lateinit var imgBack : ImageView
    lateinit var imgMore : ImageView
    lateinit var txtName : TextView
    lateinit var txtBrand : TextView
    lateinit var txtTastes : TextView
    lateinit var txtInfo : TextView
    lateinit var btnServing1 : TextView
    lateinit var btnServing2 : TextView
    lateinit var btnServing3 : TextView
    lateinit var layoutIngredients : LinearLayout
    lateinit var layoutRatioBar : LinearLayout
    lateinit var txtRatio : TextView
    lateinit var txtTip : TextView
    lateinit var btnLike : LinearLayout
    lateinit var imgLike : ImageView
    lateinit var txtLike : TextView
    lateinit var btnSave : LinearLayout
    lateinit var imgSave : ImageView
    lateinit var btnFollow : TextView
    lateinit var btnBrandSite : TextView

    // ===== 상태 변수 =====
    lateinit var recipe : Recipe
    var serving = 1
    var liked = false
    var saved = false
    var followed = false
    var followCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ===== 2. 위젯 대입 =====
        imgBack = findViewById(R.id.imgBack)
        imgMore = findViewById(R.id.imgMore)
        txtName = findViewById(R.id.txtName)
        txtBrand = findViewById(R.id.txtBrand)
        txtTastes = findViewById(R.id.txtTastes)
        txtInfo = findViewById(R.id.txtInfo)
        btnServing1 = findViewById(R.id.btnServing1)
        btnServing2 = findViewById(R.id.btnServing2)
        btnServing3 = findViewById(R.id.btnServing3)
        layoutIngredients = findViewById(R.id.layoutIngredients)
        layoutRatioBar = findViewById(R.id.layoutRatioBar)
        txtRatio = findViewById(R.id.txtRatio)
        txtTip = findViewById(R.id.txtTip)
        btnLike = findViewById(R.id.btnLike)
        imgLike = findViewById(R.id.imgLike)
        txtLike = findViewById(R.id.txtLike)
        btnSave = findViewById(R.id.btnSave)
        imgSave = findViewById(R.id.imgSave)
        btnFollow = findViewById(R.id.btnFollow)
        btnBrandSite = findViewById(R.id.btnBrandSite)

        // ===== 3. 홈 화면이 보낸 인텐트에서 레시피 꺼내기 =====
        val index = intent.getIntExtra("index", 0)
        val isUser = intent.getBooleanExtra("isUser", false)
        recipe = if (isUser) {
            userRecipes[index]
        } else {
            recipeList[index]
        }

        saved = savedRecipes.contains(recipe)
        if (saved) {
            imgSave.setImageResource(R.drawable.ic_bookmark)
        }

        liked = likedRecipes.contains(recipe)
        if (liked) {
            imgLike.setImageResource(R.drawable.ic_favorite)
        }

        setRecipeImage(findViewById(R.id.imgRecipe), recipe)
        txtName.text = recipe.name
        txtBrand.text = recipe.brand
        txtTastes.text = recipe.tastes
        txtTip.text = recipe.tip
        txtLike.text = "좋아요 " + recipe.likes
        showInfo()
        showIngredients()
        drawRatioBar()

        // ===== 4. 클릭 동작 =====
        imgBack.setOnClickListener { finish() }

        btnBrandSite.setOnClickListener {
            val intent = Intent(applicationContext, WebActivity::class.java)
            intent.putExtra("brand", recipe.brand)
            startActivity(intent)
        }

        registerForContextMenu(imgMore)
        imgMore.setOnClickListener { openContextMenu(imgMore) }

        val servingButtons = arrayOf(btnServing1, btnServing2, btnServing3)
        for (i in servingButtons.indices) {
            servingButtons[i].setOnClickListener {
                serving = i + 1
                for (b in servingButtons) {
                    b.setBackgroundResource(R.drawable.bg_chip)
                    b.setTextColor(Color.parseColor("#555555"))
                }
                servingButtons[i].setBackgroundResource(R.drawable.bg_chip_selected)
                servingButtons[i].setTextColor(Color.parseColor("#7A5200"))
                showIngredients()
            }
        }

        // [변경] 좋아요 토글 시 Firestore users/{uid} 및 recipes/{id}에 반영
        btnLike.setOnClickListener {
            if (liked) {
                recipe.likes = recipe.likes - 1
                likedRecipes.remove(recipe)
                imgLike.setImageResource(R.drawable.ic_favorite_border)
            } else {
                recipe.likes = recipe.likes + 1
                likedRecipes.add(recipe)
                imgLike.setImageResource(R.drawable.ic_favorite)
            }
            liked = !liked
            txtLike.text = "좋아요 " + recipe.likes
            updateLikeInFirestore(recipe, liked)
        }

        // [변경] 저장(북마크) 토글 시 Firestore users/{uid}에 반영
        btnSave.setOnClickListener {
            saved = !saved
            if (saved) {
                savedRecipes.add(recipe)
                imgSave.setImageResource(R.drawable.ic_bookmark)
                updateBookmarkInFirestore(recipe, true)
                Toast.makeText(applicationContext, "보관함에 저장했어요", Toast.LENGTH_SHORT).show()
            } else {
                savedRecipes.remove(recipe)
                imgSave.setImageResource(R.drawable.ic_bookmark_border)
                updateBookmarkInFirestore(recipe, false)
            }
        }

        btnFollow.setOnClickListener {
            if (followed) {
                followCount = followCount - 1
                btnFollow.text = "✓ 따라해봤어요"
                btnFollow.setBackgroundResource(R.drawable.bg_follow_button)
            } else {
                followCount = followCount + 1
                btnFollow.text = "따라해봤어요! (취소)"
                btnFollow.setBackgroundResource(R.drawable.bg_chip_selected)
                Toast.makeText(applicationContext, "맛있게 드세요!", Toast.LENGTH_SHORT).show()
            }
            followed = !followed
            showInfo()
        }
    }

    fun showInfo() {
        txtInfo.text = recipe.author + " · 따라한 사람 " + followCount + "명"
    }

    fun showIngredients() {
        layoutIngredients.removeAllViews()

        for (i in recipe.ingredients.indices) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, dp(6), 0, dp(6))

            val txtItem = TextView(this)
            txtItem.text = recipe.ingredients[i]
            txtItem.textSize = 16f
            txtItem.setTextColor(Color.parseColor("#333333"))
            txtItem.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val txtAmount = TextView(this)
            txtAmount.text = formatAmount(recipe.amounts[i] * serving) + recipe.unit
            txtAmount.textSize = 16f
            txtAmount.setTypeface(null, Typeface.BOLD)
            txtAmount.setTextColor(Color.parseColor("#222222"))

            row.addView(txtItem)
            row.addView(txtAmount)
            layoutIngredients.addView(row)
        }
    }

    fun drawRatioBar() {
        val bar = RatioBarView(this)
        bar.amounts = recipe.amounts
        bar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(14))
        layoutRatioBar.addView(bar)

        var total = 0.0
        for (a in recipe.amounts) {
            total = total + a
        }
        var text = ""
        for (i in recipe.ingredients.indices) {
            val percent = Math.round(recipe.amounts[i] / total * 100)
            if (i > 0) text = text + "   "
            text = text + recipe.ingredients[i] + " " + percent + "%"
        }
        txtRatio.text = text
    }

    fun formatAmount(d: Double): String {
        return if (d % 1.0 == 0.0) {
            d.toInt().toString()
        } else {
            d.toString()
        }
    }

    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemShare -> {
                shareRecipe()
                return true
            }
            R.id.itemReport -> {
                showReportDialog()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    fun shareRecipe() {
        var text = "[Custom'er 꿀조합]\n" + recipe.name + " (" + recipe.brand + ")\n"
        for (i in recipe.ingredients.indices) {
            text = text + "- " + recipe.ingredients[i] + " " + formatAmount(recipe.amounts[i]) + recipe.unit + "\n"
        }
        text = text + recipe.tip

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(shareIntent, "레시피 공유하기"))
    }

    fun showReportDialog() {
        val dlg = AlertDialog.Builder(this@DetailActivity)
        dlg.setTitle("레시피 신고")
        dlg.setMessage("이 레시피를 신고할까요?")
        dlg.setPositiveButton("신고") { _, _ ->
            Toast.makeText(applicationContext, "신고가 접수되었어요", Toast.LENGTH_SHORT).show()
        }
        dlg.setNegativeButton("취소", null)
        dlg.show()
    }
}