package com.example.mp_customer

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

    // ===== 상태 변수 =====
    lateinit var recipe : Recipe   // 지금 보여주는 레시피
    var serving = 1                // 선택한 인분 (1, 2, 3)
    var liked = false
    var saved = false
    var followed = false
    var followCount = 0            // 따라한 사람 수

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
        imgBack = findViewById<ImageView>(R.id.imgBack)
        imgMore = findViewById<ImageView>(R.id.imgMore)
        txtName = findViewById<TextView>(R.id.txtName)
        txtBrand = findViewById<TextView>(R.id.txtBrand)
        txtTastes = findViewById<TextView>(R.id.txtTastes)
        txtInfo = findViewById<TextView>(R.id.txtInfo)
        btnServing1 = findViewById<TextView>(R.id.btnServing1)
        btnServing2 = findViewById<TextView>(R.id.btnServing2)
        btnServing3 = findViewById<TextView>(R.id.btnServing3)
        layoutIngredients = findViewById<LinearLayout>(R.id.layoutIngredients)
        layoutRatioBar = findViewById<LinearLayout>(R.id.layoutRatioBar)
        txtRatio = findViewById<TextView>(R.id.txtRatio)
        txtTip = findViewById<TextView>(R.id.txtTip)
        btnLike = findViewById<LinearLayout>(R.id.btnLike)
        imgLike = findViewById<ImageView>(R.id.imgLike)
        txtLike = findViewById<TextView>(R.id.txtLike)
        btnSave = findViewById<LinearLayout>(R.id.btnSave)
        imgSave = findViewById<ImageView>(R.id.imgSave)
        btnFollow = findViewById<TextView>(R.id.btnFollow)

        // ===== 3. 홈 화면이 보낸 인텐트에서 레시피 번호 꺼내기 (10장 명시적 인텐트) =====
        val index = intent.getIntExtra("index", 0)
        val isUser = intent.getBooleanExtra("isUser", false)   // 사용자가 등록한 레시피인지
        if (isUser) {
            recipe = userRecipes[index]     // 등록 화면에서 만든 레시피
        } else {
            recipe = recipeList[index]      // 기본 레시피 (RecipeData.kt)
        }

        // 이미 저장한 레시피면 북마크를 채워서 보여주기
        saved = savedRecipes.contains(recipe)
        if (saved) {
            imgSave.setImageResource(R.drawable.ic_bookmark)
        }

        // 화면에 레시피 내용 채우기
        txtName.text = recipe.name
        txtBrand.text = recipe.brand
        txtTastes.text = recipe.tastes
        txtTip.text = recipe.tip
        txtLike.text = "좋아요 " + recipe.likes
        showInfo()
        showIngredients()
        drawRatioBar()

        // ===== 4. 클릭 동작 =====

        // 뒤로가기: 현재 액티비티 끝내기 → 홈 화면으로 돌아감 (10장)
        imgBack.setOnClickListener {
            finish()
        }

        // 더보기(⋮): 컨텍스트 메뉴 열기 (7장)
        registerForContextMenu(imgMore)
        imgMore.setOnClickListener {
            openContextMenu(imgMore)
        }

        // 인분 버튼: 누른 버튼만 선택 표시하고 재료 양 다시 계산
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

        // 좋아요
        btnLike.setOnClickListener {
            if (liked) {
                recipe.likes = recipe.likes - 1
                imgLike.setImageResource(R.drawable.ic_favorite_border)
            } else {
                recipe.likes = recipe.likes + 1
                imgLike.setImageResource(R.drawable.ic_favorite)
            }
            liked = !liked
            txtLike.text = "좋아요 " + recipe.likes
        }

        // 저장
        // 저장: 보관함 목록(savedRecipes)에 넣거나 빼기
        btnSave.setOnClickListener {
            saved = !saved
            if (saved) {
                savedRecipes.add(recipe)
                imgSave.setImageResource(R.drawable.ic_bookmark)
                Toast.makeText(applicationContext, "보관함에 저장했어요", Toast.LENGTH_SHORT).show()
            } else {
                savedRecipes.remove(recipe)
                imgSave.setImageResource(R.drawable.ic_bookmark_border)
            }
        }

        // 따라해봤어요: 누르면 따라한 사람 +1, 다시 누르면 취소
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

    // 작성자 · 따라한 사람 수 표시
    fun showInfo() {
        txtInfo.text = recipe.author + " · 따라한 사람 " + followCount + "명"
    }

    // 재료 목록을 Kotlin 코드로 만들어서 붙이기 (5장: Kotlin 코드로 화면 만들기)
    fun showIngredients() {
        layoutIngredients.removeAllViews()   // 이전에 붙였던 줄 지우기

        for (i in recipe.ingredients.indices) {
            // 한 줄 = 가로 리니어레이아웃 [재료 이름 ......... 양+단위]
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, dp(6), 0, dp(6))

            val txtItem = TextView(this)
            txtItem.text = recipe.ingredients[i]
            txtItem.textSize = 16f
            txtItem.setTextColor(Color.parseColor("#333333"))
            txtItem.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val txtAmount = TextView(this)
            txtAmount.text = formatAmount(recipe.amounts[i] * serving) + recipe.unit   // 1인분 양 × 인분
            txtAmount.textSize = 16f
            txtAmount.setTypeface(null, Typeface.BOLD)
            txtAmount.setTextColor(Color.parseColor("#222222"))

            row.addView(txtItem)
            row.addView(txtAmount)
            layoutIngredients.addView(row)
        }
    }

    // 비율 막대그래프 + % 글자 만들기
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

    // 2.0 → "2", 0.5 → "0.5" 처럼 보기 좋게 바꾸기
    fun formatAmount(d: Double): String {
        if (d % 1.0 == 0.0) {
            return d.toInt().toString()
        } else {
            return d.toString()
        }
    }

    // dp 단위를 픽셀로 바꾸기 (코드로 크기를 정할 때는 픽셀 단위라서)
    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // ===== 컨텍스트 메뉴 (7장) =====
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

    // 공유하기: 카톡, 메시지 등 다른 앱으로 레시피 보내기 (10장 암시적 인텐트)
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

    // 신고하기: 확인 대화상자 (7장 대화상자)
    fun showReportDialog() {
        val dlg = AlertDialog.Builder(this@DetailActivity)
        dlg.setTitle("레시피 신고")
        dlg.setMessage("이 레시피를 신고할까요?")
        dlg.setPositiveButton("신고") { dialog, which ->
            Toast.makeText(applicationContext, "신고가 접수되었어요", Toast.LENGTH_SHORT).show()
        }
        dlg.setNegativeButton("취소", null)
        dlg.show()
    }
}
