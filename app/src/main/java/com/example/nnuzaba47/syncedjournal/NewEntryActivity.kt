package com.example.nnuzaba47.syncedjournal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
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
import com.example.nnuzaba47.syncedjournal.Adapter.PostAdapter
import com.example.nnuzaba47.syncedjournal.Database.MyDatabase
import com.example.nnuzaba47.syncedjournal.POJO.Entry
import com.example.nnuzaba47.syncedjournal.POJO.InstagramResponseObject
import com.example.nnuzaba47.syncedjournal.POJO.Post
import com.google.gson.Gson


class NewEntryActivity : AppCompatActivity() {

    private var callbackManager = CallbackManager.Factory.create()!!  //Handles the facebook login callback
    private var loginManager = LoginManager.getInstance()!!    //Handles facebook login
    private var posts: ArrayList<Post> = ArrayList()    //An empty list to contain the new post
    private var postAdapter: PostAdapter? = null  //The post adapter that will display the posts
    private var database: MyDatabase? = null
    private var sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private var entryDate: Date = Date()

    companion object {
        const val REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL = 0
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
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        //First we set up the views and database
        database = MyDatabase.getDatabase(applicationContext)
        //setting up the recyclerView
        postAdapter = PostAdapter(this)
        postAdapter!!.setPosts(posts)
        rvNewPosts.adapter = postAdapter
        rvNewPosts.layoutManager = LinearLayoutManager(this)

        //Next we setup the callback method for Facebook Login manager
        loginManager.registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //If the result is from the instagram server, than retrieve the access token and get the posts
        if (requestCode == REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL) {
            if (resultCode == Activity.RESULT_OK) {
                getInstagramData(data!!.getStringExtra("ACCESS_TOKEN"))
            } else {
                Toast.makeText(this, "Sync from Instagram failed", Toast.LENGTH_LONG).show()
            }
        }
        //If not that means this is the callback from the Facebook Login Manager
        else {
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
            var oldCalendar = Calendar.getInstance()
            oldCalendar.time = entryDate
            //Create Date Picker Dialog, and set the entry Date to it
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                var newCalendar = Calendar.getInstance()
                newCalendar.set(year, monthOfYear, dayOfMonth)
                entryDate = newCalendar.time
                tbNewEntry.title = "New Entry for " + sdf.format(entryDate)
            }, oldCalendar.get(Calendar.YEAR), oldCalendar.get(Calendar.MONTH),
                    oldCalendar.get(Calendar.DAY_OF_MONTH)).show()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    //--------------------------------------------OnClick Listeners------------------------------------------------------------
    //The Listener for the add button
    fun addEntry(view: View) {
        //Don't allow entry creation if either title or description field is empty
        if (TextUtils.isEmpty(etNewEntryTitle.text) || TextUtils.isEmpty(etNewEntryDescription.text)) {
            Toast.makeText(applicationContext, "Please fill out the fields", Toast.LENGTH_LONG).show()
        } else {
            //Create the new entry and insert into database
            val title = etNewEntryTitle.text.toString()
            val description = etNewEntryDescription.text.toString()
            val entry = Entry(title, description, entryDate)
            var entryId = database!!.entryDao().insert(entry)

            //Loop through all the posts in post adapter's list
            for (ind in 0 until postAdapter!!.itemCount) {
                var post: Post = postAdapter!!.getPostAtPosition(ind)!!

                post.entryId = entryId  //update the post's entryId
                database!!.postDao().insert(post)
            }

            Toast.makeText(applicationContext, "Added successfully", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    //The button listener for the sync button. Uploads posts from users social media accounts
    fun sync(view: View) {
        var socialMediaOption = arrayOf("Facebook", "Instagram")
        var selectedItems: ArrayList<String> = ArrayList(2)
        //Create a dialog to let the user choose
        var chooseDialog: AlertDialog = AlertDialog.Builder(view.context)
                .setTitle("Choose Social Media to sync from")
                .setMultiChoiceItems(socialMediaOption, null) { _, index, isChecked ->
                    if (isChecked) {
                        selectedItems.add(socialMediaOption[index])
                    } else {
                        selectedItems.remove(socialMediaOption[index])
                    }
                }
                .setNeutralButton("Sync") { dialog, _ ->
                    if (selectedItems.contains("Facebook")) {
                        syncFB()
                    }
                    if (selectedItems.contains("Instagram")) {
                        syncIG()
                    }
                    selectedItems.clear()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    selectedItems.clear()
                    dialog.dismiss()
                }.create()
        chooseDialog.show()
    }

    //--------------------------------------------Helper Methods------------------------------------------------------------
    private fun syncFB() {
        //If the user is not logged in, log them in, while getting the necessary permissions
        if (!isLoggedIn()) {
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_posts"));
        }
        if (isLoggedIn()) {
            getFacebookData()
        } else {
            Toast.makeText(this, "Sorry syncing failed, please try again", Toast.LENGTH_LONG).show()
        }
    }

    private fun syncIG() {
        var intent = Intent(applicationContext, RetrieveInstagramAccessTokenActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL)
    }

    private fun getFacebookData(){
        //create a bundle with the necessary fields of the post that will be required
        var params: Bundle = Bundle()
        params.putString("fields", "description, full_picture, message, created_time, from")
        var fbCalendar = Calendar.getInstance()
        fbCalendar.time = entryDate
        var since: String = sdf.format(fbCalendar.time).toString() + " 04:00"
        fbCalendar.add(Calendar.HOUR_OF_DAY, 24)
        var until: String = sdf.format(fbCalendar.time).toString() + " 03:59"
        params.putString("since", since)
        params.putString("until", until)

        //Initiate the graphRequest by using facebook's GraphRequest class (https://developers.facebook.com/docs/reference/android/current/class/GraphRequest/)
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


    private fun getInstagramData(accessToken: String) {
        val queue = Volley.newRequestQueue(applicationContext)
        val url = "https://api.instagram.com/v1/users/self/media/recent/?access_token=" +
                accessToken
        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    var instagramResponseObject = Gson().fromJson(response, InstagramResponseObject::class.java)
                    var data = instagramResponseObject.data
                    var calendar = Calendar.getInstance()
                    //Set the start time as the entry Date's 12:00 AM and end time as the next day's 12:00 AM
                    calendar.time = entryDate
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    var startTime = calendar.time.time / 1000
                    calendar.add(Calendar.HOUR_OF_DAY, 24)
                    var endTime = calendar.time.time / 1000
                    //Find the posts within the given time interval and them
                    var i = 0
                    while (i < data!!.size && data!![i].created_time!! > endTime) {
                        i++
                    }
                    while (i < data!!.size && data!![i].created_time!! > startTime) {
                        createInstagramPost(data!![i])
                        i++
                    }
                },
                Response.ErrorListener { Toast.makeText(this, "Failed to sync", Toast.LENGTH_LONG).show() })
        queue.add(stringRequest)
    }


     /**
     * Helper method for creating the post
     * @param post The json object sent by GraphRequest
     */
    private fun createFacebookPost(post: JSONObject) {
        var postId: String = post.get("id") as String
        var sourceURL = "https://www.facebook.com/$postId"

        //If the user posted a message with the post, then we have to use that, otherwise we just use the caption
        var description: String = if (post.has("message")) post.get("message") as String else (if (post.has("description")) post.get("description") as String else "")

        //getting the image url, facebook only sends back one
        var imageURL: String = post.get("full_picture") as String

        //Creating the post
        postAdapter!!.add(Post(sourceURL, description, imageURL))
    }

    /**
     * Helper method for creating the post
     * @param post The json object sent from the GET Request
     */
    private fun createInstagramPost(post: InstagramResponseObject.PostObject) {
        var sourceURL = post.link!!
        var description = post.caption!!.text!!
        var imageURL = post.images!!.standard_resolution!!.url!!
        postAdapter!!.add(Post(sourceURL, description, imageURL))
    }

    /**
     * Helper function that allows us to check if user is logged in
     * @return True if the user is logged in with an unexpired access token
     */
    private fun isLoggedIn(): Boolean {
        return getCurrentAccessToken() != null && !getCurrentAccessToken().isExpired
    }

}

