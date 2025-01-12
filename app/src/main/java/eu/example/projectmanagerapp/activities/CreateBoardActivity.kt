package eu.example.projectmanagerapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.databinding.ActivityCreateBoardBinding
import eu.example.projectmanagerapp.firebase.FireStoreClass
import eu.example.projectmanagerapp.models.Board
import eu.example.projectmanagerapp.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var binding : ActivityCreateBoardBinding? = null

    private var mSelectedImageFileUri :Uri? = null

    private lateinit var mUserName :String

    private var mBoardImageURL :String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding?.ivBoardImage?.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@CreateBoardActivity)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnCreate?.setOnClickListener {
            if(mSelectedImageFileUri !=null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
            }
        }


    }

    private fun createBoard(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())

        val board = Board(
            binding?.etBoardName?.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        FireStoreClass().createBoard(this@CreateBoardActivity,board)
    }


    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        val sRef : StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis()
                        + "." + Constants.getFileExtension(this@CreateBoardActivity,mSelectedImageFileUri))

        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->

            Log.i("Board Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                Log.i("Downloadable Image URL",uri.toString())
                mBoardImageURL = uri.toString()
                hideProgressDialog()

                createBoard()

            }
        }.addOnFailureListener {
                exception ->
            Toast.makeText(this@CreateBoardActivity,
                exception.message,Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }



    private fun setUpActionBar(){
        setSupportActionBar(binding?.toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()&&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@CreateBoardActivity)
            }
        }else{
            Toast.makeText(this,
                "Oops, you just denied the permission for storage." +
                        " You can also allow it from settings.",
                Toast.LENGTH_LONG).show()
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data !=null){
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@CreateBoardActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivBoardImage!!)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
}