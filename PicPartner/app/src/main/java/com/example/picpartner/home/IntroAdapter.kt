package com.example.picpartner.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.picpartner.R
import com.example.picpartner.User
import com.example.picpartner.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class IntroAdapter(fragmentActivity: FragmentActivity, var userViewModel: UserViewModel):ListAdapter<User, RecyclerView.ViewHolder>(DiffCallback()) {
    val navController = Navigation.findNavController(fragmentActivity, R.id.fragment)
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    inner class ImageViewHolder(view: View):RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.intro_image)
        val name:TextView = view.findViewById(R.id.intro_name)
        val cardView: CardView = view.findViewById(R.id.imageSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_container, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ImageViewHolder -> {
                val user = getItem(position)
                holder.name.text = user.name
                Glide.with(holder.itemView).load(user.imageUrl).into(holder.imageView)
                holder.cardView.setOnClickListener {
                    if (user.uid == firebaseUser.uid){
                        userViewModel.currentRegular.value = user
                        navController.navigate(R.id.action_homeFragment_to_profileFragment)
                    } else {
                        startProfile(user, userViewModel)
                    }
                }
            }
        }
    }

    private fun startProfile(user:User, userViewModel:UserViewModel) {
        userViewModel.currentRegular.value = user
        navController.navigate(R.id.action_homeFragment_to_profileRegularFragment)
    }
}

class DiffCallback: DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.name == newItem.name
    }

}
