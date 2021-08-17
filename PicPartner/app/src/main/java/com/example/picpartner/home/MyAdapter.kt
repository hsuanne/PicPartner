package com.example.picpartner.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.picpartner.News
import com.example.picpartner.R

class MyAdapter(private val mList:MutableList<News>):RecyclerView.Adapter<MyAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_container, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = mList[position]
        Glide.with(holder.itemView).load(currentItem.url).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ImageViewHolder(view: View):RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.news_image)
    }
}
