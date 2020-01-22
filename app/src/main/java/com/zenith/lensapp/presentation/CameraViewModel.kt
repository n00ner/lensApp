package com.zenith.lensapp.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel(){
    val isShutterLongPressed by lazy {
        MutableLiveData<Boolean>(false)
    }

    val currentFilter by lazy {
        MutableLiveData(0)
    }

    fun releaseShutter(){
        isShutterLongPressed.postValue(false)
    }

    fun touchShutter(){
        isShutterLongPressed.postValue(true)
    }

    fun nextFilter(){
            currentFilter.postValue(setFilter(currentFilter.value, 1))
    }

    fun previousFilter(){
        currentFilter.postValue(setFilter(currentFilter.value, -1))
    }

    private fun setFilter(value: Int?, step: Int): Int{
        if(value == null){
            return 0
        }else{
            val result = value + step

            if(result < 0){
                return 3
            }else if(result > 3){
                return 0
            }
            return result
        }
    }
}