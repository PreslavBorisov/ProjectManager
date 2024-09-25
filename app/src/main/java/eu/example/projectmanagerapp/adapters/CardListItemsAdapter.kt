package eu.example.projectmanagerapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.activities.TaskListActivity
import eu.example.projectmanagerapp.models.Card
import eu.example.projectmanagerapp.models.SelectedMembers

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? =null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(
            R.layout.item_card,
            parent,false
        ))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder){
            val labelColor =holder.itemView.findViewById<View>(R.id.view_label_color)
            if (model.labelColor.isNotEmpty()){

                labelColor.visibility = View.VISIBLE
                labelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                labelColor.visibility = View.GONE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            if((context as TaskListActivity).mAssignedMemberDetailList.size>0){
                val selectedMembersList: ArrayList<SelectedMembers> =ArrayList()

                for (i in context.mAssignedMemberDetailList.indices){
                    for (j in model.assignedTo){
                        if (context.mAssignedMemberDetailList[i].id==j){
                            val selectedMembers =SelectedMembers(
                                context.mAssignedMemberDetailList[i].id,
                                context.mAssignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)

                        }
                    }

                }
                val rvCardSelectedMember = holder.itemView.
                findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                if(selectedMembersList.size>0){



                    if(selectedMembersList.size==1 && selectedMembersList[0].id ==model.createdBy){
                        rvCardSelectedMember.visibility = View.GONE
                    }else{
                        rvCardSelectedMember.visibility = View.VISIBLE
                        rvCardSelectedMember.layoutManager = GridLayoutManager(context,4)

                        val adapter = CardMemberListItemsAdapter(
                            context,selectedMembersList,false)

                        rvCardSelectedMember.adapter = adapter

                        adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener!=null){
                                    onClickListener!!.onClick(holder.adapterPosition)
                                }
                            }
                        })
                    }
                }else{
                    rvCardSelectedMember.visibility = View.GONE
                }
            }



            holder.itemView.setOnClickListener{
                if(onClickListener!=null){
                    onClickListener!!.onClick(position)
                }
            }
        }


    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int)
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}