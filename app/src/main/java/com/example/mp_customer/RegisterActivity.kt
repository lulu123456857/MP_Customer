package com.example.mp_customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
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

// 사용자가 등록한 레시피를 모아두는 목록 (앱을 끄면 사라짐 → 나중에 Firestore로 바꿀 예정)
val userRecipes = ArrayList<Recipe>()

class RegisterActivity : AppCompatActivity() {
    // ===== 1. 위젯 변수 선언 =====
    lateinit var imgBack : ImageView
    lateinit var imgHelp : ImageView
    lateinit var boxPhoto : LinearLayout
    lateinit var editName : EditText
    lateinit var boxBrand : LinearLayout
    lateinit var txtBrand : TextView
    lateinit var chipSweet : TextView
    lateinit var chipSour : TextView
    lateinit var chipNutty : TextView
    lateinit var chipSpicy : TextView
    lateinit var chipChoco : TextView
    lateinit var layoutRows : LinearLayout
    lateinit var btnAddRow : TextView
    lateinit var btnServing1 : TextView
    lateinit var btnServing2 : TextView
    lateinit var btnServing3 : TextView
    lateinit var editTip : EditText
    lateinit var btnRegister : TextView

    // ===== 상태 변수 =====
    val brands = arrayOf("아마스빈", "공차", "하이디라오", "서브웨이", "기타")
    val units = arrayOf("스푼", "샷", "ml", "g", "개", "%")
    var selectedBrand = ""          // 선택한 브랜드 (처음엔 비어 있음)
    var selectedUnit = "스푼"        // 선택한 단위 (모든 재료에 똑같이 적용)
    var baseServing = 1             // 기준 인분
    val tasteSelected = BooleanArray(5)

    // 재료 입력 줄마다 만든 위젯을 모아두는 목록
    val nameEdits = ArrayList<EditText>()
    val amountEdits = ArrayList<EditText>()
    val unitTexts = ArrayList<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ===== 2. 위젯 대입 =====
        imgBack = findViewById<ImageView>(R.id.imgBack)
        imgHelp = findViewById<ImageView>(R.id.imgHelp)
        boxPhoto = findViewById<LinearLayout>(R.id.boxPhoto)
        editName = findViewById<EditText>(R.id.editName)
        boxBrand = findViewById<LinearLayout>(R.id.boxBrand)
        txtBrand = findViewById<TextView>(R.id.txtBrand)
        chipSweet = findViewById<TextView>(R.id.chipSweet)
        chipSour = findViewById<TextView>(R.id.chipSour)
        chipNutty = findViewById<TextView>(R.id.chipNutty)
        chipSpicy = findViewById<TextView>(R.id.chipSpicy)
        chipChoco = findViewById<TextView>(R.id.chipChoco)
        layoutRows = findViewById<LinearLayout>(R.id.layoutRows)
        btnAddRow = findViewById<TextView>(R.id.btnAddRow)
        btnServing1 = findViewById<TextView>(R.id.btnServing1)
        btnServing2 = findViewById<TextView>(R.id.btnServing2)
        btnServing3 = findViewById<TextView>(R.id.btnServing3)
        editTip = findViewById<EditText>(R.id.editTip)
        btnRegister = findViewById<TextView>(R.id.btnRegister)

        // 처음에 재료 입력 줄 2개 만들어두기
        addIngredientRow()
        addIngredientRow()

        // ===== 3. 클릭 동작 =====

        // 뒤로가기
        imgBack.setOnClickListener {
            finish()
        }

        // 도움말: 안내 대화상자 (7장)
        imgHelp.setOnClickListener {
            val dlg = AlertDialog.Builder(this@RegisterActivity)
            dlg.setTitle("꿀조합 등록 방법")
            dlg.setMessage("1. 조합 이름과 브랜드를 정해요\n2. 맛 태그를 골라요\n3. 재료와 양을 적어요 (단위는 하나로 통일)\n4. 몇 인분 기준인지 골라요\n\n등록하면 인분별 양과 비율 그래프가 자동으로 계산돼요!")
            dlg.setPositiveButton("확인", null)
            dlg.show()
        }

        // 사진 추가: 아직 준비 중
        boxPhoto.setOnClickListener {
            Toast.makeText(applicationContext, "사진 추가는 준비 중이에요", Toast.LENGTH_SHORT).show()
        }

        // 브랜드: 목록 대화상자에서 고르기 (7장 목록 대화상자)
        boxBrand.setOnClickListener {
            val dlg = AlertDialog.Builder(this@RegisterActivity)
            dlg.setTitle("브랜드 선택")
            dlg.setItems(brands) { dialog, which ->
                selectedBrand = brands[which]
                txtBrand.text = selectedBrand
                txtBrand.setTextColor(Color.parseColor("#222222"))
            }
            dlg.setNegativeButton("취소", null)
            dlg.show()
        }

        // 맛 태그: 누를 때마다 켜짐/꺼짐 (여러 개 선택)
        val tasteChips = arrayOf(chipSweet, chipSour, chipNutty, chipSpicy, chipChoco)
        for (i in tasteChips.indices) {
            tasteChips[i].setOnClickListener {
                tasteSelected[i] = !tasteSelected[i]
                setChipSelected(tasteChips[i], tasteSelected[i])
            }
        }

        // 재료 추가 버튼: 입력 줄 하나 더 만들기
        btnAddRow.setOnClickListener {
            addIngredientRow()
        }

        // 기준 인분: 하나만 선택
        val servingButtons = arrayOf(btnServing1, btnServing2, btnServing3)
        for (i in servingButtons.indices) {
            servingButtons[i].setOnClickListener {
                baseServing = i + 1
                for (b in servingButtons) {
                    setChipSelected(b, false)
                }
                setChipSelected(servingButtons[i], true)
            }
        }

        // 등록 버튼
        btnRegister.setOnClickListener {
            checkAndRegister()
        }
    }

    // 재료 입력 줄 하나를 Kotlin 코드로 만들어 붙이기 (5장: Kotlin 코드로 화면 만들기)
    // [재료 이름 입력칸] [양 입력칸] [단위 ▾]
    fun addIngredientRow() {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val rowParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48))
        rowParams.topMargin = dp(8)
        row.layoutParams = rowParams

        // 재료 이름
        val editItem = EditText(this)
        editItem.hint = "재료명"
        editItem.textSize = 15f
        editItem.setBackgroundResource(R.drawable.bg_input)
        editItem.setPadding(dp(12), 0, dp(12), 0)
        editItem.inputType = InputType.TYPE_CLASS_TEXT
        editItem.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        // 양 (숫자만 입력, 소수점 가능)
        val editAmount = EditText(this)
        editAmount.hint = "양"
        editAmount.textSize = 15f
        editAmount.gravity = Gravity.CENTER
        editAmount.setBackgroundResource(R.drawable.bg_input)
        editAmount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val amountParams = LinearLayout.LayoutParams(dp(64), LinearLayout.LayoutParams.MATCH_PARENT)
        amountParams.leftMargin = dp(8)
        editAmount.layoutParams = amountParams

        // 단위 (누르면 목록 대화상자)
        val txtUnit = TextView(this)
        txtUnit.text = selectedUnit + " ▾"
        txtUnit.textSize = 14f
        txtUnit.gravity = Gravity.CENTER
        txtUnit.setTextColor(Color.parseColor("#333333"))
        txtUnit.setBackgroundResource(R.drawable.bg_input)
        val unitParams = LinearLayout.LayoutParams(dp(76), LinearLayout.LayoutParams.MATCH_PARENT)
        unitParams.leftMargin = dp(8)
        txtUnit.layoutParams = unitParams
        txtUnit.setOnClickListener {
            showUnitDialog()
        }

        row.addView(editItem)
        row.addView(editAmount)
        row.addView(txtUnit)
        layoutRows.addView(row)

        // 나중에 입력값을 꺼내 쓰려고 목록에 저장
        nameEdits.add(editItem)
        amountEdits.add(editAmount)
        unitTexts.add(txtUnit)
    }

    // 단위 고르기: 고르면 모든 재료 줄의 단위가 함께 바뀜 (비율 그래프를 위해 통일)
    fun showUnitDialog() {
        val dlg = AlertDialog.Builder(this@RegisterActivity)
        dlg.setTitle("단위 선택")
        dlg.setItems(units) { dialog, which ->
            selectedUnit = units[which]
            for (t in unitTexts) {
                t.text = selectedUnit + " ▾"
            }
        }
        dlg.setNegativeButton("취소", null)
        dlg.show()
    }

    // 입력값을 검사하고, 괜찮으면 확인 대화상자 띄우기
    fun checkAndRegister() {
        val name = editName.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(applicationContext, "조합 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedBrand.isEmpty()) {
            Toast.makeText(applicationContext, "브랜드를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 재료 모으기: 이름과 양이 둘 다 적힌 줄만 사용
        val ingredientList = ArrayList<String>()
        val amountList = ArrayList<Double>()
        for (i in nameEdits.indices) {
            val item = nameEdits[i].text.toString()
            val amount = amountEdits[i].text.toString().toDoubleOrNull()   // 숫자가 아니면 null
            if (item.isNotEmpty() && amount != null && amount > 0) {
                ingredientList.add(item)
                amountList.add(amount / baseServing)    // 1인분 기준 양으로 바꿔서 저장
            }
        }
        if (ingredientList.size < 2) {
            Toast.makeText(applicationContext, "재료를 2개 이상 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 선택한 맛 태그를 "고소한 맛 · 매운맛" 형태로 만들기
        val tasteNames = arrayOf("달달한 맛", "상큼한 맛", "고소한 맛", "매운맛", "초코")
        var tastes = ""
        for (i in tasteNames.indices) {
            if (tasteSelected[i]) {
                if (tastes.isNotEmpty()) tastes = tastes + " · "
                tastes = tastes + tasteNames[i]
            }
        }
        if (tastes.isEmpty()) tastes = "맛 태그 없음"

        var tip = editTip.text.toString()
        if (tip.isEmpty()) tip = "작성된 팁이 없어요."

        // 확인 대화상자 (7장)
        val dlg = AlertDialog.Builder(this@RegisterActivity)
        dlg.setTitle("꿀조합 등록")
        dlg.setMessage("'" + name + "'을(를) 등록할까요?")
        dlg.setPositiveButton("등록") { dialog, which ->
            // 새 레시피 만들어서 목록에 추가
            val newRecipe = Recipe(
                name, selectedBrand, tastes, "나",
                ingredientList.toTypedArray(),
                amountList.toDoubleArray(),
                selectedUnit, tip, 0
            )
            userRecipes.add(newRecipe)
            Toast.makeText(applicationContext, "등록됐어요!", Toast.LENGTH_SHORT).show()

            // 방금 등록한 레시피의 상세 화면으로 이동 (10장 인텐트)
            val intent = Intent(applicationContext, DetailActivity::class.java)
            intent.putExtra("index", userRecipes.size - 1)
            intent.putExtra("isUser", true)
            startActivity(intent)
            finish()   // 등록 화면은 닫기
        }
        dlg.setNegativeButton("취소", null)
        dlg.show()
    }

    // 칩 선택/해제 모양 바꾸기
    fun setChipSelected(chip : TextView, selected : Boolean) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(Color.parseColor("#7A5200"))
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(Color.parseColor("#555555"))
        }
    }

    // dp → 픽셀
    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
