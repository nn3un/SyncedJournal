package com.example.nnuzaba47.syncedjournal

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
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
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import android.app.DatePickerDialog
import android.view.Menu
import android.view.MenuItem
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson


class NewEntryActivity : AppCompatActivity() {

    private var entryViewModel:EntryViewModel?=null   //The view model displaying the entry information
    private var postViewModel:PostViewModel?=null   //The view model displaying the post information
    private var callbackManager = CallbackManager.Factory.create()!!  //Handles the facebook login callback
    private var loginManager = LoginManager.getInstance()!!    //Handles facebook login
    private var posts:ArrayList<Post> = ArrayList()    //An empty list to contain the new post
    private var postAdapter:PostAdapterForNewEntryActivity ?= null  //The post adapter that will display the posts
    private var database: MyDatabase ?=null
    private var calendar:Calendar = Calendar.getInstance()
    private var sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private var entryDate:Date = Date()
    //Set up the date picker dialog

    companion object {
        val REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL = 0
    }


    //--------------------------------------------Override Methods------------------------------------------------------------
    /**
     * Override the on create function to set up the new entry form, along with the new posts view
     * @param savedInstanceState The saved layout information
     */
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_entry)
        tbNewEntry.title = "New Entry for " + sdf.format(entryDate)
        setSupportActionBar(tbNewEntry)
        if(supportActionBar!=null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

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
        if(requestCode == REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL){
            if(resultCode== Activity.RESULT_OK){
                val queue = Volley.newRequestQueue(this)
                val url = "https://api.instagram.com/v1/users/self/media/recent/?access_token=" +
                        data!!.getStringExtra("ACCESS_TOKEN")
                val stringRequest = StringRequest(Request.Method.GET, url,
                    Response.Listener<String> { response ->
                        var instagramResponseObject = Gson().fromJson(response, InstagramResponseObject::class.java)
                        var data = instagramResponseObject.data
                        var calendarNew = calendar
                        calendarNew.set(Calendar.HOUR_OF_DAY, 0)
                        calendarNew.set(Calendar.MINUTE, 0)
                        calendarNew.set(Calendar.SECOND, 0)
                        var startTime = calendarNew.time.time/1000
                        calendarNew.add(Calendar.HOUR_OF_DAY, 24)
                        var endTime = calendarNew.time.time/1000
                        var i = 0
                        while(i < data!!.size && data!![i].created_time!! > endTime){
                            i++
                        }
                        while(i < data!!.size && data!![i].created_time!! > startTime){
                            createInstagramPost(data!![i])
                            i++
                        }
                    },
                    Response.ErrorListener {  Toast.makeText(this, "Failed to sync", Toast.LENGTH_LONG).show()})
                queue.add(stringRequest)
            }
            else{
                Toast.makeText(this, "Sync failed", Toast.LENGTH_LONG).show()
            }
        }
        else{
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new_entry, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_changeDate -> {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                entryDate = calendar.time
                tbNewEntry.title = "New Entry for " + sdf.format(entryDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
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
                val entry = Entry(title, description, entryDate)
                var entryId = database!!.entryDao().insert(entry)

                //Set up postViewModel
                postViewModel = ViewModelProviders.of(this).get(PostViewModel::class.java)
                //Loop through all the posts in post adapter's list
                for(ind in 0 until postAdapter!!.itemCount){
                    var post:Post = postAdapter!!.getPostAtPosition(ind)!!
                    //update the description of the post, as the user might have changed it to better describe their day

                    var etPostDescription: EditText = postAdapter!!.postToDescriptionMap[post]!!
                    post.description = etPostDescription.text.toString()
                    post.entryId = entryId  //update the post's entryId
                    database!!.postDao().insert(post)
                }

                Toast.makeText(applicationContext, "Added successfully", Toast.LENGTH_LONG).show()
                finish()
            }

    /**
     * The button listener for the sync button. Uploads posts from users social media accounts
     * @param view The current view activity_new_entry
     */
    fun syncFB(view: View){
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
                            //for each post, if there's a picture involved than it was probably an activity in the user day, so we are interested in it
                            if (post!!.has("full_picture")) {
                                createFacebookPost(post)
                            }
                        }
                    }).executeAsync()
        }
        else{
            Toast.makeText(this, "Sorry syncing failed, please try again", Toast.LENGTH_LONG).show()
        }

    }

    fun syncIG(view:View){
        var intent = Intent(applicationContext, RetrieveInstagramAccessTokenActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL)
    }



    //--------------------------------------------Helper Methods------------------------------------------------------------
    /**
     * Helper method for creating the post
     * @param post The json object sent by GraphRequest
     */
    private fun createFacebookPost(post: JSONObject){
        var postId:String = post.get("id") as String
        var sourceURL = "https://www.facebook.com/$postId"

        //If the user posted a message with the post, then we have to use that, otherwise we just use the caption
        var description: String = if (post.has("message")) post.get("message") as String  else (if (post.has("description")) post.get("description") as String else "")

        //getting the image url, facebook only sends back one
        var imageURL: String = post.get("full_picture") as String

        //Creating the post
        postAdapter!!.add(Post(sourceURL, description, imageURL))
    }

    private fun createInstagramPost(post: InstagramResponseObject.PostObject){
        var sourceURL = post.link!!
        var description = post.caption!!.text!!
        var imageURL = post.images!!.standard_resolution!!.url!!
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
