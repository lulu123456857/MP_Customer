package com.sswu.mp_customer

import android.widget.ImageView

// 레시피의 브랜드를 보고, 이미지 칸에 알맞은 이미지를 넣는 함수
fun setRecipeImage(img: ImageView, recipe: Recipe) {
    var image = R.drawable.recipe_drink    // 기본: 음료 이미지

    when (recipe.brand) {
        "공차", "아마스빈" -> image = R.drawable.recipe_drink
        "하이디라오" -> image = R.drawable.recipe_bowl
    }

    // 이전에 입힌 배경·색을 지우고 이미지를 꽉 차게 넣기
    img.background = null
    img.clearColorFilter()
    img.scaleType = ImageView.ScaleType.FIT_CENTER
    img.setImageResource(image)
}