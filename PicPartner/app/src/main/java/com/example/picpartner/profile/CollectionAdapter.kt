package com.example.picpartner.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.picpartner.collection.Collection
import com.example.picpartner.R
import com.example.picpartner.UserViewModel

class CollectionAdapter(val navController: NavController, var userViewModel: UserViewModel, val fragmentName:String): ListAdapter<Collection, RecyclerView.ViewHolder>(CollectionDiffCallback()) {
    inner class CollectionViewHolder(view: View):RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.collection_image)
        val title:TextView = view.findViewById(R.id.collection_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.collection_container, parent, false)
        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is CollectionViewHolder -> {
                val collection = getItem(position)
                holder.title.text = collection.title
                Glide.with(holder.itemView).load(collection.imgUrl?.get(0)).into(holder.imageView)

                holder.imageView.setOnClickListener {
                    userViewModel.currentCollection.value = collection
                    if (fragmentName == "own") {
                        navController.navigate(R.id.action_profileFragment_to_collectionMainFragment)
                    } else if (fragmentName == "regular") {
                        navController.navigate(R.id.action_profileRegularFragment_to_collectionMainFragment)
                    }
                }
            }
        }
    }
}

class CollectionDiffCallback: DiffUtil.ItemCallback<Collection>() {
    override fun areItemsTheSame(oldItem: Collection, newItem: Collection): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Collection, newItem: Collection): Boolean {
        return oldItem == newItem
    }
}