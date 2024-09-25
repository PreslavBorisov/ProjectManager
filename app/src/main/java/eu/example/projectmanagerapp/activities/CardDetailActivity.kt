package eu.example.projectmanagerapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.adapters.CardMemberListItemsAdapter
import eu.example.projectmanagerapp.databinding.ActivityCardDetailBinding
import eu.example.projectmanagerapp.dialogs.LabelColorListDialog
import eu.example.projectmanagerapp.dialogs.SelectMemberListDialog
import eu.example.projectmanagerapp.firebase.FireStoreClass
import eu.example.projectmanagerapp.models.Board
import eu.example.projectmanagerapp.models.Card
import eu.example.projectmanagerapp.models.SelectedMembers
import eu.example.projectmanagerapp.models.Task
import eu.example.projectmanagerapp.models.User
import eu.example.projectmanagerapp.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class CardDetailActivity : BaseActivity() {

    private var binding: ActivityCardDetailBinding? =null
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor =""
    private lateinit var mMembersDetailsList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        setUpActionBar()

        binding?.etNameCardDetails?.setText(mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].name)

        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails?.text.toString().length)

        mSelectedColor= mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }


        binding?.btnUpdateCardDetails?.setOnClickListener {
            if(binding?.etNameCardDetails?.text.toString().isNotEmpty())
                updateCardDetails()
            else
                Toast.makeText(this@CardDetailActivity,"Enter a card name.", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.tv_select_label_color).setOnClickListener {
            labelColorListDialog()
        }

        findViewById<TextView>(R.id.tv_select_members).setOnClickListener {
            memberListDialog()
        }

        mSelectedDueDateMilliSeconds =
            mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].dueDate

        val selectedDateDueDate = findViewById<TextView>(R.id.tv_select_due_date)

        if(mSelectedDueDateMilliSeconds>0){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date( mSelectedDueDateMilliSeconds))
            selectedDateDueDate.text = selectedDate
        }

        selectedDateDueDate.setOnClickListener {
            showDatePicker()
        }

        setUpSelectedMembersList()
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding?.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = mBoardDetails.
            taskList[mTaskListPosition].
            cards[mCardPosition].name
        }

        binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun colorList(): ArrayList<String>{
        val colorList: ArrayList<String> = ArrayList()
        colorList.add("#43C86F")
        colorList.add("#0C90F1")
        colorList.add("#F72400")
        colorList.add("#7A8089")
        colorList.add("#D57C1D")
        colorList.add("#770000")
        colorList.add("#0022F8")
        return colorList
    }

    private fun setColor(){
        val tvSelectedColor = findViewById<TextView>(R.id.tv_select_label_color)

       tvSelectedColor.text = ""
        tvSelectedColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.action_delete_card ->{
                alertDialogForDeleteCard(
                    mBoardDetails
                    .taskList[mTaskListPosition]
                    .cards[mCardPosition]
                    .name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailsList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun memberListDialog(){
        val cardAssignedMemberList = mBoardDetails
            .taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if(cardAssignedMemberList.size>0){
            for( i in mMembersDetailsList.indices){
                for(j in cardAssignedMemberList){
                    if(mMembersDetailsList[i].id == j){
                        mMembersDetailsList[i].selected =true
                    }
                }
            }
        }else{
            for( i in mMembersDetailsList.indices){

                mMembersDetailsList[i].selected = false

            }
        }

        val listDialog = object: SelectMemberListDialog(
            this,
            mMembersDetailsList,
            resources.getString(R.string.str_select_member)
        ){
            override fun onMemberSelected(member: User, action: String) {
                if(action==Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition]
                        .cards[mCardPosition].assignedTo.contains(member.id)){

                        mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardPosition].assignedTo.add(member.id)
                    }
                } else {
                    mBoardDetails.taskList[mTaskListPosition]
                        .cards[mCardPosition].assignedTo.remove(member.id)

                    for(i in mMembersDetailsList.indices){
                        if(mMembersDetailsList[i].id == member.id){
                            mMembersDetailsList[i].selected = false
                        }
                    }
                }
                setUpSelectedMembersList()
            }
        }
        listDialog.show()

    }

    private fun updateCardDetails(){
        val card = Card(
            binding?.etNameCardDetails?.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailActivity,mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList

        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailActivity,mBoardDetails)

    }

    private fun alertDialogForDeleteCard(cardName: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon((android.R.drawable.ic_dialog_alert))

        builder.setPositiveButton(resources.getString(R.string.yes)){dialogInterface,_ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)){dialogInterface,_ ->
            dialogInterface.dismiss()
        }

        val dialogAlert : AlertDialog = builder.create()

        dialogAlert.setCancelable(false)
        dialogAlert.show()

    }

    private fun labelColorListDialog(){
        val colorList: ArrayList<String> = colorList()

        val listDialog = object : LabelColorListDialog(
            this,
            colorList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor){
            override fun onItemSelected(color: String) {
               mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setUpSelectedMembersList(){
        val cardAssignedMemberList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for( i in mMembersDetailsList.indices){
            for(j in cardAssignedMemberList){
                if(mMembersDetailsList[i].id == j){
                    val selectedMember = SelectedMembers(
                        mMembersDetailsList[i].id,
                        mMembersDetailsList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }
        val tvSelectedMembers = findViewById<TextView>(R.id.tv_select_members)
        val rvSelectedMembers: RecyclerView = findViewById(R.id.rv_selected_members_list)

        if(selectedMembersList.size>0){
            selectedMembersList.add(SelectedMembers("",""))

            tvSelectedMembers.visibility = View.GONE
            rvSelectedMembers.visibility = View.VISIBLE

            rvSelectedMembers.layoutManager = GridLayoutManager(
                this@CardDetailActivity,
                6
            )
            val adapter = CardMemberListItemsAdapter(this,selectedMembersList,true)

            rvSelectedMembers.adapter = adapter
            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        memberListDialog()
                    }

                }
            )
        }else{
            tvSelectedMembers.visibility = View.VISIBLE
            rvSelectedMembers.visibility = View.GONE
        }

    }

    private fun showDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            { _, _, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if(dayOfMonth<10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear = if((monthOfYear+1)<10)"0${monthOfYear+1}" else "${monthOfYear+1}"
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                val tvSelectedDueDate = findViewById<TextView>(R.id.tv_select_due_date)
                tvSelectedDueDate.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },year,month,day
        )

        dpd.show()
    }
}