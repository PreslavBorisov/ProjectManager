package eu.example.projectmanagerapp.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.adapters.MemberListItemAdapter
import eu.example.projectmanagerapp.models.User

abstract class SelectMemberListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
): Dialog(context) {

    private var adapter: MemberListItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list,null)

        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setContentView(view)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View){

        val rvList = view.findViewById<RecyclerView>(R.id.rvList)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitleDialog)

        tvTitle.text = title
        if(list.size>0){

            rvList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemAdapter(context,list)
            rvList.adapter = adapter

            adapter!!.setOnClickListener(object : MemberListItemAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onMemberSelected(user,action)
                }

            })
        }
    }
    protected abstract fun onMemberSelected(member: User,action: String)

}