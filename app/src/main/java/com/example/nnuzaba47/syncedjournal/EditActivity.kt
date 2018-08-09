package com.example.nnuzaba47.syncedjournal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_edit.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditActivity : AppCompatActivity() {

    var entryId:Long = -1
    var entry:Entry?= null
    var postDao: PostDao? = null
    var sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    var entryDate:Date ?= null
    private var postsInDB: List<Post> ?= null
    private var mPostViewModel:PostViewModel ?= null
    private var adapter: PostAdapterForEditActivity ?= null
    private var loginManager = LoginManager.getInstance()!!    //Handles facebook login
    private var callbackManager = CallbackManager.Factory.create()!!  //Handles the facebook login callback
    private var calendar: Calendar = Calendar.getInstance()

    //Set up the date picker dialog

    var dateSetListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        entryDate = calendar.time
        tbEditEntry.title = "Edit entry for " + sdf.format(entryDate)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        //Set up
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
                entryDate = entry!!.date
                tbEditEntry.title = "Edit entry for " + sdf.format(entryDate)
                setSupportActionBar(tbEditEntry)
                etEditEntryTitle.setText(entry!!.title)
                etEditEntryDescription.setText(entry!!.description)


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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new_entry, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_changeDate -> {
            DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            true
        }

        R.id.homeAsUp->{
            onBackPressed()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun sync(view: View){
        //If the user is not logged in, log them in, while getting the necessary permissions
        if (!isLoggedIn()){
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_posts"));
        }

        if (isLoggedIn()) {
            //create a bundle with the necessary fields of the post that will be required
            var params: Bundle = Bundle()
            params.putString("fields", "description, full_picture, message, created_time, from")
            calendar.time = entryDate
            var since:String = sdf.format(calendar.time).toString() + " 04:00"
            calendar.add(Calendar.HOUR_OF_DAY, 24)
            var until:String = sdf.format(calendar.time).toString() + " 03:59"
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
                            //for each post, if there's a full_picture involved than it was probably an activity in the user day, so we are interested in it
                            if (post!!.has("full_picture")) {
                                createPost(post)
                            }
                        }
                    }).executeAsync()
            }
        else{
            Toast.makeText(this, "Sorry syncing failed, please try again", Toast.LENGTH_LONG).show()
        }

    }

    fun updateEntry(view: View){
        //update the entry information
        entry!!.title = etEditEntryTitle.text.toString()
        entry!!.description = etEditEntryDescription.text.toString()
        entry!!.date = entryDate
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
        var imageURL: String = post.get("full_picture") as String

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
