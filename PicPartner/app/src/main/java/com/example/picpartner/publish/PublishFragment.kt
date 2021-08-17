package com.example.picpartner.publish

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.picpartner.R
import com.example.picpartner.User
import com.example.picpartner.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.example.picpartner.collection.Collection
import com.example.picpartner.databinding.FragmentPublishBinding


class PublishFragment : Fragment() {
    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    val dataRef =
        Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
            "Person"
        )
    val storageRef = FirebaseStorage.getInstance().reference.child("collection")

    private lateinit var editTitle:EditText
    private lateinit var imageUri: Uri
    private lateinit var choose: MaterialButton
    private lateinit var upload: MaterialButton
    private var imageUriList: ArrayList<Uri> = arrayListOf()
    private lateinit var myToast: TextView
    private lateinit var recyclerView: RecyclerView
    private var adapter = ImgAdapter()
    private var realtimeUser: User? = null
    private lateinit var progressBar: ProgressBar
    private var uploadCount = 0


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
        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        val userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.fragment)

        editTitle = binding.editCollectionTitle
        choose = binding.chooseButton
        upload = binding.uploadButton
        upload.visibility = View.INVISIBLE
        recyclerView = binding.chooseImageGrid
        myToast = binding.myToast

        realtimeUser = userViewModel.currentUser.value

        progressBar = binding.chosenUploadProgress
        progressBar.visibility = View.INVISIBLE


        // publish
        choose.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, PICK_MULT_IMAGE)
        }

        upload.setOnClickListener {
            uploadToFirebase(realtimeUser!!, imageUriList)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageUriList.clear()
        if (requestCode == PICK_MULT_IMAGE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                if (data!!.clipData != null) {
                    val countClipData = data.clipData!!.itemCount
                    var currentImgSelect = 0
                    while (currentImgSelect < countClipData) {
                        imageUri = data.clipData!!.getItemAt(currentImgSelect).uri
                        imageUriList.add(imageUri)
                        currentImgSelect += 1
                    }
                    myToast.text = "您已選擇 ${imageUriList.size} 張照片"
                    upload.visibility = View.VISIBLE
                } else {
                    //pick single image
                    val imageUri = data.data
                    imageUriList.add(imageUri!!)
                    myToast.text = "您已選擇 ${imageUriList.size} 張照片"
                    upload.visibility = View.VISIBLE
                }
                recyclerView.adapter = adapter
                adapter.submitList(imageUriList)
            }
        }
    }

    private fun uploadToFirebase(realtimeUser: User, imageUriList: MutableList<Uri>) {
        val userId = realtimeUser.uid
        val downloadList = mutableListOf<String>()
        var key = dataRef.child(userId!!).child("collection").push().key

        for (image in imageUriList) {
            val fileRef = storageRef.child(
                System.currentTimeMillis().toString() + "." + getFileExtension(image)
            )
            fileRef.putFile(image).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener {
                    progressBar.visibility = View.INVISIBLE
                    val url = it.toString()
                    println("url:$url")
                    downloadList.add(url)
                    println("downloadList:${downloadList.size}")
                    recyclerView.adapter = null
                    upload.visibility = View.INVISIBLE
                    choose.visibility = View.INVISIBLE
                    myToast.text = "${imageUriList.size} 張照片已上傳成功!"
                    val title = editTitle.text.toString()
                    if (downloadList.size == imageUriList.size) {
                        val dList = downloadList.toList()
                        val collect = Collection(title, dList)
                        dataRef.child(userId).child("collection").child(key!!).setValue(collect)
                        navController.navigate(R.id.action_publishFragment_to_profileFragment)
                    }
                }
            }.addOnProgressListener {
                progressBar.visibility = View.VISIBLE
            }.addOnFailureListener {
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileExtension(mUri: Uri): String? {
        val cr: ContentResolver = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(mUri))
    }

    companion object {
        const val PICK_MULT_IMAGE = 2

        @JvmStatic
        fun newInstance() =
            PublishFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}