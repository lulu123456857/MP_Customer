package com.example.mp_customer

// 레시피 한 개를 담는 클래스 (3장 클래스와 인스턴스)
class Recipe(
    val name: String,               // 조합 이름
    val brand: String,              // 브랜드
    val tastes: String,             // 맛 태그 (예: "고소한 맛 · 매운맛")
    val author: String,             // 작성자 닉네임
    val ingredients: Array<String>, // 재료 이름들
    val amounts: DoubleArray,       // 1인분 기준 양 (재료 순서와 같게)
    val unit: String,               // 단위 (한 레시피 안에서는 하나로 통일)
    val tip: String,                // 만드는 방법·팁
    var likes: Int                  // 좋아요 수
)

// 앱에서 사용할 레시피 목록
// recipeList[0] = 첫 번째 레시피, recipeList[1] = 두 번째 레시피 ...
val recipeList = arrayOf(
    Recipe(
        "고소한 마늘 참깨 소스", "하이디라오", "고소한 맛 · 매운맛", "소스장인",
        arrayOf("참깨소스", "땅콩소스", "다진마늘"),
        doubleArrayOf(2.0, 1.0, 0.5),
        "스푼",
        "소스를 먼저 섞고 다진마늘을 넣어 잘 저어주세요. 고기와 찰떡! 매운맛이 부담되면 마늘을 줄여요.",
        328
    ),
    Recipe(
        "흑당펄 듬뿍 밀크티", "공차", "달달한 맛", "펄러버",
        arrayOf("블랙밀크티", "흑당펄", "치즈폼"),
        doubleArrayOf(1.0, 2.0, 1.0),
        "샷",
        "당도는 30%, 얼음은 적게 주문하면 펄의 단맛이 더 잘 느껴져요.",
        246
    )
    // 지윤: 여기에 레시피 3개 더 추가 (위 레시피 끝 ) 뒤에 쉼표 잊지 말기)
)
