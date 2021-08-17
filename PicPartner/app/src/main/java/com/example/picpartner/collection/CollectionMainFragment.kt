package com.example.picpartner.collection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.picpartner.R
import com.example.picpartner.UserViewModel
import com.example.picpartner.databinding.FragmentCollectionMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class CollectionMainFragment : Fragment() {
    private var _binding:FragmentCollectionMainBinding?=null
    private val binding get() = _binding!!

    private lateinit var deleteButton: Button
    private lateinit var title:TextView
    private lateinit var viewPager2:ViewPager2
    private lateinit var adapter: CollectionMainAdapter

    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    val dataRef = Firebase
        .database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference.child("Person")


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
        _binding = FragmentCollectionMainBinding.inflate(inflater, container, false)
        val userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        deleteButton = binding.deleteCollectionButton
        title = binding.collectionMainTitle
        viewPager2 = binding.collectionMainViewpager2
        adapter = CollectionMainAdapter()

        userViewModel.currentCollection.observe(viewLifecycleOwner){
            title.text = it.title
            println(it.imgUrl?.get(0))
            viewPager2.adapter = adapter
            adapter.submitList(it.imgUrl)
        }

        if (userViewModel.currentUser.value?.uid == userViewModel.currentRegular.value?.uid){
            deleteButton.visibility = View.VISIBLE
        }

        deleteButton.setOnClickListener {
            if (currentUser != null) {
                deleteCollectionToFirebase(currentUser)
            }
        }

        return binding.root
    }

    private fun deleteCollectionToFirebase(currentUser: FirebaseUser) {
        val currentId = currentUser.uid
        val collectionRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("Person").child(currentId).child("collection")

        val eventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        val collection = dataSnapshot.getValue(Collection::class.java)
                        val key = dataSnapshot.key
                        if (collection?.title == title.text){
                            collectionRef.child(key!!).removeValue()
                            findNavController(requireActivity(), R.id.fragment).popBackStack()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()
            }

        }
        collectionRef.addListenerForSingleValueEvent(eventListener)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CollectionMainFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}