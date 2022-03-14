package com.yorbax.EMA

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yorbax.EMA.lecturer.model.MCQsModel
import com.yorbax.EMA.lecturer.model.McqsDetail
import kotlinx.android.synthetic.main.activity_examination.*
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

class ExaminationActivity : AppCompatActivity() ,  SurfaceHolder.Callback {
    lateinit var mcQsModel: MCQsModel
    lateinit var questionslist  : ArrayList<McqsDetail>
    val answerList   = ArrayList<AnswerList>()

    var position = 0
    var userName = ""
    val STORAGE_PERMISSION_REQUEST_CODE = 1
    var PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
//            Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private val imageReader: ImageReader? = null
    private val ORIENTATIONS = SparseIntArray()
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    var stateCallback: CameraDevice.StateCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    //setting up camera
    object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
        }

        override fun onError(cameraDevice: CameraDevice, i: Int) {
            cameraDevice.close()
        }
    }

    lateinit var  counter : CountDownTimer

    lateinit var myRef : DatabaseReference
    lateinit var myRefResult : DatabaseReference
    var correctOptioncounter = 0
    lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_examination)
        myRef = FirebaseDatabase.getInstance().getReference("Exam");
        myRefResult = FirebaseDatabase.getInstance().getReference("Result");
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        try{
            mcQsModel = intent!!.extras!!.get("MCQsModel") as MCQsModel
            questionslist = mcQsModel.mcqsDetails!! as ArrayList<McqsDetail>
            position = 0
            dataAdder()
            Log.e("examopt", "size " + questionslist.size)
        }catch (e: Exception){
            e.printStackTrace()
        }
        next_question.setOnClickListener {
            try{
                var userSlection = ""
                if(rb_option_one.isChecked){
                    userSlection = questionslist[position].option1
                }else if(rb_option_two.isChecked){
                    userSlection = questionslist[position].option2
                }else if(rb_option_three.isChecked){
                    userSlection = questionslist[position].option3
                }else if(rb_option_four.isChecked){
                    userSlection = questionslist[position].option4
                }
//                dbHelper.addDataResults(questionslist[position], userSlection, userName)
                if(userSlection.equals(questionslist[position].correctOption)){
                    correctOptioncounter = correctOptioncounter + 1
                }
                val answer= AnswerList()
                answer.correctAnswer = questionslist[position].correctOption
                answer.id = questionslist[position].questionId
                answer.question = questionslist[position].mcqsQuestion
//                answer.questionId = questionslist[position].
                answerList.add(answer)
                position = position + 1
                dataAdder()
            }catch (e: Exception){
                e.printStackTrace()
            }

            if(position == questionslist.size) {
//                Toast.makeText(this@ExaminationActivity, "Exam Submited", Toast.LENGTH_LONG).show()
//                val sharedPreferences = getSharedPreferences("MYPREF", MODE_PRIVATE)
//                val editor = sharedPreferences.edit()
//                editor.putBoolean(questionslist[0].subname, true).apply()
//                startActivity(Intent(this@ExaminationActivity, StudentActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                //submitt call
                    val resultModel = ResultModel()
                resultModel.answerList = answerList
                resultModel.correctAnsCount = correctOptioncounter.toString()
                resultModel.examId = mcQsModel.ExamId
                resultModel.lecturerId = mcQsModel.lectureId
                resultModel.studentId = firebaseUser.uid

                myRef.child(mcQsModel.ExamId).child("submitedString").setValue(firebaseUser.uid+",")
                myRefResult.push().setValue(resultModel).addOnSuccessListener {
                    Toast.makeText(this@ExaminationActivity,"Exam submitted",Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@ExaminationActivity,HomeActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

                }.addOnFailureListener {
                    Toast.makeText(this@ExaminationActivity,"Exam can not be submitted",Toast.LENGTH_LONG).show()
                }

                return@setOnClickListener

            }

        }
        rb_option_one.setOnClickListener {
            radioChanger(it.id)
        }
        rb_option_two.setOnClickListener {
            radioChanger(it.id)
        }
        rb_option_three.setOnClickListener {
            radioChanger(it.id)
        }
        rb_option_four.setOnClickListener {
            radioChanger(it.id)
        }
        cameraSetUp()

        //taking picture after 10 sec
        counter = object : CountDownTimer(100000, 10000) {
            override fun onTick(millisUntilFinished: Long) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    takePicture()
                }
            }

            override fun onFinish() {
                counter.start()
            }
        }
        counter.start()

    }
    fun dataAdder(){
        try{
            rb_option_one.isChecked = false
            rb_option_two.isChecked = false
            rb_option_three.isChecked = false
            rb_option_four.isChecked = false
            question.setText(questionslist[position].mcqsQuestion)
            option_one.setText(questionslist[position].option1)
            option_two.setText(questionslist[position].option2)
            option_three.setText(questionslist[position].option3)
            option_four.setText(questionslist[position].option4)
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun radioChanger(id: Int){
        rb_option_one.isChecked = false
        rb_option_two.isChecked = false
        rb_option_three.isChecked = false
        rb_option_four.isChecked = false
        if(id == R.id.rb_option_one){
            rb_option_one.isChecked = true
        }else if(id == R.id.rb_option_two){
            rb_option_two.isChecked = true
        }else if(id == R.id.rb_option_three){
            rb_option_three.isChecked = true
        }else if(id == R.id.rb_option_four){
            rb_option_four.isChecked = true
        }
    }

    fun cameraSetUp(){
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)

        //From Java 1.4 , you can use keyword 'assert' to check expression true or false
        assert(textureView != null)
        textureView!!.setSurfaceTextureListener(textureListener)
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                    1,
                    1,
                    Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun takePicture() {
        if (null == cameraDevice) {
            Log.e("No cam", "cameraDevice is null and not being detected!!!")
            return
        }
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            var jpegSizes: Array<Size>? = null
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(
                        ImageFormat.JPEG
                )
            }
            var width = 640
            var height = 480
            if (jpegSizes != null &&
                    0 < jpegSizes.size) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces: MutableList<Surface> = ArrayList(2)
            outputSurfaces.add(reader.surface)
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            val rotation = windowManager.defaultDisplay.rotation
            var rotate = 0
            when (rotate) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 90
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
            Log.e("roration ", " " + rotation)
            Log.e("roration ", " " + rotate)
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS[2])
//            val path: String = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name) + "/"+ System.currentTimeMillis()+".jpg"
//            val file: File = File(path)
//            val file : File = File(Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name) + "/" + System.currentTimeMillis() + ".jpg")
//            val cDir = application.getExternalFilesDir(null)
            val file = File(Environment.getExternalStorageDirectory().toString() + "/" + "tempfolder" + "/" + System.currentTimeMillis().toString() + ".jpg")
            val readerListener: ImageReader.OnImageAvailableListener = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    try {
//                        textureView!!.setDrawingCacheEnabled(true)
//                        textureView!!.buildDrawingCache()
////                        val cachePath: File = saveImage(Bitmap.createBitmap(textureView!!.getDrawingCache()), bytes)
//                        textureView.destroyDrawingCache()

                        image = reader.acquireLatestImage()
                        val buffer: ByteBuffer = image.getPlanes().get(0).getBuffer()
                        val bytes = ByteArray(buffer.capacity())

                        buffer.get(bytes)
                        saveImage(bytes)
//                        save(bytes)

                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        Log.e("exception ", "exception ")
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("exception ", "exception ")
                    } finally {
                        if (image != null) {
                            image.close()
                        }
                    }
                }
                @Throws(IOException::class)
                private fun save(bytes: ByteArray) {
                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        output.write(bytes)
                        Log.e("exception ", "image saved ")
                    } finally {
                        if (null != output) {
                            output.close()
                        }
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
//                    Toast.makeText(this@GestureUnlockActivity, "Saved:$file", Toast.LENGTH_SHORT).show()
                    createCameraPreview()
                }
            }
            cameraDevice!!.createCaptureSession(
                    outputSurfaces,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            try {
                                session.capture(
                                        captureBuilder.build(),
                                        captureListener,
                                        mBackgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Toast.makeText(applicationContext, "Failed on config!", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    },
                    mBackgroundHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveImage(bytes: ByteArray): File {
        val outputfile: File
        val direct = File(
                Environment.getExternalStorageDirectory()
                        .toString() + "/." + getString(R.string.app_name)
        )
        if (!direct.exists()) {
            val wallpaperDirectory = File(
                    Environment.getExternalStorageDirectory()
                            .toString() + "/." + getString(R.string.app_name) + "/"
            )
            wallpaperDirectory.mkdirs()
        }

//        File file = new File(Environment.getExternalStorageDirectory() + "/TempFolder/", "TempImage.png");
//        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
//        String childName="QR_"+ timeStamp +".png";
        val name = System.currentTimeMillis().toString() + ""
        outputfile = File(Environment.getExternalStorageDirectory().toString() + "/." + getString(R.string.app_name) + "/", "$name.png")
        if (outputfile.exists()) {
            outputfile.delete()
        }
        try {
            val out = FileOutputStream(outputfile)
            //            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image003);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.write(bytes)
            out.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val f1 =
                        File("file://" + Environment.getExternalStorageDirectory())
                val contentUri = Uri.fromFile(f1)
                mediaScanIntent.data = contentUri
                sendBroadcast(mediaScanIntent)
            } else {
                sendBroadcast(
                        Intent(
                                Intent.ACTION_MEDIA_MOUNTED,
                                Uri.parse("file://" + Environment.getExternalStorageDirectory())
                        )
                )
            }
            //            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/TempFolder/", "TempImage.png"))));
            sendBroadcast(
                    Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(
                                    File(
                                            Environment.getExternalStorageDirectory()
                                                    .toString() + "/" + getString(R.string.app_name) + "/",
                                            "$name.png"
                                    )
                            )
                    )
            )
            //            Toast.makeText(activity,"File saved",Toast.LENGTH_LONG).show();
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("InNewSave", "Exception agai")
        }
//        Toast.makeText(applicationContext, "Image saved", Toast.LENGTH_LONG).show()
        return outputfile
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                    Arrays.asList(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            if (cameraDevice == null) return
                            cameraCaptureSessions = cameraCaptureSession
                            updatePreview()
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
//                            Toast.makeText(this@ExaminationActivity, "Changed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun updatePreview() {
        if (cameraDevice == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions!!.setRepeatingRequest(
                    captureRequestBuilder!!.build(),
                    null,
                    mBackgroundHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = manager.cameraIdList[1]
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            //Check realtime permission if run higher API 23

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                openCamera()
            }
        }

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {}
        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
    }

    override fun onResume() {
        super.onResume()
        try{
            startBackgroundThread()
            if (textureView!!.isAvailable) if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                openCamera()
            } else textureView!!.surfaceTextureListener = textureListener
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onPause() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stopBackgroundThread()
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        }else{
            finishAffinity()
        }
        super.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.getLooper())
    }

    private fun askPermissions() {
        val permissionCheckStorage = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // we already asked for permisson & Permission granted, call camera intent
        if (permissionCheckStorage == PackageManager.PERMISSION_GRANTED) {

            //do what you want
        } else {

            // if storage request is denied
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage("You need to give permission to access storage in order to work this feature.")
                builder.setNegativeButton(
                        "CANCEL",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                            finish()
                        })
                builder.setPositiveButton(
                        "GIVE PERMISSION",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()

                            // Show permission request popup
                            ActivityCompat.requestPermissions(
                                    this@ExaminationActivity,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    STORAGE_PERMISSION_REQUEST_CODE
                            )
                        })
                builder.show()
            } //asking permission for first time
            else {
                // Show permission request popup for the first time
                ActivityCompat.requestPermissions(
                        this@ExaminationActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
//        TODO("Not yet implemented")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        TODO("Not yet implemented")
    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

}