package com.example.nnuzaba47.syncedjournal.Adapter
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.nnuzaba47.syncedjournal.POJO.Post
import com.example.nnuzaba47.syncedjournal.R
import java.io.InputStream
import java.net.URL

class PostAdapter: RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    var items: ArrayList<Post> = ArrayList()
    private val mInflater: LayoutInflater
    //var postIdToDescriptionMap = HashMap<Long, EditText>()

    constructor(context: Context) {
        mInflater = LayoutInflater.from(context)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = items[position]

        //Set up the source link
        val content = SpannableString("Source URL")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        holder.sourceURL.text = content
        holder.sourceURL.setOnClickListener {
            ContextCompat.startActivity(it.context, Intent(Intent.ACTION_VIEW, Uri.parse(item.sourceURL)), null)
        }

        //If the description is changed by the user, update the original post
        holder.description.setText(item.description)
        holder.description.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                item.description = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
            }

        })

        //Set image
        var asyncTask = holder.SetImageInBackground()
        asyncTask.execute(item.imageURL)

        //Delete post listener
        holder.deleteNewPost.setOnClickListener{
            var index = items.indexOf(item)
            items!!.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, items.size)
            //postIdToDescriptionMap.remove(item.postId!!)
        }
    }


    fun getPostAtPosition(position: Int): Post? {
        return items!![position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.post_ticket, parent, false)
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

    fun add(post: Post){
        var position = items.size
        items.add(post)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, items.size)
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

