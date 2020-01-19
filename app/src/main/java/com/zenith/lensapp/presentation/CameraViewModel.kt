package com.zenith.lensapp.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel(){
    val isShutterLongPressed by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun releaseShutter(){
        isShutterLongPressed.postValue(false)
    }

    fun touchShutter(){
        isShutterLongPressed.postValue(true)
    }
}