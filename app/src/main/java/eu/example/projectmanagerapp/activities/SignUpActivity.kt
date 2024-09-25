package eu.example.projectmanagerapp.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.databinding.ActivitySignUpBinding
import eu.example.projectmanagerapp.firebase.FireStoreClass
import eu.example.projectmanagerapp.models.User

class SignUpActivity : BaseActivity() {

    private var binding:ActivitySignUpBinding? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setUpActionBar()

    }

    fun userRegisteredSuccess(){

        Toast.makeText(this@SignUpActivity,
            "you have successfully registered the",
            Toast.LENGTH_LONG).show()

        hideProgressDialog()

        FirebaseAuth.getInstance().signOut()
        finish()

    }


    private fun setUpActionBar(){
        setSupportActionBar(binding?.toolbarSignUpActivity)

        val actionBar= supportActionBar

        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back)
        }

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding?.btnSignUp?.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim{it <= ' ' }
        val email: String = binding?.etEmail?.text.toString().trim{it<=' '}
        val password:String = binding?.etPassword?.text.toString().trim{it<=' '}

        if(validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email,password).addOnCompleteListener  { task ->

                        if (task.isSuccessful){
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid,name,registeredEmail)

                            FireStoreClass().registerUser(this,user)

                        }else{
                            Toast.makeText(this@SignUpActivity,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun validateForm(name: String,email : String, password: String):Boolean{

        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }else->{
                true
            }
        }
    }
}