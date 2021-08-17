package com.example.picpartner

import com.example.picpartner.collection.Collection

data class User(
    var uid: String? = null, var name: String? = null,
    var email: String? = null, var imageUrl: String? = null,
    var identity: String? = null,
    var collection: Collection? = null,
    var follower: User?= null, var following: User?=null
) {
}