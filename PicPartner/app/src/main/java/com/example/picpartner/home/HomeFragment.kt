package com.example.picpartner.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.picpartner.News
import com.example.picpartner.User
import com.example.picpartner.UserViewModel
import com.example.picpartner.databinding.FragmentHomeBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import me.relex.circleindicator.CircleIndicator3


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private val imageRef = FirebaseStorage.getInstance().reference
    private lateinit var viewPager2: ViewPager2
    private lateinit var indicator: CircleIndicator3
    private lateinit var dataRef: DatabaseReference
    private lateinit var modelRecyclerView: RecyclerView
    private lateinit var photographerRecyclerView: RecyclerView
    private lateinit var mList: MutableList<News>
    private lateinit var userList: MutableList<User>
    private lateinit var modelList: MutableList<User>
    private lateinit var photographerList: MutableList<User>

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        viewPager2 = binding.newsViewpager2
        indicator = binding.newsIndicator

        modelRecyclerView = binding.modelRecyclerview
        modelRecyclerView.setHasFixedSize(true)

        photographerRecyclerView = binding.photographerRecyclerview
        photographerRecyclerView.setHasFixedSize(true)

        // TODO: 自動輪播圖片
        getNews()

        // TODO: query only specific people
        getPerson(userViewModel)

        return binding.root
    }

//    override fun onResume() {
//        super.onResume()
//        navController = findNavController(requireActivity(), R.id.fragment)
//    }

    private fun getNews() {
        dataRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "News"
            )
        mList = mutableListOf()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val news = dataSnapshot.getValue(News::class.java)
                        mList.add(news!!)
                    }
                }
                viewPager2.adapter = MyAdapter(mList)
                indicator.setViewPager(viewPager2)
                indicator.createIndicators(mList.size, 0)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()

            }
        }
        dataRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun getPerson(userViewModel:UserViewModel) {
        dataRef =
            Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "Person"
            )
        val query = dataRef.orderByChild("name").limitToFirst(10)
//        userList = mutableListOf()
        modelList = mutableListOf()
        photographerList = mutableListOf()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val user = dataSnapshot.getValue(User::class.java)
//                            userList.add(user!!)
                        if (user?.identity == "模特兒") {
                            modelList.add(user)
                        }
                        if (user?.identity == "攝影師") {
                            photographerList.add(user)
                        }
                    }
                }
                val modelAdapter = IntroAdapter(requireActivity(),userViewModel)
                modelRecyclerView.adapter = modelAdapter
                modelAdapter.submitList(modelList)

                val photographerAdapter = IntroAdapter(requireActivity(),userViewModel)
                photographerRecyclerView.adapter = photographerAdapter
                photographerAdapter.submitList(photographerList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DatabaseError:$error", Toast.LENGTH_SHORT).show()

            }
        }
        dataRef.addListenerForSingleValueEvent(eventListener)
//        query.addValueEventListener(eventListener)
    }

//    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
//        val imageUrls = mutableListOf<String>()
//        try {
//            val images =
//                imageRef.child("news_placeholder/horizontal_placeHolder1.jpg").downloadUrl.addOnSuccessListener {
//
//                    imageUrls.add(it.toString())
//                    val newsAdapter = IntroAdapter()
//                    viewPager2.adapter = newsAdapter
//                    newsAdapter.submitList(imageUrls)
//                    indicator.createIndicators(imageUrls.size, 0)
//                }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
//            }
//        }
//    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}