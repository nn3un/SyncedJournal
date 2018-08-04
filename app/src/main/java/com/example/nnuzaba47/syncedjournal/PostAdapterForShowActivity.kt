package com.example.nnuzaba47.syncedjournal
import android.arch.lifecycle.LiveData
import android.content.Context
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.io.InputStream
import java.net.URL


class PostAdapterForShowActivity(context: Context) : RecyclerView.Adapter<PostAdapterForShowActivity.PostViewHolder>() {

    var items:ArrayList<Post> = ArrayList()
    private val mInflater:LayoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = items!![position]
        holder.sourceURL.text = item.sourceURL
        holder.description.text = item.description
        var asyncTask = holder.SetImageInBackground()
        asyncTask.execute(item.imageURL)
    }



    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostViewHolder {
        val v = mInflater.inflate(R.layout.post_show_ticket, parent, false)
        return PostViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    fun setPosts(posts: ArrayList<Post>) {
        items = posts
        notifyDataSetChanged()
    }

    class PostViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var sourceURL:TextView = itemView.findViewById(R.id.tvShowPostURL) as TextView
        var description:TextView = itemView.findViewById(R.id.tvShowPostDescription) as TextView
        var picture: ImageView = itemView.findViewById(R.id.ivPostPicture) as ImageView

        inner class SetImageInBackground : AsyncTask<String, Void, Void>() {

            private var exception: Exception? = null

            override fun doInBackground(vararg urls: String): Void? {
                try {
                    val url = URL(urls[0])
                    val bitmap = BitmapFactory.decodeStream(url.getContent() as InputStream)
                    picture.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    this.exception = e
                }
                return null
            }
        }

    }
}