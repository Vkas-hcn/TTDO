package com.pink.hami.melon.dual.option.ui.list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils.getDualImage

class ListServiceAdapter(private val dataList: MutableList<VpnServiceBean>) :
    RecyclerView.Adapter<ListServiceAdapter.ViewHolder>() {

    private var filteredList = dataList.toMutableList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_country)
        var aivFlag: ImageView = itemView.findViewById(R.id.aiv_flag)
        var llItem: LinearLayout = itemView.findViewById(R.id.ll_item)
        var imgCheck: ImageView = itemView.findViewById(R.id.img_check)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


    private fun onItemClick(position: Int) {
        if (position != -1 ) {
            onItemClickListener?.onItemClick(filteredList[position])
        }
    }


    interface OnItemClickListener {
        fun onItemClick(positionBean: VpnServiceBean)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]
        if (item.hideViewShow) {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )

            if (item.best_dualLoad) {
                holder.tvName.text = "Faster Server"
                holder.aivFlag.setImageResource(R.drawable.ic_fast)
            } else {
                holder.tvName.text = String.format(item.country_name + "," + item.city)
                holder.aivFlag.setImageResource(item.country_name.getDualImage())
            }

            if (item.check_dualLoad && App.vpnLink) {
                holder.llItem.background =
                    holder.itemView.context.getDrawable(R.drawable.bg_item_2_op)
                holder.imgCheck.setImageResource(R.drawable.ic_check)
            } else {
                holder.imgCheck.setImageResource(R.drawable.ic_discheck)
                holder.llItem.background =
                    holder.itemView.context.getDrawable(R.drawable.bg_item_1_op)
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context: Context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemView: View = inflater.inflate(R.layout.item_service, parent, false)
        return ViewHolder(itemView)
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            dataList.toMutableList()
        } else {
            dataList.filter {
                it.country_name.contains(query, ignoreCase = true) || it.city.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}

