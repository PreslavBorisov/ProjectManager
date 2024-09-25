package eu.example.projectmanagerapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import eu.example.projectmanagerapp.R
import eu.example.projectmanagerapp.models.SelectedMembers

open class CardMemberListItemsAdapter(
    private val context: Context,
    private val list:ArrayList<SelectedMembers>,
    private val assignMembers:Boolean
):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener:OnClickListener? =null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
            R.layout.item_selected_member,
            parent,
            false
            ))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){

            val civSelectedMember = holder.itemView
                .findViewById<CircleImageView>(R.id.civ_selected_member_image)

            val civAddMember = holder.itemView
                .findViewById<CircleImageView>(R.id.civ_add_member)

            if(position == list.size-1&& assignMembers){

                civAddMember.visibility = View.VISIBLE
                civSelectedMember.visibility = View.GONE
            }else{

                civAddMember.visibility = View.GONE
                civSelectedMember.visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(civSelectedMember)
            }
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick()
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnClickListener{
        fun onClick()
    }

}