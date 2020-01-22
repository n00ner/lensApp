package com.zenith.lensapp.presentation

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.zenith.lensapp.R
import com.zenith.lensapp.presentation.glview.PreviewGLRenderer
import com.zenith.lensapp.presentation.glview.TextureRenderer
import kotlinx.android.synthetic.main.activity_main.*

class CameraActivity : AppCompatActivity() {

    private lateinit var viewModel: CameraViewModel
    private lateinit var renderer: PreviewGLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setFullscreenMode()
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)
        initShutterAnimation()
        initFilterChanger()
        initCameraPreview()
    }

    fun initCameraPreview(){
        renderer = PreviewGLRenderer(effectsview)
        effectsview.setEGLContextClientVersion(2)
        effectsview.setRenderer(renderer)
        effectsview.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        renderer.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.puppy))
    }


    private fun setFullscreenMode(){
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun initFilterChanger(){
        container.setOnTouchListener(object: OnSwipeListener(this){
            override fun onSwipeRight() {
                viewModel.previousFilter()
            }

            override fun onSwipeLeft() {
                viewModel.nextFilter()
            }

        })

        viewModel.currentFilter.observe(this, Observer{
            //Toast.makeText(this, "Value is $it", Toast.LENGTH_SHORT).show()
            renderer.setEffect(it)
        })
    }

    private fun initShutterAnimation(){
        viewModel.isShutterLongPressed.observe(this, Observer {
            if(it){
                startScaleAnimation(shutter_btn, 1.5f, 300L)
            }else{
                startScaleAnimation(shutter_btn, 1.0f, 300L)
            }
        })
        shutter_btn.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_UP ){
                viewModel.releaseShutter()
            }
            false
        }
        shutter_btn.setOnLongClickListener {
            viewModel.touchShutter()
            true
        }
    }

    private fun startScaleAnimation(view: View, scaleRatio: Float, duration: Long){
        view.animate().scaleY(scaleRatio)
        view.animate().scaleX(scaleRatio)
        view.animate().duration = duration
        view.animate().start()
    }
}
