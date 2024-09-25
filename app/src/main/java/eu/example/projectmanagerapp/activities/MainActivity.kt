package eu.example.projectmanagerapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.adapters.BoardItemsAdapter
import eu.example.projectmanagerapp.databinding.ActivityMainBinding
import eu.example.projectmanagerapp.firebase.FireStoreClass
import eu.example.projectmanagerapp.models.Board
import eu.example.projectmanagerapp.models.User
import eu.example.projectmanagerapp.utils.Constants

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences

    companion object{
        const val MY_PROFILE_REQUEST_CODE : Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpActionBar()
        onBackPressedNew()

        binding?.navView?.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(
            Constants.PROJECT_MANAGER_PREFERENCES,Context.MODE_PRIVATE)



        FireStoreClass().loadUserData(this,true)

        val floatActionBtn =
            findViewById<FloatingActionButton>(R.id.fab_create_board)

        floatActionBtn.setOnClickListener{

            val intent = Intent(this@MainActivity,
                CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }



    }

    fun populateBoardListToUI(boardList: ArrayList<Board>){
        hideProgressDialog()

        val rvBoardList = findViewById<RecyclerView>(R.id.rv_boards_list)
        val tvNoBoardAvailable = findViewById<TextView>(R.id.tv_no_boards_available)
        if (boardList.size>0){
            rvBoardList.visibility = View.VISIBLE
            tvNoBoardAvailable.visibility = View.GONE

            rvBoardList.layoutManager = LinearLayoutManager(this@MainActivity)
            rvBoardList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this@MainActivity,boardList)
            rvBoardList.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {

                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })

        }else{
            rvBoardList.visibility = View.GONE
            tvNoBoardAvailable.visibility = View.VISIBLE
        }
    }

    private fun setUpActionBar(){
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }

    }

    private fun toggleDrawer(){
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }


    private fun onBackPressedNew(){
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Your logic for handling back presses
                if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
                    binding?.drawerLayout?.closeDrawer(GravityCompat.START)
                } else {
                    doubleBackToExit()
                }
            }
        })
    }


    fun updateNavigationUserDetails(user: User, readBoardsList:Boolean){
        hideProgressDialog()
        mUserName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.nav_user_image))

        val tvUsername = findViewById<TextView>(R.id.tv_username)
        tvUsername.text = user.name
        tvUsername.visibility = View.VISIBLE

        if(readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this@MainActivity)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserData(this@MainActivity)
        }else if(resultCode == RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FireStoreClass().getBoardsList(this@MainActivity)
        } else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_my_profile ->{
                startActivityForResult(Intent(this@MainActivity,
                    MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this@MainActivity,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        }
        binding?.drawerLayout!!.closeDrawer(GravityCompat.START)

        return true
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().loadUserData(this,true)
    }


}