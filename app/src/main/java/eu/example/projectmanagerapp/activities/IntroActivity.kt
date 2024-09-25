package eu.example.projectmanagerapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import eu.example.projectmanagerapp.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding?.btnSignUp?.setOnClickListener{
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }

        binding?.btnSingIn?.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

    }
}