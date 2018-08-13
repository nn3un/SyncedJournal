package com.example.nnuzaba47.syncedjournal.POJO

//Class that mimics Instagram User Recent Media Response
class InstagramResponseObject {
    var data:Array<PostObject>?=null

    class PostObject{
        var images: ImageObjects?= null
        var created_time:Long ?=null
        var caption: CaptionObject?= null
        var link:String?=null

        class ImageObjects{
            var standard_resolution: ImageObject?= null
            class ImageObject{
                var url:String?=null
            }
        }
        class CaptionObject{
            var text:String?=null
        }
    }
}