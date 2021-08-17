package com.example.picpartner.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.picpartner.MainActivity
import com.example.picpartner.R
import com.example.picpartner.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignupTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignupTabFragment : Fragment() {
    private val transX = 300f
    private val alpha = 0f
    private lateinit var auth:FirebaseAuth
    private lateinit var name:EditText
    private lateinit var email:EditText
    private lateinit var mobile:EditText
    private lateinit var password:EditText
    private lateinit var passwordConfirm:EditText
    val dataRef =
        Firebase.database("https://application-login-c0b0a-default-rtdb.asia-southeast1.firebasedatabase.app")
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
        val view = inflater.inflate(R.layout.fragment_signup_tab, container, false)
        name = view.findViewById(R.id.edit_name)
        email = view.findViewById(R.id.edit_email)
        password = view.findViewById(R.id.edit_password)
        passwordConfirm = view.findViewById(R.id.edit_password_confirm)
        val login: AppCompatButton = view.findViewById(R.id.signup_button)

        name.translationX = transX
        email.translationX = transX
        password.translationX = transX
        passwordConfirm.translationX = transX
        login.translationX = transX

        name.alpha = alpha
        email.alpha = alpha
        password.alpha = alpha
        passwordConfirm.alpha = alpha
        login.alpha = alpha

        name.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(100).start()
        email.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(300).start()
        password.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(500).start()
        passwordConfirm.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(700).start()
        login.animate().translationX(0f).alpha(1f).setDuration(800).setStartDelay(900).start()

        auth = FirebaseAuth.getInstance()
        login.setOnClickListener {
            createUser()
        }

        return view
    }

    private fun createUser() {
        val name = name.text.toString()
        val email = email.text.toString()
        val password = password.text.toString()
        val passwordConfirm = passwordConfirm.text.toString()

        if (TextUtils.isEmpty(name)){
            this.name.error = "暱稱不可空白!"
            this.name.requestFocus()
        } else if (TextUtils.isEmpty(email)){
            this.email.error = "Email不可空白!"
            this.email.requestFocus()
        } else if (TextUtils.isEmpty(password)){
            this.password.error = "密碼不可空白!"
            this.password.requestFocus()
        } else if (TextUtils.isEmpty(passwordConfirm)){
            this.passwordConfirm.error = "請再次輸入密碼!"
            this.passwordConfirm.requestFocus()
        } else if (password != passwordConfirm){
            this.passwordConfirm.error = "密碼不吻合!"
            this.passwordConfirm.requestFocus()
        } else{
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(activity, "user registered successfully", Toast.LENGTH_SHORT)

                    //直接登入
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(activity, "user login successfully", Toast.LENGTH_SHORT).show()
                            val currentUser = auth.currentUser
                            val user = User(currentUser.uid, name, currentUser.email)
                            uploadToFirebase(user)

                            startActivity(Intent(activity, MainActivity::class.java))
                        } else {
                            Toast.makeText(activity, "login error:"+it.exception, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(activity, "registration error:"+it.exception, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadToFirebase(user: User) {
        val userId = user.uid
        if (userId != null) {
            dataRef.child(userId).setValue(user)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignupTabFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            SignupTabFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}