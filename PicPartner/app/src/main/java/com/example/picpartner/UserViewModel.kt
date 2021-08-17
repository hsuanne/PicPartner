package com.example.picpartner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.picpartner.collection.Collection

class UserViewModel : ViewModel() {
    var currentUser = MutableLiveData<User>()
    var currentCollection = MutableLiveData<Collection>()
    var currentRegular = MutableLiveData<User>()
}
