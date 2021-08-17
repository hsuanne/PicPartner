package com.example.picpartner.profile

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.picpartner.*
import com.example.picpartner.collection.Collection
import com.example.picpartner.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
    private var _binding:FragmentProfileBinding?=null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private lateinit var name:TextView
    private lateinit var email:TextView
    private lateinit var imageUri: Uri
    private lateinit var pic:ImageView
    private lateinit var setting:ImageView
    private lateinit var publish:MaterialButton
    val dataRef = Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("Person")
    val storageRef = FirebaseStorage.getInstance().getReference()
    private lateinit var progressBar:ProgressBar
    private var realtimeUser: User?=null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:CollectionAdapter
    private lateinit var collectionList: MutableList<Collection>

    private lateinit var numCollection: TextView
    private lateinit var numFollower:TextView
    private lateinit var numFollowing:TextView


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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.fragment)
        adapter = CollectionAdapter(navController, userViewModel, "own")

        name = binding.googleName
        email = binding.googleEmail
        pic = binding.profilePic
        setting = binding.settingIcon
        publish = binding.publishButton

        numCollection = binding.numCollection
        numFollower = binding.numFollower
        numFollowing = binding.numFollowing

        recyclerView = binding.collectionGrid

        progressBar = binding.uploadProgress
        progressBar.visibility = View.INVISIBLE


        val signInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(activity)
        if (signInAccount != null){
            name.text = signInAccount.displayName
            email.text = signInAccount.email
        }

        // TODO: 改成從realtime db拿資料
        val user = FirebaseAuth.getInstance().currentUser
        if (user!=null){
            var provider=user.providerData[0].providerId
            var glideUrl=""
            if (user.providerData.size>1) {
                provider = user.providerData[1].providerId
            }

            when(provider){
                "google.com" -> {
                    name.text = String.format(resources.getString(R.string.user_name), user.displayName)
                    email.text = String.format(resources.getString(R.string.user_email), user.email)
                    val newUrl = user.photoUrl.toString()
                    glideUrl = newUrl.substring(0, newUrl.length - 5) + "s400-c"
                }
                "facebook.com" -> {
                    name.text = String.format(resources.getString(R.string.user_name), user.displayName)
                    email.text = String.format(resources.getString(R.string.user_email), user.email)
                    val newUrl = user.photoUrl.toString()
                    glideUrl = "$newUrl?height=500"
                    Glide.with(requireContext()).load(glideUrl).into(pic)
                }
                else -> {
                    getUserData(user, userViewModel)
                    getFollower(user.uid)
                    getFollowing(user.uid)
                }
            }
        }

        // select profile image
        pic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        // change setting
        setting.setOnClickListener {
            navController.navigate(R.id.action_profileFragment_to_settingFragment)
        }

        // publish change fragment
        publish.setOnClickListener {
            navController.navigate(R.id.action_profileFragment_to_publishFragment)
        }

        return binding.root
    }

    private fun uploadToFirebase(realtimeUser: User, uri:Uri) {
       val fileRef = storageRef.child(System.currentTimeMillis().toString() + "." + getFileExtension(uri))
        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener {
                progressBar.visibility = View.INVISIBLE
                realtimeUser.imageUrl = it.toString()
                val userId = realtimeUser.uid
                if (userId != null) {
                    dataRef.child(userId).setValue(realtimeUser)
                }
            }
        }.addOnProgressListener {
            progressBar.visibility = View.VISIBLE
        }.addOnFailureListener {
            progressBar.visibility = View.INVISIBLE
            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileExtension(mUri:Uri):String? {
        val cr:ContentResolver = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(mUri))
    }

    private fun getUserData(user:FirebaseUser, userViewModel: UserViewModel) {
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        if (user.uid == dataSnapshot.key){
                            realtimeUser = dataSnapshot.getValue(User::class.java)!!
                            userViewModel.currentUser.value = realtimeUser
                            userViewModel.currentRegular.value = realtimeUser
                            name.text = realtimeUser!!.name
//                            email.text = String.format(resources.getString(R.string.user_email), realtimeUser!!.email)
                            if (realtimeUser!!.imageUrl!=null){
                                pic.scaleType = ImageView.ScaleType.CENTER_CROP
                                Glide.with(activity!!).load(realtimeUser!!.imageUrl).into(pic)
                            } else {
                                pic.scaleType = ImageView.ScaleType.CENTER
                                pic.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
                            }
                            getCollection(user.uid)
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()
            }
        }
        dataRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun getCollection(uid:String) {
        val collectionRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("Person").child(uid).child("collection")
        println(collectionRef.toString())
        collectionList = mutableListOf()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data!=null){
            imageUri = data.data!!
            pic.setImageURI(imageUri)
            if (realtimeUser!=null){
                uploadToFirebase(realtimeUser!!, imageUri)
            } else {
                Toast.makeText(activity, "please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val PICK_IMAGE = 1

        @JvmStatic
        fun newInstance() =
            ProfileFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}