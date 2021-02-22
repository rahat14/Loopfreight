package com.spinnertech.loopfreight.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.spinnertech.loopfreight.R
import com.spinnertech.loopfreight.model.resultList
import com.spinnertech.loopfreight.utils.Utils
import java.text.SimpleDateFormat


class mainRecycerAdapter(
    var items: MutableList<resultList>,
    private val interaction: Interactions? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MainRecyclerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_repo,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MainRecyclerViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


    interface Interactions {
        fun onItemSelected(position: Int, item: resultList)
    }


    class MainRecyclerViewHolder
    constructor(
        itemView: View,
        private val interaction: Interactions?
    ) : RecyclerView.ViewHolder(itemView) {

        val title = itemView.findViewById<TextView>(R.id.name)
        val autherName = itemView.findViewById<TextView>(R.id.auther)
        val shortDesc = itemView.findViewById<TextView>(R.id.short_desc)
        val lang = itemView.findViewById<TextView>(R.id.lang)
        val date = itemView.findViewById<TextView>(R.id.dateTv)
        val contibutorName = itemView.findViewById<TextView>(R.id.contibutorName)
        val addCount = itemView.findViewById<TextView>(R.id.addCount)
        val deleteCount = itemView.findViewById<TextView>(R.id.deleteCount)
        val commitsCount = itemView.findViewById<TextView>(R.id.commitsCount)


        fun bind(item: resultList) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            title.text = item.data.name
            autherName.text = item.data.owner.login
            shortDesc.text = item.data.description
            lang.text = item.data.language
           //("yyyy-MM-dd'T'HH:mm:ss'Z'")

            date.text = "Upated On ${Utils.covertTime(item.data.updated_at)}"
            contibutorName.text = item.result.highestContributor?.author?.login
            addCount.text = item.result.highestContributorScore?.a.toString()
            commitsCount.text = item.result.highestContributorScore?.c.toString()
            deleteCount.text = item.result.highestContributorScore?.d.toString()


        }
    }

}




