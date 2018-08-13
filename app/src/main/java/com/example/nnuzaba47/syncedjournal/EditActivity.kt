package com.example.nnuzaba47.syncedjournal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.nnuzaba47.syncedjournal.Adapter.PostAdapterForEditActivity
import com.example.nnuzaba47.syncedjournal.Database.MyDatabase
import com.example.nnuzaba47.syncedjournal.Database.PostDao
import com.example.nnuzaba47.syncedjournal.Database.PostViewModel
import com.example.nnuzaba47.syncedjournal.POJO.Entry
import com.example.nnuzaba47.syncedjournal.POJO.InstagramResponseObject
import com.example.nnuzaba47.syncedjournal.POJO.Post
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_edit.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditActivity : AppCompatActivity() {

    var entryId:Long = -1
    var entry: Entry?= null
    var entryDate:Date ?= null

    var postDao: PostDao? = null
    private var postsInDB: List<Post> ?= null
    private var adapter: PostAdapterForEditActivity?= null

    private var loginManager = LoginManager.getInstance()!!    //Handles facebook login
    private var callbackManager = CallbackManager.Factory.create()!!  //Handles the facebook login callback

    private var calendar: Calendar = Calendar.getInstance()
    var sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)

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
                //set up title bar
                tbEditEntry.title = "Edit entry for " + sdf.format(entryDate)
                setSupportActionBar(tbEditEntry)
                //set up entry fields
                etEditEntryTitle.setText(entry!!.title)
                etEditEntryDescription.setText(entry!!.description)

                //set up adapter
                adapter = PostAdapterForEditActivity(this)
                postsInDB = postDao!!.loadPostsForEntry(entryId)
                adapter!!.setPosts(ArrayList(postsInDB))
                rvEditPosts.adapter = adapter
                rvEditPosts.layoutManager = LinearLayoutManager(this)

                //register facebook's callback manager
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

    //Set up on back pressed so that the show page has access to the entryId
    override fun onBackPressed() {
        var intent = Intent()
        intent.putExtra("entryId", entryId)
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
        finish()
    }

    //overriding onActivityResult for the callback of login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == NewEntryActivity.REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL){
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
            DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            true
        }

        R.id.homeAsUp->{
            onBackPressed()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

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
                .setPositiveButton("Sync") { dialog, _ ->
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

    fun syncFB(){
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
                                createFacebookPost(post)
                            }
                        }
                    }).executeAsync()
            }
        else{
            Toast.makeText(this, "Sorry syncing failed, please try again", Toast.LENGTH_LONG).show()
        }

    }

    private fun syncIG(){
        var intent = Intent(applicationContext, RetrieveInstagramAccessTokenActivity::class.java)
        startActivityForResult(intent, NewEntryActivity.REQUEST_CODE_FOR_ACCESS_TOKEN_RETRIEVAL)
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
        postsInDB!!.forEach{
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
    private fun createFacebookPost(post: JSONObject){
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

    //Create new instagram post andd add it to database
    private fun createInstagramPost(post: InstagramResponseObject.PostObject){
        var sourceURL = post.link!!
        var description = post.caption!!.text!!
        var imageURL = post.images!!.standard_resolution!!.url!!
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
