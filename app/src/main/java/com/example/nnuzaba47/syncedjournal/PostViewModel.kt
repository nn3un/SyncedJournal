package com.example.nnuzaba47.syncedjournal

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData


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

    fun getAllPosts(): LiveData<List<Post>> {
        return allPosts
    }

    fun getAllPostsForEntryId(entryId: Long): LiveData<List<Post>>{
        return mRepository.getPostsByEntryId(entryId)!!
    }

    fun update(post:Post){
        mRepository.update(post)
    }

    fun getById(id: Int):Post?{
        return mRepository.getPostById(id)
    }

    fun delete(post:Post){
        mRepository.delete(post)
    }

}