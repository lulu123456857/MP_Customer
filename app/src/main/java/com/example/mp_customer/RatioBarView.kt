package com.example.mp_customer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

// 재료 비율을 가로 막대그래프로 직접 그리는 뷰 (9장 그래픽: View를 상속받아 onDraw 재정의)
class RatioBarView(context: Context) : View(context) {
    var amounts = DoubleArray(0)   // 그래프로 그릴 재료 양들

    // 재료마다 쓸 색 (재료가 5개를 넘으면 처음 색부터 다시 사용)
    val colors = arrayOf("#E5BC58", "#C49A6C", "#EADBC0", "#8A6A3A", "#F3E3B5")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. 전체 양 구하기
        var total = 0.0
        for (a in amounts) {
            total = total + a
        }
        if (total == 0.0) return

        // 2. 재료마다 (내 양 / 전체 양) 비율만큼 사각형을 이어서 그리기
        val paint = Paint()
        var left = 0f
        for (i in amounts.indices) {
            val barWidth = (amounts[i] / total * width).toFloat()
            paint.color = Color.parseColor(colors[i % colors.size])
            canvas.drawRect(left, 0f, left + barWidth, height.toFloat(), paint)
            left = left + barWidth
        }
    }
}
