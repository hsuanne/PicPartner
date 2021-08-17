package com.example.picpartner.profile

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.picpartner.R
import com.example.picpartner.User
import com.example.picpartner.UserViewModel
import com.example.picpartner.collection.Collection
import com.example.picpartner.databinding.FragmentProfileRegularBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class ProfileRegularFragment : Fragment() {
    private var _binding: FragmentProfileRegularBinding?=null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var imageUri: Uri
    private lateinit var pic: ImageView
    private lateinit var setting: ImageView
    private lateinit var follow: MaterialButton

    val dataRef = Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("Person")
    val storageRef = FirebaseStorage.getInstance().getReference()
    private lateinit var progressBar: ProgressBar
    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private lateinit var regularUser: User

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:CollectionAdapter
    private lateinit var collectionList: MutableList<Collection>

    private lateinit var numCollection: TextView
    private lateinit var numFollower:TextView
    private lateinit var numFollowing:TextView

    private var isFollowed:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileRegularBinding.inflate(inflater, container, false)
        val userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.fragment)
        adapter = CollectionAdapter(navController, userViewModel, "regular")

        name = binding.regularName
        email = binding.regularEmail
        pic = binding.profilePic
        follow = binding.followButton

        numCollection = binding.numCollection
        numFollower = binding.numFollower
        numFollowing = binding.numFollowing

        recyclerView = binding.collectionGrid

        progressBar = binding.uploadProgress
        progressBar.visibility = View.INVISIBLE

        val unfollowDialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage("確定取消關注?")
            .setPositiveButton("確認"){ _, _ ->
//                Toast.makeText(requireContext(), "確認", Toast.LENGTH_SHORT).show()
                if (currentUser != null) {
                    unFollowToFirebase(currentUser, regularUser)
                    getFollower(regularUser.uid!!)
                    getFollowing(regularUser.uid!!)
                }
            }
            .setNegativeButton("取消"){ _, _ ->
//                Toast.makeText(requireContext(), "取消", Toast.LENGTH_SHORT).show()
            }
            .create()

        userViewModel.currentRegular.observe(viewLifecycleOwner){
            regularUser = it
            getCollection(it.uid!!)
            getFollower(it.uid!!)
            getFollowing(it.uid!!)
            //檢查是否已追蹤
            isFollowed(currentUser!!.uid,it.uid!!)
            name.text = it.name
            Glide.with(this).load(it.imageUrl).into(pic)
        }

        follow.setOnClickListener {
            if (currentUser!=null){
                if (!isFollowed) {
                    //關注
                    addFollowToFirebase(currentUser, regularUser)
                    getFollower(regularUser.uid!!)
                    getFollowing(regularUser.uid!!)
                } else {
                    //取消關注
                    unfollowDialog.show()
                }
            }
        }

        return binding.root
    }

    private fun addFollowToFirebase(currentUser:FirebaseUser, regularUser:User) {
        val currentId = currentUser.uid
        val regularId = regularUser.uid!!
        dataRef.child(currentId).child("following").child(regularId).setValue(User(regularId))
        dataRef.child(regularId).child("follower").child(currentId).setValue(User(currentId))
        isFollowed = true
        follow.text = "已追蹤"
        follow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pink))
    }

    private fun unFollowToFirebase(currentUser:FirebaseUser, regularUser:User) {
        val currentId = currentUser.uid
        val regularId = regularUser.uid!!
        dataRef.child(currentId).child("following").child(regularId).removeValue()
        dataRef.child(regularId).child("follower").child(currentId).removeValue()
        isFollowed = false
        follow.text = "追蹤"
        follow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun getCollection(uid:String){
        val collectionRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("Person").child(uid).child("collection")
        val collectionList = mutableListOf<Collection>()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val collection = dataSnapshot.getValue(Collection::class.java)
                        println("collection_imgUrl_size: " + collection?.imgUrl?.size)
                        collectionList.add(collection!!)
                    }
                }
                numCollection.text = collectionList.size.toString()
                recyclerView.adapter = adapter
                adapter.submitList(collectionList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()
            }
        }
        collectionRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun getFollower(uid:String){
        val followerRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("Person").child(uid).child("follower")
        val followerList = mutableListOf<User>()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val follower = dataSnapshot.getValue(User::class.java)
                        followerList.add(follower!!)
                    }
                }
                numFollower.text = followerList.size.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()
            }
        }
        followerRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun getFollowing(uid:String){
        val followingRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("Person").child(uid).child("following")
        val followingList = mutableListOf<User>()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val following = dataSnapshot.getValue(User::class.java)
                        followingList.add(following!!)
                    }
                }
                numFollowing.text = followingList.size.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()
            }
        }
        followingRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun isFollowed(uid:String, regUid:String){
        val followingRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("Person").child(uid).child("following")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val following = dataSnapshot.getValue(User::class.java)
                        if (following?.uid == regUid){
                            isFollowed = true
                            follow.text = "已追蹤"
                            follow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pink))
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()
            }
        }
        followingRef.addListenerForSingleValueEvent(eventListener)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileRegularFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}