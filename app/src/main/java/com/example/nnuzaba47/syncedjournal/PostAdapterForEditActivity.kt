package com.example.nnuzaba47.syncedjournal
import android.content.Context
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat

class PostAdapterForEditActivity: RecyclerView.Adapter<PostAdapterForEditActivity.PostViewHolder> {

    var items: ArrayList<Post>? = ArrayList()
    private val mInflater: LayoutInflater
    var postToDescriptionMap = HashMap<Post, EditText>()

    constructor(context: Context) {
        mInflater = LayoutInflater.from(context)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = items!![position]
        holder.sourceURL.text = item.sourceURL
        holder.description.setText(item.description)
        var asyncTask = holder.SetImageInBackground()
        asyncTask.execute(item.imageURL)
        postToDescriptionMap[item] = holder.description
        holder.deleteNewPost.setOnClickListener{
            var index = items!!.indexOf(item)
            MyDatabase.getDatabase(holder.itemView.context)!!.postDao().delete(items!!.removeAt(index))
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, items!!.size)
            postToDescriptionMap.remove(items!!.removeAt(position))
        }
    }


    public fun getPostAtPosition(position: Int): Post? {
        return items!![position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_ticket, parent, false)
        return PostViewHolder(v)
    }

    override fun getItemCount(): Int {
        if (items != null) {
            return items!!.size
        }
        return 0
    }

    fun setPosts(posts: ArrayList<Post>) {
        items = posts
        notifyDataSetChanged()
    }


    public class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sourceURL: TextView = itemView.findViewById(R.id.tvShowPostURL) as TextView
        var description: EditText = itemView.findViewById(R.id.etPostDescription) as EditText
        var picture: ImageView = itemView.findViewById(R.id.ivPostPicture)
        var deleteNewPost: FloatingActionButton = itemView.findViewById(R.id.fabDeleteNewPost)

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

