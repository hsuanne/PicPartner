package com.example.picpartner.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.picpartner.MainActivity
import com.example.picpartner.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginTabFragment : Fragment() {
    private val transX = 800f
    private val alpha = 0f
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText

    val dataRef =
        Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
            "Person"
        )
    val storageRef = FirebaseStorage.getInstance().getReference()

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
        val view = inflater.inflate(R.layout.fragment_login_tab, container, false)

        email = view.findViewById(R.id.edit_email)
        password = view.findViewById(R.id.edit_password)
        val forgetPassword: TextView = view.findViewById(R.id.forget_password)
        val login: AppCompatButton = view.findViewById(R.id.login_button)

        email.translationX = transX
        password.translationX = transX
        forgetPassword.translationX = transX
        login.translationX = transX

        email.alpha = alpha
        password.alpha = alpha
        forgetPassword.alpha = alpha
        login.alpha = alpha

        email.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(300).start()
        password.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        forgetPassword.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(800)
            .start()
        login.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(1000).start()

        auth = FirebaseAuth.getInstance()

        login.setOnClickListener {
            loginUser()
        }


        return view
    }

    private fun loginUser() {
        val email = email.text.toString()
        val password = password.text.toString()

        if (TextUtils.isEmpty(email)) {
            this.email.error = "Email cannot be empty!"
            this.email.requestFocus()
        } else if (TextUtils.isEmpty(password)) {
            this.password.error = "Password cannot be empty!"
            this.password.requestFocus()
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(activity, "Email Password Login Success.", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(activity, MainActivity::class.java))
                } else {
                    Toast.makeText(activity, "login error:" + it.exception, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginTabFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            LoginTabFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}