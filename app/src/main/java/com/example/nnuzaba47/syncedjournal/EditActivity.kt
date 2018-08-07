package com.example.nnuzaba47.syncedjournal

import android.app.Activity
import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_new_entry.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditActivity : AppCompatActivity() {

    var entryId:Long = -1
    var entry:Entry?= null
    var postsInDB: List<Post> ?= null
    private var mPostViewModel:PostViewModel ?= null
    private var adapter: PostAdapterForEditActivity ?= null
    var postDao: PostDao? = null
    private var loginManager = LoginManager.getInstance()!!    //Handles facebook login
    private var callbackManager = CallbackManager.Factory.create()!!  //Handles the facebook login callback
    private var calendar: Calendar = Calendar.getInstance()
    var sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    //Set up the date picker dialog

    var date: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        etEditEntryDate.setText(sdf.format(calendar.time))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        //Set up
        setSupportActionBar(my_toolbar)
        mPostViewModel = PostViewModel(application)
        postDao = MyDatabase.getDatabase(applicationContext)!!.postDao()

        entryId = intent.getLongExtra("id", -1)
        if(entryId <  0){
            startActivity(Intent(applicationContext, EntriesActivity::class.java))
            Toast.makeText(applicationContext, "Error getting data from database", Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            //var entry:Entry? = ViewModelProviders.of(this).get(EntryViewModel::class.java).getById(entryId)
            entry = MyDatabase.getDatabase(applicationContext)!!.entryDao().loadById(entryId)
            if (entry != null){
                etEditEntryTitle.setText(entry!!.title)
                etEditEntryDescription.setText(entry!!.description)
                etEditEntryDate.setText(sdf.format(entry!!.date)) //Set up the date edit text with the current date
                adapter = PostAdapterForEditActivity(this)

                postsInDB = postDao!!.loadPostsForEntry(entryId)
                adapter!!.setPosts(ArrayList(postsInDB))

                /**var postsLiveData = mPostViewModel!!.getAllPostsForEntryId(entry!!.id!!)
                postsLiveData.observe(this, Observer<List<Post>>{
                    adapter!!.setPosts(ArrayList(it!!))
                })*/

                rvEditPosts.adapter = adapter
                rvEditPosts.layoutManager = LinearLayoutManager(this)

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
            else{
                startActivity(Intent(applicationContext, EntriesActivity::class.java))
                Toast.makeText(applicationContext, "Failed to load entry :(", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        var intent = Intent()
        intent.putExtra("entryId", entryId)
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
        finish()
    }

    //overriding onActivityResult for the callback of login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun sync(view: View){
        //If the user is not logged in, log them in, while getting the necessary permissions
        if (!isLoggedIn()){
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_posts"));
        }

        if (isLoggedIn()) {
            //create a bundle with the necessary fields of the post that will be required
            var params: Bundle = Bundle()
            params.putString("fields", "description, picture, message, created_time, from")
            var since:String = etEditEntryDate.text.toString()+ " 00:00"
            var until:String = etEditEntryDate.text.toString() + " 23:59"
            params.putString("since", since)
            params.putString("until", until)

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

    fun editDate(view: View){
        DatePickerDialog(this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun updateEntry(view: View){
        //update the entry information
        entry!!.title = etEditEntryTitle.text.toString()
        entry!!.description = etEditEntryDescription.text.toString()
        entry!!.date = sdf.parse(etEditEntryDate.text.toString())
        MyDatabase.getDatabase(applicationContext)!!.entryDao().update(entry!!)

        //Update the post's information, namely the description field
        adapter!!.items!!.forEach {
            postDao!!.update(it)
        }

        //Delete the removed entries from database
        postsInDB!!.forEach(){
            if(!adapter!!.items.contains(it)){
                postDao!!.delete(it)
            }
        }

        var intent = Intent()
        intent.putExtra("entryId", entry!!.id)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

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

        var newPost = Post(sourceURL, description, imageURL)
        newPost.entryId = entryId
        var newPostId=  postDao!!.insert(newPost)
        if(newPost.postId == null) {
            newPost.postId =  newPostId
        }
        adapter!!.add(newPost)
    }

    /**
     * Helper function that allows us to check if user is logged in
     * @return True if the user is logged in with an unexpired access token
     */
    private fun isLoggedIn():Boolean{
        return AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired
    }


}
