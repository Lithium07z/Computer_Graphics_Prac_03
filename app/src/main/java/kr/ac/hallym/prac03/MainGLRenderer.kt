package kr.ac.hallym.prac03

import android.content.Context
import android.opengl.GLES30
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import com.google.android.material.resources.MaterialAttributes
import java.io.BufferedInputStream
import kotlin.math.sin
import kotlin.math.cos
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

const val COORDS_PER_VERTEX = 3

var eyePos = floatArrayOf(0.0f, 3.0f, 3.0f)
var eyeAt = floatArrayOf(0.0f, 0.0f, 0.0f)
var cameraVec = floatArrayOf(0.0f, -0.7071f, -0.7071f)

val lightDir = floatArrayOf(0.0f, 1.0f, 1.0f)
val lightAmbient = floatArrayOf(0.1f, 0.1f, 0.1f)
val lightDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f)
val lightSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)

var prevPosX = 3.0f
var outwardDir = true
var objectPos = arrayOf(
    floatArrayOf(3.0f, 0.0f, -7.0f), floatArrayOf(-3.0f, 0.0f, -7.0f),
    floatArrayOf(3.0f, 0.0f, -4.0f), floatArrayOf(-3.0f, 0.0f, -4.0f),
    floatArrayOf(3.0f, 0.0f, -1.0f), floatArrayOf(-3.0f, 0.0f, -1.0f)
)

class MainGLRenderer (val context: Context): GLSurfaceView.Renderer{

    //private lateinit var mTexGround: MyTexGround
    private lateinit var mGround: MyLitTexGround
    private lateinit var mHexa: MyLitHexa
    private lateinit var mCube0: MyLitCube
    private lateinit var mCube: MyLitTexCube
    private lateinit var mArcball: MyArcball

    private var modelMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)
    private var projectionMatrix = FloatArray(16)
    private var vpMatrix = FloatArray(16)
    private var mvpMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    private var startTime = SystemClock.uptimeMillis()
    private var rotYAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
        
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.setIdentityM(vpMatrix, 0)

        mGround = MyLitTexGround(context)
        mHexa = MyLitHexa(context)
        mCube0 = MyLitCube(context)
        mCube = MyLitTexCube(context)
        mArcball = MyArcball()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        mArcball.resize(width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 90f, ratio, 0.001f, 1000f)

        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], eyeAt[0], eyeAt[1], eyeAt[2], 0f, 1f, 0f)

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        eyeAt[0] = eyePos[0] + cameraVec[0]
        eyeAt[1] = eyePos[1] + cameraVec[1]
        eyeAt[2] = eyePos[2] + cameraVec[2]
        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], eyeAt[0], eyeAt[1], eyeAt[2], 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, eyePos[0], 0f, eyePos[2])
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        mCube0.draw(mvpMatrix, modelMatrix)

        //Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mArcball.rotationMatrix, 0)

        //mGround.draw(mvpMatrix, modelMatrix)
        mGround.draw(mvpMatrix, mArcball.rotationMatrix)

        val endTime = SystemClock.uptimeMillis()
        val angle = 0.1f * (endTime - startTime).toFloat()
        startTime = endTime
        rotYAngle += angle
        var rotYMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotYMatrix, 0, rotYAngle, 0f, 1f, 0f)

        var rotMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotMatrix, 0, 45f, 0f, 0f, 1f)
        Matrix.multiplyMM(rotMatrix, 0, rotYMatrix, 0, rotMatrix, 0)

        lightDir[0] = sin(rotYAngle * 0.01f)
        lightDir[2] = cos(rotYAngle * 0.01f)

        val posX: Float
        if (outwardDir) {
            posX = prevPosX + angle * 0.01f
        } else {
            posX = prevPosX - angle * 0.01f
        }
        if (posX > 9) {
            outwardDir = false
        } else if (posX < 1) {
            outwardDir = true
        }
        prevPosX = posX

        var objectId = 0
        for (z in -7 .. 0 step 3) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, posX, 0f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix, modelMatrix)

            objectPos[objectId++][0] = posX

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, posX, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix, modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -posX, 0f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix, modelMatrix)

            objectPos[objectId++][0] = -posX

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -posX, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix, modelMatrix)
        }

        if (detectCollision(eyePos[0], eyePos[2])) {
            eyePos[0] = 0.0f
            eyePos[1] = 3.0f
            eyePos[2] = 3.0f
            cameraVec[0] = 0.0f
            cameraVec[1] = -0.7071f
            cameraVec[2] = -0.7071f
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> mArcball.start(x, y)
            MotionEvent.ACTION_MOVE -> mArcball.end(x, y)
        }
        return true
    }
}

fun loadShader(type: Int, filename: String, myContext: Context): Int {

    // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
    return GLES30.glCreateShader(type).also { shader ->

        val inputStream = myContext.assets.open(filename)
        val inputBuffer = ByteArray(inputStream.available())
        inputStream.read(inputBuffer)
        val shaderCode = String(inputBuffer)

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        // log the compile error
        val compiled = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled)
        if (compiled.get(0) == 0) {
            GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, compiled)
            if (compiled.get(0) > 1) {
                Log.e("Shader", "$type shader: " + GLES30.glGetShaderInfoLog(shader))
            }
            GLES30.glDeleteShader(shader)
            Log.e("Shader", "$type shader compile error.")
        }
    }
}

fun loadBitmap(filename: String, myContext: Context): Bitmap {
    val manager = myContext.assets
    val inputStream = BufferedInputStream(manager.open(filename))
    val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
    return bitmap!!
}

fun cameraRotate(theta: Float) {
    val sinTheta = sin(theta)
    val cosTheta = cos(theta)
    val newVecZ = cosTheta * cameraVec[2] - sinTheta * cameraVec[0]
    val newVecX = sinTheta * cameraVec[2] + cosTheta * cameraVec[0]
    cameraVec[0] = newVecX
    cameraVec[2] = newVecZ
}

fun cameraMove(distance: Float) {
    val newPosX = eyePos[0] + distance * cameraVec[0]
    val newPosZ = eyePos[2] + distance * cameraVec[2]
    //if (newPosX > -10 && newPosX < 10 && newPosZ > -10 && newPosZ < 10) {
    if (!detectCollision(newPosX, newPosZ)) {
        eyePos[0] = newPosX
        eyePos[2] = newPosZ
    }
}

fun detectCollision(newPosX: Float, newPosZ: Float): Boolean {
    if (newPosX < -10 || newPosX > 10 || newPosZ < -10 || newPosZ > 10) {
        return true
    }
    for (i in 0 .. objectPos.size - 1) {
        if (abs(newPosX - objectPos[i][0]) < 1.0 && abs(newPosZ - objectPos[i][2]) < 1.0) {
            println("***** detection of collision at($newPosX, 0, $newPosZ) *****");
            return true;
        }
    }
    return false
}