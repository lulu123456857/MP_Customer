package com.sswu.mp_customer

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {
    lateinit var webView : WebView
    lateinit var txtWebTitle : TextView
    lateinit var txtWebBack : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        webView = findViewById<WebView>(R.id.webView)
        txtWebTitle = findViewById<TextView>(R.id.txtWebTitle)
        txtWebBack = findViewById<TextView>(R.id.txtWebBack)

        // 상세 화면에서 넘겨준 브랜드 이름으로 주소 고르기
        val brand = intent.getStringExtra("brand") ?: ""
        val url = when (brand) {
            "공차" -> "https://www.gong-cha.co.kr"
            "아마스빈" -> "https://www.amasvin.com"
            "하이디라오" -> "https://www.haidilao.com"
            else -> ""
        }

        if (url == "") {
            Toast.makeText(applicationContext, "이 브랜드의 홈페이지 주소가 없어요", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        txtWebTitle.text = brand

        txtWebBack.setOnClickListener {
            finish()
        }

        // 웹뷰 설정: 링크를 눌러도 앱 안에서 열리게
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
    }
}