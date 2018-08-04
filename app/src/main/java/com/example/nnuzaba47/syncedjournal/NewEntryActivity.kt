package com.example.nnuzaba47.syncedjournal

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.facebook.*
import java.util.*
import com.facebook.AccessToken.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_new_entry.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList


class NewEntryActivity : AppCompatActivity() {

    private var entryViewModel:EntryViewModel?=null   //The view model displaying the entry information
    private var postViewModel:PostViewModel?=null   //The view model displaying the post information
    private var callbackManager = CallbackManager.Factory.create()!!  //Handles the facebook login callback
    private var loginManager = LoginManager.getInstance()!!    //Handles facebook login
    private var posts:ArrayList<Post> = ArrayList()    //An empty list to contain the new post
    private var postAdapter:PostAdapterForNewEntryActivity ?= null  //The post adapter that will display the posts
    private var database: MyDatabase ?=null


    //--------------------------------------------Override Methods------------------------------------------------------------
    /**
     * Override the oncreate function to set up the new entry form, along with the new posts view
     * @param savedInstanceState The saved layout information
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_entry)

        //First we set up the views and database
        database = MyDatabase.getDatabase(applicationContext)
        entryViewModel = ViewModelProviders.of(this).get(EntryViewModel::class.java)
        //setting up the recyclerView
        postAdapter = PostAdapterForNewEntryActivity(this)
        postAdapter!!.setPosts(posts)
        rvNewPosts.adapter = postAdapter
        rvNewPosts.layoutManager = LinearLayoutManager(this)

        //Next we setup the callback method for Facebook Login manager
        loginManager.registerCallback(callbackManager,
            object: FacebookCallback<LoginResult> {
                //overriding the necessary abstract functions
                override fun onSuccess(loginResult: LoginResult) {
                    setResult(Activity.RESULT_OK)
                }
                override fun onCancel() {
                    setResult(Activity.RESULT_CANCELED)
                }
                override fun onError(exception: FacebookException) {
                }
            })
    }
    //overriding onActivityResult for the callback of login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    //--------------------------------------------OnClick Listeners------------------------------------------------------------
    /**
     * The button listener for the add button. It creates an entry in the EntryDatabase
     * @param view The current view activity_new_entry
     */
    fun addEntry(view: View) =//Don't allow entry creation if either title or description field is empty
            if (TextUtils.isEmpty(etNewEntryTitle.text) || TextUtils.isEmpty(etNewEntryDescription.text)) {
                Toast.makeText(applicationContext, "Please fill out the fields", Toast.LENGTH_LONG).show()
            }
            else {
                //Create the new entry and insert into database
                val title = etNewEntryTitle.text.toString()
                val description = etNewEntryDescription.text.toString()
                val entry = Entry(title, description, Date())
                var entryId = database!!.entryDao().insert(entry)

                //Set up postViewModel
                postViewModel = ViewModelProviders.of(this).get(PostViewModel::class.java)
                //Loop through all the posts in post adapter's list
                for(ind in 0 until postAdapter!!.itemCount){
                    var post:Post = postAdapter!!.getPostAtPosition(ind)!!
                    //update the description of the post, as the user might have changed it to better describe their day
                    //post!!.description = postAdapterForNewEntryActivity!!.mPostViewHolders[ind].itemView.etPostDescription.text.toString()

                   /**
                    Log.i("Mytag", "current index : $ind with post: $post")
                    Log.i("Mytag", "" +rvPosts.layoutManager)

                    var postViewHolder2 = rvPosts.layoutManager!!.getChildAt(ind)!!
                    var itemId: Long = rvPosts.adapter!!.getItemId(ind)
                    var postViewHolder = rvPosts.findViewHolderForItemId(itemId)
                    var postView = postViewHolder.itemView
                    */

                    var etPostDescription: EditText = postAdapter!!.postToDescriptionMap[post]!!
                    post.description = etPostDescription.text.toString()
                    post.entryId = entryId  //update the post's entryId
                    database!!.postDao().insert(post)
                }

                Toast.makeText(applicationContext, "Added successfully", Toast.LENGTH_LONG).show()

                //go back to the all entries page and close the current one
                startActivity(Intent(applicationContext, EntriesActivity::class.java))
                finish()
            }

    /**
     * The button listener for the sync button. Uploads posts from users social media accounts
     * @param view The current view activity_new_entry
     */
    fun sync(view: View){
        //If the user is not logged in, log them in, while getting the necessary permissions
        if (!isLoggedIn()){
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_posts"));
        }

        if (isLoggedIn()) {
            //create a bundle with the necessary fields of the post that will be required
            var params: Bundle = Bundle()
            params.putString("fields", "description, picture, message, created_time, from")
            //Initiate the graphRequest by using facebook's Graphrequest class (https://developers.facebook.com/docs/reference/android/current/class/GraphRequest/)
            GraphRequest(
                    AccessToken.getCurrentAccessToken(), "/me/feed", params, HttpMethod.GET,
                    GraphRequest.Callback { response ->
                        //On a successful callback, collect the information i.e. the posts into a Json Array and loop through them
                        var postArray: JSONArray? = response.jsonObject.getJSONArray("data")
                        for (item in 0 until postArray!!.length()) {
                            var post: JSONObject? = postArray.getJSONObject(item)
                            //for each post, if there's a picture involved than it was probably an activity in the user day, so we are interested in it
                            if (post!!.has("picture")) {
                                createPost(post)
                            }
                        }
                    }).executeAsync()
        }
        else{
            Toast.makeText(this, "Sorry syncing failed, please try again", Toast.LENGTH_LONG).show()
        }

    }
    /**
    fun deleteNewPost(view: View){
        var linearLayout: View = view.parent as View
        var position:Int = rvPosts.getChildAdapterPosition(linearLayout)
        postAdapter!!.removeAtPosition(position)
    }
    **/
    //--------------------------------------------Helper Methods------------------------------------------------------------
    /**
     * Helper method for creating the post
     * @param post The json object sent by GraphRequest
     */
    private fun createPost(post: JSONObject){
        var postId:String = post.get("id") as String
        var sourceURL = "https://www.facebook.com/$postId"

        //If the user posted a message with the post, then we have to use that, otherwise we just use the caption
        var description: String = if (post.has("message")) post.get("message") as String  else (if (post.has("description")) post.get("description") as String else "")

        //getting the image url, facebook only sends back one
        var imageURL: String = post.get("picture") as String

        //Creating the post
        postAdapter!!.add(Post(sourceURL, description, imageURL))
    }

    /**
     * Helper function that allows us to check if user is logged in
     * @return True if the user is logged in with an unexpired access token
     */
    private fun isLoggedIn():Boolean{
        return getCurrentAccessToken() != null && !getCurrentAccessToken().isExpired
    }

}
