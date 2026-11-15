package com.example.mp_customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 목록 카드에 보여줄 정보 (기존 Recipe 클래스와 이름이 겹치지 않게 RecipeCard로 함)
data class RecipeCard(
    val id: Int,
    val name: String,
    val brand: String,
    val tastes: String,
    val likes: Int,
    val saved: Boolean = false   // 저장 여부
)

class RecipeAdapter(
    private var items: List<RecipeCard>,
    private val onClick: (RecipeCard) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgCard: ImageView = view.findViewById(R.id.imgCard)
        val brand: TextView = view.findViewById(R.id.txtCardBrand)
        val name: TextView = view.findViewById(R.id.txtCardName)
        val tastes: TextView = view.findViewById(R.id.txtCardTastes)
        val likes: TextView = view.findViewById(R.id.txtCardLikes)
        val imgSave: ImageView = view.findViewById(R.id.imgCardSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.brand.text = r.brand
        holder.name.text = r.name
        holder.tastes.text = r.tastes
        holder.likes.text = r.likes.toString()
        holder.imgSave.setImageResource(
            if (r.saved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
        )
        holder.itemView.setOnClickListener { onClick(r) }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<RecipeCard>) {
        items = newItems
        notifyDataSetChanged()
    }
}