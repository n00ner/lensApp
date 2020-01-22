package com.zenith.lensapp.presentation.glview

import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.IntBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class PreviewGLRenderer(private var glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer{

    private val textures = IntArray(2)
    private var effectContext: EffectContext? = null
    private var effect: Effect? = null
    private val texRenderer = TextureRenderer()
    private var imageWidth = 0
    private var imageHeight = 0
    private var initialized = false
    var currentEffect = Constants.NEGATIVE_FILTER
    var saveFrame = false
    private var bitmap: Bitmap? = null

    @Synchronized
    fun setImage(image: Bitmap) {
        this.bitmap?.recycle()
        this.bitmap = image
    }

    fun setEffect(filter: Int){
        currentEffect = filter
        glSurfaceView.requestRender()
    }


    private fun loadTextures() { // Generate textures
        GLES20.glGenTextures(2, textures, 0)
        bitmap?.run {
            // Load input bitmap
            imageWidth = bitmap!!.width
            imageHeight = bitmap!!.height
            texRenderer.updateTextureSize(imageWidth, imageHeight)
            // Upload to texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
        // Set texture parameters
        GLToolbox.initTexParams()
    }

    private fun initEffect() {
        val effectFactory = effectContext!!.factory
            effect?.release()
        when (currentEffect) {
            Constants.NO_FILTER -> {
            }
            Constants.GRAIN_FILTER -> {
                effect = effectFactory.createEffect(EffectFactory.EFFECT_GRAIN)
                effect?.setParameter("strength", 1.0f)
            }

            Constants.NEGATIVE_FILTER -> effect = effectFactory.createEffect(EffectFactory.EFFECT_NEGATIVE)
            Constants.SEPIA_FILTER -> effect = effectFactory.createEffect(EffectFactory.EFFECT_SEPIA)
            else -> {

            }
        }
    }

    private fun applyEffect() {
        effect!!.apply(textures[0], imageWidth, imageHeight, textures[1])
    }

    private fun renderResult() {
        if (currentEffect != Constants.NO_FILTER) { // if no effect is chosen, just render the original bitmap
            texRenderer.renderTexture(textures[1])
        } else {
            saveFrame = true
            // render the result of applyEffect()
            texRenderer.renderTexture(textures[0])
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!initialized) { // Only need to do this once
            effectContext = EffectContext.createWithCurrentGlContext()
            texRenderer.init()
            loadTextures()
            initialized = true
        }
        if (currentEffect != Constants.NO_FILTER) { // if an effect is chosen initialize it and apply it to the texture
            initEffect()
            applyEffect()
        }
        renderResult()
        if (saveFrame) {
            SaveImage(takeScreenshot(gl!!)!!)
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/saved_images")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            Log.i("TAG", "Image SAVED==========" + file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun takeScreenshot(mGL: GL10): Bitmap? {
        val mWidth: Int = glSurfaceView.width
        val mHeight: Int = glSurfaceView.height
        val ib = IntBuffer.allocate(mWidth * mHeight)
        val ibt = IntBuffer.allocate(mWidth * mHeight)
        mGL.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib)
        // Convert upside down mirror-reversed image to right-side up normal
// image.
        for (i in 0 until mHeight) {
            for (j in 0 until mWidth) {
                ibt.put((mHeight - i - 1) * mWidth + j, ib[i * mWidth + j])
            }
        }
        val mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mBitmap.copyPixelsFromBuffer(ibt)
        return mBitmap
    }

    private fun SaveImage(finalBitmap: Bitmap) {
        val root = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).toString()
        val myDir = File("$root/saved_images")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            saveFrame = false
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            saveFrame = false
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        texRenderer.updateViewSize(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

    }

}