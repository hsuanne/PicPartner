package com.example.picpartner

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.picpartner.databinding.FragmentSettingBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private lateinit var logout: TextView
    private lateinit var editComplete: TextView
    private lateinit var editName: TextInputEditText
    private lateinit var editIdentity: AutoCompleteTextView
    private var currentUser:User?=null
    val dataRef = Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("Person")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.fragment)

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            currentUser = it
            if (currentUser!=null) {
                editName.text = SpannableStringBuilder(currentUser!!.name)
            }
        }

        logout = binding.logoutTextview
        editComplete = binding.editComplete
        editIdentity = binding.identitySelect
        editName = binding.editName

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(activity, StartActivity::class.java))
        }

        editComplete.setOnClickListener {
            updateUser(currentUser!!)
        }

        return binding.root
    }

    private fun updateUser(currentUser:User) {
        val name = editName.text.toString()
        val identity = editIdentity.text.toString()
        if (name.isEmpty()){
            editName.error = "名字長度不得為 0"
            editName.requestFocus()
        } else if (identity.isEmpty()){
            editIdentity.error = "請選擇一個身分"
            editIdentity.requestFocus()
        } else {
            currentUser.name = name
            currentUser.identity = identity
            val userId = currentUser.uid
            if (userId != null) {
                dataRef.child(userId).setValue(currentUser)
            }
            navController.navigate(R.id.action_settingFragment_to_profileFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        val identities = resources.getStringArray(R.array.identity)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_items, identities)
        editIdentity.setAdapter(arrayAdapter)
        if (currentUser?.identity!=null && currentUser?.identity!!.isNotEmpty()) {
            editIdentity.setText(currentUser?.identity, false)
        } else {
            editIdentity.setText("請選擇身分", false)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}