package com.example.nnuzaba47.syncedjournal.Database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.example.nnuzaba47.syncedjournal.Database.PostRepository
import com.example.nnuzaba47.syncedjournal.POJO.Post


class PostViewModel : AndroidViewModel {

    private var mRepository: PostRepository
    private var allPosts: LiveData<List<Post>>

    constructor(application: Application) : super(application) {
        mRepository = PostRepository(application)
        allPosts = mRepository.getAllPosts()
    }

    fun insert(post: Post) {
        mRepository.insert(post)
    }

    fun update(post: Post){
        mRepository.update(post)
    }


}