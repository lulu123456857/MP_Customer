package com.sswu.mp_customer

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
    val brands = arrayOf("아마스빈", "공차", "하이디라오", "기타")
    val units = arrayOf("스푼", "샷", "ml", "g", "개", "%")
    var selectedBrand = ""
    var selectedUnit = "스푼"
    var baseServing = 1
    val tasteSelected = BooleanArray(5)

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
        imgBack = findViewById(R.id.imgBack)
        imgHelp = findViewById(R.id.imgHelp)
        boxPhoto = findViewById(R.id.boxPhoto)
        editName = findViewById(R.id.editName)
        boxBrand = findViewById(R.id.boxBrand)
        txtBrand = findViewById(R.id.txtBrand)
        chipSweet = findViewById(R.id.chipSweet)
        chipSour = findViewById(R.id.chipSour)
        chipNutty = findViewById(R.id.chipNutty)
        chipSpicy = findViewById(R.id.chipSpicy)
        chipChoco = findViewById(R.id.chipChoco)
        layoutRows = findViewById(R.id.layoutRows)
        btnAddRow = findViewById(R.id.btnAddRow)
        btnServing1 = findViewById(R.id.btnServing1)
        btnServing2 = findViewById(R.id.btnServing2)
        btnServing3 = findViewById(R.id.btnServing3)
        editTip = findViewById(R.id.editTip)
        btnRegister = findViewById(R.id.btnRegister)

        addIngredientRow()
        addIngredientRow()

        // ===== 3. 클릭 동작 =====
        imgBack.setOnClickListener { finish() }

        imgHelp.setOnClickListener {
            val dlg = AlertDialog.Builder(this@RegisterActivity)
            dlg.setTitle("꿀조합 등록 방법")
            dlg.setMessage("1. 조합 이름과 브랜드를 정해요\n2. 맛 태그를 골라요\n3. 재료와 양을 적어요 (단위는 하나로 통일)\n4. 몇 인분 기준인지 골라요\n\n등록하면 인분별 양과 비율 그래프가 자동으로 계산돼요!")
            dlg.setPositiveButton("확인", null)
            dlg.show()
        }

        boxPhoto.setOnClickListener {
            Toast.makeText(applicationContext, "사진 추가는 준비 중이에요", Toast.LENGTH_SHORT).show()
        }

        boxBrand.setOnClickListener {
            val dlg = AlertDialog.Builder(this@RegisterActivity)
            dlg.setTitle("브랜드 선택")
            dlg.setItems(brands) { _, which ->
                selectedBrand = brands[which]
                txtBrand.text = selectedBrand
                txtBrand.setTextColor(Color.parseColor("#222222"))
            }
            dlg.setNegativeButton("취소", null)
            dlg.show()
        }

        val tasteChips = arrayOf(chipSweet, chipSour, chipNutty, chipSpicy, chipChoco)
        for (i in tasteChips.indices) {
            tasteChips[i].setOnClickListener {
                tasteSelected[i] = !tasteSelected[i]
                setChipSelected(tasteChips[i], tasteSelected[i])
            }
        }

        btnAddRow.setOnClickListener { addIngredientRow() }

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

        btnRegister.setOnClickListener {
            if (!isLoggedIn()) {
                Toast.makeText(applicationContext, "로그인 후 사용 가능합니다", Toast.LENGTH_SHORT).show()
            } else {
                checkAndRegister()
            }
        }}

    fun addIngredientRow() {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val rowParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48))
        rowParams.topMargin = dp(8)
        row.layoutParams = rowParams

        val editItem = EditText(this)
        editItem.hint = "재료명"
        editItem.textSize = 15f
        editItem.setBackgroundResource(R.drawable.bg_input)
        editItem.setPadding(dp(12), 0, dp(12), 0)
        editItem.inputType = InputType.TYPE_CLASS_TEXT
        editItem.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        val editAmount = EditText(this)
        editAmount.hint = "양"
        editAmount.textSize = 15f
        editAmount.gravity = Gravity.CENTER
        editAmount.setBackgroundResource(R.drawable.bg_input)
        editAmount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val amountParams = LinearLayout.LayoutParams(dp(64), LinearLayout.LayoutParams.MATCH_PARENT)
        amountParams.leftMargin = dp(8)
        editAmount.layoutParams = amountParams

        val txtUnit = TextView(this)
        txtUnit.text = "$selectedUnit ▾"
        txtUnit.textSize = 14f
        txtUnit.gravity = Gravity.CENTER
        txtUnit.setTextColor(Color.parseColor("#333333"))
        txtUnit.setBackgroundResource(R.drawable.bg_input)
        val unitParams = LinearLayout.LayoutParams(dp(76), LinearLayout.LayoutParams.MATCH_PARENT)
        unitParams.leftMargin = dp(8)
        txtUnit.layoutParams = unitParams
        txtUnit.setOnClickListener { showUnitDialog() }

        row.addView(editItem)
        row.addView(editAmount)
        row.addView(txtUnit)
        layoutRows.addView(row)

        nameEdits.add(editItem)
        amountEdits.add(editAmount)
        unitTexts.add(txtUnit)
    }

    fun showUnitDialog() {
        val dlg = AlertDialog.Builder(this@RegisterActivity)
        dlg.setTitle("단위 선택")
        dlg.setItems(units) { _, which ->
            selectedUnit = units[which]
            for (t in unitTexts) {
                t.text = "$selectedUnit ▾"
            }
        }
        dlg.setNegativeButton("취소", null)
        dlg.show()
    }

    fun checkAndRegister() {
        val name = editName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(applicationContext, "조합 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedBrand.isEmpty()) {
            Toast.makeText(applicationContext, "브랜드를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredientList = ArrayList<String>()
        val amountList = ArrayList<Double>()
        for (i in nameEdits.indices) {
            val item = nameEdits[i].text.toString().trim()
            val amount = amountEdits[i].text.toString().toDoubleOrNull()
            if (item.isNotEmpty() && amount != null && amount > 0) {
                ingredientList.add(item)
                amountList.add(amount / baseServing)
            }
        }
        if (ingredientList.size < 2) {
            Toast.makeText(applicationContext, "재료를 2개 이상 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val tasteNames = arrayOf("달달한 맛", "상큼한 맛", "고소한 맛", "매운맛", "초코")
        var tastes = ""
        for (i in tasteNames.indices) {
            if (tasteSelected[i]) {
                if (tastes.isNotEmpty()) tastes += " · "
                tastes += tasteNames[i]
            }
        }
        if (tastes.isEmpty()) tastes = "맛 태그 없음"

        var tip = editTip.text.toString().trim()
        if (tip.isEmpty()) tip = "작성된 팁이 없어요."

        // [추가] 현재 로그인한 사용자 UID 및 닉네임 가져오기
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val authorNickname = prefs.getString("NICKNAME", "소스장인") ?: "소스장인"

        val dlg = AlertDialog.Builder(this@RegisterActivity)
        dlg.setTitle("꿀조합 등록")
        dlg.setMessage("'$name'을(를) 등록할까요?")
        dlg.setPositiveButton("등록") { _, _ ->
            btnRegister.isEnabled = false

            // [변경] Firestore 'recipes' 컬렉션에 저장할 데이터 구성
            val db = FirebaseFirestore.getInstance()
            val newDocRef = db.collection("recipes").document()
            val recipeData = hashMapOf(
                "name" to name,
                "brand" to selectedBrand,
                "tastes" to tastes,
                "author" to authorNickname,
                "uid" to uid,
                "ingredients" to ingredientList,
                "amounts" to amountList,
                "unit" to selectedUnit,
                "tip" to tip,
                "likes" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )

            newDocRef.set(recipeData)
                .addOnSuccessListener {
                    // 로컬 userRecipes에도 동일 ID로 즉시 반영 후 상세 화면으로 이동
                    val newRecipe = Recipe(
                        name, selectedBrand, tastes, authorNickname,
                        ingredientList.toTypedArray(),
                        amountList.toDoubleArray(),
                        selectedUnit, tip, 0,
                        id = newDocRef.id,
                        authorUid = uid
                    )
                    userRecipes.add(newRecipe)
                    Toast.makeText(applicationContext, "등록됐어요!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(applicationContext, DetailActivity::class.java)
                    intent.putExtra("index", userRecipes.size - 1)
                    intent.putExtra("isUser", true)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnRegister.isEnabled = true
                    Toast.makeText(applicationContext, "등록 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
        dlg.setNegativeButton("취소", null)
        dlg.show()
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
}