package eu.example.projectmanagerapp.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(
    context: Context,
    private var list:ArrayList<String>,
    private val title: String = "",
    private var mSelectedColor: String = ""
):Dialog(context) {

    private var adapter: LabelColorListItemsAdapter?=null
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_list,null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View){

        val rvList = view.findViewById<RecyclerView>(R.id.rvList)

        view.findViewById<TextView>(R.id.tvTitleDialog).text = title
        rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context,list,mSelectedColor)
        rvList.adapter = adapter

        adapter!!.onItemClickListener =
            object : LabelColorListItemsAdapter.OnItemClickListener{
                override fun onClick(position: Int, color: String) {
                    dismiss()
                    onItemSelected(color)
                }
            }
    }

    protected abstract fun onItemSelected(color: String)

}