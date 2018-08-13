package com.example.nnuzaba47.syncedjournal.Adapter
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.nnuzaba47.syncedjournal.POJO.Entry
import com.example.nnuzaba47.syncedjournal.R
import java.text.SimpleDateFormat

class EntryAdapter: RecyclerView.Adapter<EntryAdapter.EntryViewHolder>{

    private var items:List<Entry>?=null
    private val mInflater:LayoutInflater

    constructor(context: Context){
        mInflater = LayoutInflater.from(context)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val item = items!!.get(position)
        holder.title.text = item.title.toString()
        holder.date.text = SimpleDateFormat("MMM d, yyyy").format(item.date).toString()
    }


    public fun getEntryAtPosition(position: Int): Entry?{
        return items!!.get(position)
    }
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): EntryViewHolder {
        val v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_ticket, parent, false)
        return EntryViewHolder(v)
    }

    override fun getItemCount(): Int {
        if (items != null) {
            return items!!.size
        }
        return 0
    }

    fun setEntries(words: List<Entry>) {
        items = words
        notifyDataSetChanged()
    }




    class EntryViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var title:TextView = itemView.findViewById(R.id.tvTitle) as TextView
        var date:TextView = itemView.findViewById(R.id.tvDate) as TextView
    }
}

