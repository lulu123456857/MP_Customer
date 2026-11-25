package com.sswu.mp_customer

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// 레시피 한 개를 담는 클래스
class Recipe(
    val name: String,               // 조합 이름
    val brand: String,              // 브랜드
    val tastes: String,             // 맛 태그 (예: "고소한 맛 · 매운맛")
    val author: String,             // 작성자 닉네임
    val ingredients: Array<String>, // 재료 이름들
    val amounts: DoubleArray,       // 1인분 기준 양 (재료 순서와 같게)
    val unit: String,               // 단위 (한 레시피 안에서는 하나로 통일)
    val tip: String,                // 만드는 방법·팁
    var likes: Int,                 // 좋아요 수
    var id: String = "",            // [추가] Firestore 문서 ID (기본 레시피는 "base_0", "base_1"...)
    var authorUid: String = ""      // [추가] 작성자 Firebase UID
)

// [추가] 레시피 고유 ID 반환 (기본 레시피는 인덱스 기반, 사용자 등록 레시피는 Firestore 문서 ID)
fun getRecipeId(recipe: Recipe): String {
    if (recipe.id.isNotEmpty()) return recipe.id
    val baseIdx = recipeList.indexOf(recipe)
    if (baseIdx >= 0) {
        recipe.id = "base_$baseIdx"
        return recipe.id
    }
    return ""
}

// [추가] ID로 전체 레시피(기본 + 사용자 등록) 중에서 일치하는 Recipe 찾기
fun findRecipeById(id: String): Recipe? {
    if (id.startsWith("base_")) {
        val idx = id.removePrefix("base_").toIntOrNull()
        if (idx != null && idx in recipeList.indices) {
            recipeList[idx].id = id
            return recipeList[idx]
        }
    }
    return userRecipes.find { it.id == id }
}

// [추가] 앱 시작 시 Firestore 'recipes' 컬렉션 불러오기 + 로그인 상태면 내 북마크/좋아요 불러오기
fun syncFirestoreData(onComplete: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    db.collection("recipes")
        .orderBy("createdAt")
        .get()
        .addOnSuccessListener { result ->
            userRecipes.clear()
            for (doc in result) {
                val ingList = (doc.get("ingredients") as? List<*>)?.map { it.toString() } ?: emptyList()
                val amtList = (doc.get("amounts") as? List<*>)?.mapNotNull { (it as? Number)?.toDouble() } ?: emptyList()

                if (ingList.size == amtList.size && ingList.isNotEmpty()) {
                    val recipe = Recipe(
                        name = doc.getString("name") ?: "",
                        brand = doc.getString("brand") ?: "",
                        tastes = doc.getString("tastes") ?: "",
                        author = doc.getString("author") ?: "익명",
                        ingredients = ingList.toTypedArray(),
                        amounts = amtList.toDoubleArray(),
                        unit = doc.getString("unit") ?: "스푼",
                        tip = doc.getString("tip") ?: "",
                        likes = (doc.getLong("likes") ?: 0L).toInt(),
                        id = doc.id,
                        authorUid = doc.getString("uid") ?: ""
                    )
                    userRecipes.add(recipe)
                }
            }
            loadUserBookmarksAndLikes(onComplete)
        }
        .addOnFailureListener {
            loadUserBookmarksAndLikes(onComplete)
        }
}

// [추가] 현재 로그인된 사용자의 Firestore users/{uid}에서 savedRecipes, likedRecipes 복원
fun loadUserBookmarksAndLikes(onComplete: () -> Unit = {}) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        onComplete()
        return
    }
    FirebaseFirestore.getInstance().collection("users").document(uid).get()
        .addOnSuccessListener { doc ->
            val savedIds = (doc.get("savedRecipes") as? List<*>)?.map { it.toString() } ?: emptyList()
            val likedIds = (doc.get("likedRecipes") as? List<*>)?.map { it.toString() } ?: emptyList()

            savedRecipes.clear()
            for (id in savedIds) {
                findRecipeById(id)?.let { savedRecipes.add(it) }
            }

            likedRecipes.clear()
            for (id in likedIds) {
                findRecipeById(id)?.let { likedRecipes.add(it) }
            }
            onComplete()
        }
        .addOnFailureListener {
            onComplete()
        }
}

// [추가] 북마크(보관함) 토글 결과를 Firestore users/{uid}에 즉시 저장
fun updateBookmarkInFirestore(recipe: Recipe, isSaved: Boolean) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val id = getRecipeId(recipe)
    if (id.isEmpty()) return

    val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
    val updateOp = if (isSaved) FieldValue.arrayUnion(id) else FieldValue.arrayRemove(id)
    userRef.set(mapOf("savedRecipes" to updateOp), SetOptions.merge())
}

// [추가] 보관함 전체 삭제 시 Firestore users/{uid}의 savedRecipes 비우기
fun clearBookmarksInFirestore() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance().collection("users").document(uid)
        .set(mapOf("savedRecipes" to emptyList<String>()), SetOptions.merge())
}

// [추가] 좋아요 토글 결과를 Firestore users/{uid} 및 recipes/{id}에 반영
fun updateLikeInFirestore(recipe: Recipe, isLiked: Boolean) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val id = getRecipeId(recipe)
    if (id.isEmpty()) return

    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)
    val updateOp = if (isLiked) FieldValue.arrayUnion(id) else FieldValue.arrayRemove(id)
    userRef.set(mapOf("likedRecipes" to updateOp), SetOptions.merge())

    // 사용자 등록 레시피라면 recipes 문서의 전체 좋아요 수도 동기화
    if (!id.startsWith("base_")) {
        db.collection("recipes").document(id).update("likes", recipe.likes)
    }
}

// 앱에서 사용할 레시피 목록
// recipeList[0] = 첫 번째 레시피, recipeList[1] = 두 번째 레시피 ...
val recipeList = arrayOf(
    Recipe(
        "고소한 마늘 참깨 소스", "하이디라오", "고소한 맛 · 매운맛", "소스장인",
        arrayOf("참깨소스", "땅콩소스", "다진마늘"),
        doubleArrayOf(2.0, 1.0, 0.5),
        "스푼",
        "소스를 먼저 섞고 다진마늘을 넣어 잘 저어주세요. 고기와 찰떡! 매운맛이 부담되면 마늘을 줄여요.",
        0
    ),
    Recipe(
        "흑당펄 듬뿍 밀크티", "공차", "달달한 맛", "펄러버",
        arrayOf("블랙밀크티", "흑당펄", "치즈폼"),
        doubleArrayOf(1.0, 2.0, 1.0),
        "샷",
        "당도는 30%, 얼음은 적게 주문하면 펄의 단맛이 더 잘 느껴져요.",
        0
    ),
    Recipe(
        "성찬 소스", "하이디라오", "고소", "수정이",
        arrayOf("참깨 소스", "칠리소스", "다진고기", "마늘", "땅콩가루", "대파"),
        doubleArrayOf(2.0, 1.0, 2.0, 1.0, 1.0, 0.5),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 쪽파.",
        0
    ),
    Recipe(
        "한진 소스", "하이디라오", "짭짤 · 자극적", "수정이",
        arrayOf("마늘", "청양고추", "소고기 소스", "마라가루", "참기름", "참깨 소스", "굴소스", "완자 소스"),
        doubleArrayOf(1.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 통깨, 설탕, 산초기름.",
        0
    ),
    Recipe(
        "하이디라오 책임님 소스", "하이디라오", "고소 · 매콤", "수정이",
        arrayOf("참기름", "고추기름", "칠리소스", "마라시즈닝", "산초기름", "태국고추", "굴소스", "통깨"),
        doubleArrayOf(3.0, 2.0, 0.5, 1.0, 2.0, 1.0, 0.5, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "버논 소스", "하이디라오", "매콤 · 짭짤", "수정이",
        arrayOf("칠리소스", "볶음 소고기장", "굴소스", "고추기름", "파", "양파"),
        doubleArrayOf(2.0, 1.0, 1.0, 1.0, 1.0, 2.0),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "건희 소스1", "하이디라오", "매콤 · 달콤", "수정이",
        arrayOf("참깨 소스", "스위트칠리", "볶음 소고기장", "마늘", "파", "고춧가루", "설탕", "고추기름", "다진땅콩", "통깨"),
        doubleArrayOf(1.0, 2.5, 0.5, 0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "건희 소스2", "하이디라오", "짭짤 · 상큼", "수정이",
        arrayOf("양파", "완자간장", "파", "마늘", "굴소스", "고추기름", "중국식초"),
        doubleArrayOf(3.0, 2.0, 1.0, 0.5, 0.5, 0.5, 0.7),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "영지 소스", "하이디라오", "매콤 · 자극적", "수정이",
        arrayOf("참깨 소스", "스위트칠리", "태국고추", "고춧가루", "파", "마늘", "다진고기", "참기름", "간장"),
        doubleArrayOf(2.0, 1.5, 2.0, 1.5, 1.5, 1.0, 1.5, 2.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 통깨, 설탕, 땅콩가루.",
        0
    ),
    Recipe(
        "마크 소스", "하이디라오", "고소", "수정이",
        arrayOf("참깨 소스", "간장", "마늘", "양파", "굴소스", "태국고추"),
        doubleArrayOf(2.0, 2.0, 1.5, 1.5, 1.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 다진고기, 파, 땅콩가루, 고춧가루.",
        0
    ),
    Recipe(
        "쑨디 소스", "하이디라오", "마라 · 자극적", "수정이",
        arrayOf("참깨 소스", "파", "스위트칠리", "마늘", "굴소스", "매운 소고기 소스", "청유훠궈", "다진땅콩", "만구향"),
        doubleArrayOf(0.5, 3.0, 0.5, 3.0, 1.0, 1.0, 2.0, 2.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "연준 소스", "하이디라오", "고소 · 매콤", "수정이",
        arrayOf("참깨 소스", "마늘", "청유훠궈", "스위트칠리", "고추기름", "완자간장", "굴소스", "파", "참기름"),
        doubleArrayOf(1.0, 1.0, 1.0, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "필릭스 소스", "하이디라오", "고소 · 마라", "수정이",
        arrayOf("참깨 소스", "청유훠궈", "마늘", "양파", "마라시즈닝", "파"),
        doubleArrayOf(1.0, 1.0, 1.0, 1.0, 1.0, 0.5),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "쯔양 소스", "하이디라오", "고소 · 매콤", "수정이",
        arrayOf("참깨 소스", "파", "매운 소고기 소스", "청유훠궈", "설탕", "굴소스", "다진땅콩", "마늘", "버섯소스"),
        doubleArrayOf(2.0, 1.0, 1.0, 2.0, 0.3, 0.25, 0.5, 0.5, 0.5),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 고추기름, 태국고추, 튀긴대두.",
        0
    ),
    Recipe(
        "우기 소스", "하이디라오", "고소 · 시큼", "수정이",
        arrayOf("참깨 소스", "중국식초", "파", "다진땅콩", "고수"),
        doubleArrayOf(2.0, 4.0, 1.0, 1.0, 0.5),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "소희 소스", "하이디라오", "고소", "수정이",
        arrayOf("참기름", "마늘", "굴소스", "파", "볶음 소고기장", "스위트칠리"),
        doubleArrayOf(3.0, 1.0, 1.0, 0.5, 0.5, 0.5),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "박은영 소스", "하이디라오", "담백 · 깔끔", "수정이",
        arrayOf("참기름", "중국식초", "마늘"),
        doubleArrayOf(2.0, 1.0, 2.0),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "페이커 소스", "하이디라오", "짭짤 · 마늘", "수정이",
        arrayOf("완자간장", "마늘", "다진땅콩", "파", "굴소스"),
        doubleArrayOf(4.0, 3.0, 1.0, 1.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "석준 소스", "하이디라오", "고소 · 마늘", "수정이",
        arrayOf("참기름", "칠리소스", "마늘", "쪽파", "참깨 소스", "고추기름"),
        doubleArrayOf(3.0, 2.0, 4.0, 1.0, 1.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요.",
        0
    ),
    Recipe(
        "영케이 소스1", "하이디라오", "", "수정이",
        arrayOf("간장", "양파", "마늘", "파", "참기름", "굴소스"),
        doubleArrayOf(3.0, 2.0, 1.0, 1.0, 1.0, 0.5),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 마라가루. 추가 재료: 홍고추 3조각.",
        0
    ),
    Recipe(
        "영케이 소스2", "하이디라오", "고소", "수정이",
        arrayOf("참깨 소스", "땅콩가루", "마늘", "굴소스", "매운 소고기 소스", "건고기"),
        doubleArrayOf(3.0, 1.0, 1.5, 0.33, 1.5, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 마라가루.",
        0
    ),
    Recipe(
        "유우시 소스", "하이디라오", "", "수정이",
        arrayOf("스위트칠리", "참깨 소스", "오향우육", "청유훠궈", "굴소스", "설탕", "매운 소고기 소스", "양념 고춧가루/마라시즈닝", "고추기름", "마늘"),
        doubleArrayOf(1.5, 1.0, 1.0, 1.5, 0.5, 1.0, 1.5, 1.0, 1.0, 1.0),
        "스푼",
        "재료를 모두 섞어 만들어요. 취향껏 조금 넣는 재료: 다진땅콩/튀긴대두, 파, 산초기름.",
        0
    ),
    Recipe(
        "타로 밀크티 + 밀크폼 + 펄", "공차", "고소 · 달콤 · 부드러움", "수정이",
        arrayOf("밀크폼", "펄"),
        doubleArrayOf(1.0, 1.0),
        "개",
        "타로의 달콤하고 고소한 맛에 밀크폼의 부드러움이 어우러져요. 추천 당도는 50%예요.",
        0
    ),
    Recipe(
        "망고 요구르트 + 화이트 펄", "공차", "상큼 · 새콤 · 달콤 · 과일향", "수정이",
        arrayOf("화이트펄"),
        doubleArrayOf(1.0),
        "개",
        "망고의 진하고 달콤한 맛에 요구르트의 새콤함이 더해지고 화이트펄의 쫀득한 식감이 어우러져요. 추천 당도는 0%예요.",
        0
    ),
    Recipe(
        "초콜릿 밀크티 + 밀크폼 + 펄", "공차", "달콤 · 진한 · 고소", "수정이",
        arrayOf("밀크폼", "펄"),
        doubleArrayOf(1.0, 1.0),
        "개",
        "진한 초콜릿의 달콤한 맛과 부드러운 밀크폼이 조화를 이뤄요. 추천 당도는 50%예요.",
        0
    ),
    Recipe(
        "청포도 자스민티 + 밀크폼 + 코코넛", "공차", "상큼 · 달콤 · 향긋", "수정이",
        arrayOf("밀크폼", "코코넛"),
        doubleArrayOf(1.0, 1.0),
        "개",
        "청포도의 상큼하고 달콤한 맛과 자스민의 은은한 꽃향에 밀크폼의 부드러움과 코코넛의 식감이 어우러져요. 추천 당도는 30%예요.",
        0
    ),
    Recipe(
        "패션 프룻 히비스커스 + 알로에/코코넛", "공차", "새콤 · 상큼 · 달콤", "수정이",
        arrayOf("알로에/코코넛"),
        doubleArrayOf(1.0),
        "개",
        "패션프루트의 새콤달콤한 과일 맛과 히비스커스의 향긋함에 알로에 또는 코코넛의 탱글한 식감이 어우러져요. 추천 당도는 30%예요.",
        0
    ),
    Recipe(
        "저당 우롱티 + 치즈폼 + 펄", "공차", "고소 · 짭짤 · 담백", "수정이",
        arrayOf("치즈폼", "펄"),
        doubleArrayOf(1.0, 1.0),
        "개",
        "우롱티의 고소한 풍미에 치즈폼의 짭짤하고 고소한 맛이 더해져요. 추천 당도는 70%예요.",
        0
    ),
    Recipe(
        "블랙티 + 밀크폼 + 화이트펄", "공차", "진한 · 부드러운 · 달콤", "수정이",
        arrayOf("밀크폼", "화이트펄"),
        doubleArrayOf(1.0, 1.0),
        "개",
        "블랙티의 깊고 진한 향에 밀크폼의 부드러움과 화이트펄의 달콤하고 쫀득한 식감이 어우러져요. 추천 당도는 30%예요.",
        0
    ),
    Recipe(
        "딸기 가득 밀크티 + 밀크폼 + 미니펄", "공차", "달콤 · 상큼 · 부드러운", "수정이",
        arrayOf("밀크폼", "미니펄"),
        doubleArrayOf(1.0, 1.0),
        "개",
        "딸기의 달콤하고 상큼한 과일 맛에 밀크티의 고소함과 밀크폼의 부드러움, 미니펄의 쫀득함이 더해진 조합이에요. 추천 당도는 0%예요.",
        0
    ),
    Recipe(
        "딸기 쥬얼리 밀크티 + 치즈폼", "공차", "새콤 · 달콤 · 고소 · 부드러운", "수정이",
        arrayOf("치즈폼"),
        doubleArrayOf(1.0),
        "개",
        "딸기의 상큼, 달콤한 맛과 밀크티의 부드러운 풍미에 치즈폼의 짭짤하고 고소한 맛이 더해진 조합이에요. 추천 당도는 50%예요.",
        0
    ),
    Recipe(
        "우롱티 + 치즈폼 + 화이트펄", "공차", "고소 · 짭짤", "수정이",
        arrayOf("치즈폼", "화이트펄"),
        doubleArrayOf(1.0, 1.0),
        "개",
        "우롱티의 고소하고 깔끔한 맛에 치즈폼의 짭짤한 풍미와 화이트펄의 달콤하고 쫀득한 식감이 어우러져요. 추천 당도는 0~30%예요.",
        0
    ),
    Recipe(
        "미니펄 망고 밀크 + 리얼 망고", "공차", "달콤 · 진한 · 과일향", "수정이",
        arrayOf("리얼 망고"),
        doubleArrayOf(1.0),
        "개",
        "망고의 진하고 달콤한 풍미와 미니펄의 쫀득함이 더해져 다채로운 식감을 즐길 수 있는 조합이에요. 추천 당도는 50%예요.",
        0
    ),
    Recipe(
        "자몽 요구르트 + 코코넛/화이트펄", "공차", "상큼 · 새콤 · 달콤 · 산뜻", "수정이",
        arrayOf("코코넛/화이트펄"),
        doubleArrayOf(1.0),
        "개",
        "자몽 특유의 상큼, 쌉싸름한 맛에 요구르트의 새콤달콤함과 코코넛 또는 화이트펄의 쫀득한 식감이 더해진 조합이에요. 추천 당도는 0%예요.",
        0
    )
)