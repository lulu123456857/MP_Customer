package com.example.mp_customer

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
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

    // 레시피 카드 1, 2
    lateinit var card1 : LinearLayout
    lateinit var card2 : LinearLayout
    lateinit var imgLike1 : ImageView
    lateinit var imgLike2 : ImageView
    lateinit var txtLike1 : TextView
    lateinit var txtLike2 : TextView
    lateinit var imgSave1 : ImageView
    lateinit var imgSave2 : ImageView

    // 하단 메뉴
    lateinit var navHome : LinearLayout
    lateinit var navSearch : LinearLayout
    lateinit var navAdd : LinearLayout
    lateinit var navSave : LinearLayout
    lateinit var navMy : LinearLayout

    // 좋아요/저장 상태를 기억하는 변수
    var liked1 = false
    var liked2 = false
    var likeCount1 = 328
    var likeCount2 = 246
    var saved1 = false
    var saved2 = false

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

        card1 = findViewById<LinearLayout>(R.id.card1)
        card2 = findViewById<LinearLayout>(R.id.card2)
        imgLike1 = findViewById<ImageView>(R.id.imgLike1)
        imgLike2 = findViewById<ImageView>(R.id.imgLike2)
        txtLike1 = findViewById<TextView>(R.id.txtLike1)
        txtLike2 = findViewById<TextView>(R.id.txtLike2)
        imgSave1 = findViewById<ImageView>(R.id.imgSave1)
        imgSave2 = findViewById<ImageView>(R.id.imgSave2)

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

        // 브랜드 필터: 하나만 선택됨 (라디오버튼처럼)
        val brandChips = arrayOf(chipAll, chipAmasvin, chipGongcha, chipHaidilao, chipMore)
        for (chip in brandChips) {
            chip.setOnClickListener {
                for (c in brandChips) {
                    setChipSelected(c, false)
                }
                setChipSelected(chip, true)
                Toast.makeText(applicationContext, chip.text.toString() + " 선택", Toast.LENGTH_SHORT).show()
            }
        }

        // 맛 필터: 여러 개 선택 가능 (체크박스처럼, 누를 때마다 켜짐/꺼짐)
        val tasteChips = arrayOf(chipSweet, chipSour, chipNutty, chipSpicy, chipChoco)
        val tasteSelected = BooleanArray(tasteChips.size)   // 처음엔 모두 false
        for (i in tasteChips.indices) {
            tasteChips[i].setOnClickListener {
                tasteSelected[i] = !tasteSelected[i]
                setChipSelected(tasteChips[i], tasteSelected[i])
            }
        }

        // 정렬 탭: 선택된 쪽은 진하게, 아닌 쪽은 회색
        tabPopular.setOnClickListener {
            tabPopular.setTextColor(Color.parseColor("#222222"))
            tabRecent.setTextColor(Color.parseColor("#888888"))
            Toast.makeText(applicationContext, "인기순 정렬", Toast.LENGTH_SHORT).show()
        }
        tabRecent.setOnClickListener {
            tabRecent.setTextColor(Color.parseColor("#222222"))
            tabPopular.setTextColor(Color.parseColor("#888888"))
            Toast.makeText(applicationContext, "최신순 정렬", Toast.LENGTH_SHORT).show()
        }

        // 좋아요: 누르면 하트가 채워지고 숫자 +1, 다시 누르면 취소
        imgLike1.setOnClickListener {
            if (liked1) {
                likeCount1 = likeCount1 - 1
                imgLike1.setImageResource(R.drawable.ic_favorite_border)
            } else {
                likeCount1 = likeCount1 + 1
                imgLike1.setImageResource(R.drawable.ic_favorite)
            }
            liked1 = !liked1
            txtLike1.text = likeCount1.toString()
        }
        imgLike2.setOnClickListener {
            if (liked2) {
                likeCount2 = likeCount2 - 1
                imgLike2.setImageResource(R.drawable.ic_favorite_border)
            } else {
                likeCount2 = likeCount2 + 1
                imgLike2.setImageResource(R.drawable.ic_favorite)
            }
            liked2 = !liked2
            txtLike2.text = likeCount2.toString()
        }

        // 저장: 누르면 북마크가 채워짐
        imgSave1.setOnClickListener {
            saved1 = !saved1
            if (saved1) {
                imgSave1.setImageResource(R.drawable.ic_bookmark)
                Toast.makeText(applicationContext, "보관함에 저장했어요", Toast.LENGTH_SHORT).show()
            } else {
                imgSave1.setImageResource(R.drawable.ic_bookmark_border)
            }
        }
        imgSave2.setOnClickListener {
            saved2 = !saved2
            if (saved2) {
                imgSave2.setImageResource(R.drawable.ic_bookmark)
                Toast.makeText(applicationContext, "보관함에 저장했어요", Toast.LENGTH_SHORT).show()
            } else {
                imgSave2.setImageResource(R.drawable.ic_bookmark_border)
            }
        }

        // 카드 클릭: 상세 화면 이동은 Intent 배우면 연결
        card1.setOnClickListener {
            Toast.makeText(applicationContext, "고소한 마늘 참깨 소스 상세 (준비 중)", Toast.LENGTH_SHORT).show()
        }
        card2.setOnClickListener {
            Toast.makeText(applicationContext, "흑당펄 듬뿍 밀크티 상세 (준비 중)", Toast.LENGTH_SHORT).show()
        }

        // 하단 메뉴: 화면 전환은 Fragment 배우면 연결
        navSearch.setOnClickListener {
            Toast.makeText(applicationContext, "검색 화면 (준비 중)", Toast.LENGTH_SHORT).show()
        }
        navAdd.setOnClickListener {
            Toast.makeText(applicationContext, "등록 화면 (준비 중)", Toast.LENGTH_SHORT).show()
        }
        navSave.setOnClickListener {
            Toast.makeText(applicationContext, "저장 화면 (준비 중)", Toast.LENGTH_SHORT).show()
        }
        navMy.setOnClickListener {
            Toast.makeText(applicationContext, "마이페이지 (준비 중)", Toast.LENGTH_SHORT).show()
        }
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
