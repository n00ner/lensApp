package com.zenith.lensapp.presentation.glview

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureRenderer {
    private var program = 0
    private var texSamplerHandle = 0
    private var texCoordHandle = 0
    private var posCoordHandle = 0

    private var texVertices: FloatBuffer? = null
    private var posVertices: FloatBuffer? = null

    private var viewWidth1 = 0
    private var viewHeight1 = 0

    private var texWidth1 = 0
    private var texHeight1 = 0

    private val VERTEX_SHADER = "attribute vec4 a_position;\n" +
            "attribute vec2 a_texcoord;\n" +
            "varying vec2 v_texcoord;\n" +
            "void main() {\n" +
            "  gl_Position = a_position;\n" +
            "  v_texcoord = a_texcoord;\n" +
            "}\n"

    private val FRAGMENT_SHADER = "precision mediump float;\n" +
            "uniform sampler2D tex_sampler;\n" +
            "varying vec2 v_texcoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
            "}\n"

    private val TEX_VERTICES = floatArrayOf(
        0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
    )

    private val POS_VERTICES = floatArrayOf(
        -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f
    )

    private val FLOAT_SIZE_BYTES = 4

    fun init() { // Create program
        program = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        // Bind attributes and uniforms
        texSamplerHandle = GLES20.glGetUniformLocation(
            program,
            "tex_sampler"
        )
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_texcoord")
        posCoordHandle = GLES20.glGetAttribLocation(program, "a_position")
        // Setup coordinate buffers
        texVertices = ByteBuffer.allocateDirect(
            TEX_VERTICES.size * FLOAT_SIZE_BYTES
        )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        texVertices?.put(TEX_VERTICES)?.position(0)
        posVertices = ByteBuffer.allocateDirect(
            POS_VERTICES.size * FLOAT_SIZE_BYTES
        )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        posVertices?.put(POS_VERTICES)?.position(0)
    }

    fun tearDown() {
        GLES20.glDeleteProgram(program)
    }

    fun updateTextureSize(texWidth: Int, texHeight: Int) {
        texWidth1 = texWidth
        texHeight1 = texHeight
        computeOutputVertices()
    }

    fun updateViewSize(viewWidth: Int, viewHeight: Int) {
        viewWidth1 = viewWidth
        viewHeight1 = viewHeight
        computeOutputVertices()
    }

    fun renderTexture(texId: Int) { // Bind default FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        // Use our shader program
        GLES20.glUseProgram(program)
        GLToolbox.checkGlError("glUseProgram")
        // Set viewport
        GLES20.glViewport(0, 0, viewWidth1, viewHeight1)
        GLToolbox.checkGlError("glViewport")
        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND)
        // Set the vertex attributes
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2, GLES20.GL_FLOAT, false,
            0, texVertices
        )
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(
            posCoordHandle, 2, GLES20.GL_FLOAT, false,
            0, posVertices
        )
        GLES20.glEnableVertexAttribArray(posCoordHandle)
        GLToolbox.checkGlError("vertex attribute setup")
        // Set the input texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLToolbox.checkGlError("glActiveTexture")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLToolbox.checkGlError("glBindTexture")
        GLES20.glUniform1i(texSamplerHandle, 0)
        // Draw
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun computeOutputVertices() {
        if (posVertices != null) {
            val imgAspectRatio = texWidth1 / texHeight1.toFloat()
            val viewAspectRatio = viewWidth1 / viewHeight1.toFloat()
            val relativeAspectRatio = viewAspectRatio / imgAspectRatio
            val x0: Float
            val y0: Float
            val x1: Float
            val y1: Float
            if (relativeAspectRatio > 1.0f) {
                x0 = -1.0f / relativeAspectRatio
                y0 = -1.0f
                x1 = 1.0f / relativeAspectRatio
                y1 = 1.0f
            } else {
                x0 = -1.0f
                y0 = -relativeAspectRatio
                x1 = 1.0f
                y1 = relativeAspectRatio
            }
            val coords = floatArrayOf(x0, y0, x1, y0, x0, y1, x1, y1)
            posVertices!!.put(coords).position(0)
        }
    }
}