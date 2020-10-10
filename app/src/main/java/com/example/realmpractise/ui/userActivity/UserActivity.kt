package com.example.realmpractise.ui.userActivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.realmpractise.BuildConfig
import com.example.realmpractise.R
import com.example.realmpractise.db.User
import com.example.realmpractise.db.UserModule
import com.example.realmpractise.util.FileCompressor
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_user.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class UserActivity : AppCompatActivity() {

    private var userModule: UserModule = UserModule()
    val realm: Realm = Realm.getDefaultInstance()
    private var selectedPhotoUri: Uri? = null
    private lateinit var mPhotoFile: File
    private lateinit var mCompressor: FileCompressor

    companion object {
        const val tag = "UserActivity"
        const val IMAGE_PICK_CODE = 1000
        const val IMAGE_CAPTURE_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        addUserBtn.setOnClickListener {
            saveDATA()
        }
        userPPBtn.setOnClickListener {
            selectImage()
        }
    }

    private fun selectImage() {
        val items = arrayOf<CharSequence>(
            "Take Photo",
            "Choose from Library",
            "Cancel"
        )
        val builder =
            AlertDialog.Builder(this@UserActivity)
        builder.setItems(
            items
        ) { dialog: DialogInterface, item: Int ->
            when {
                items[item] == "Take Photo" -> {
                    requestStoragePermission(true)
                }
                items[item] == "Choose from Library" -> {
                    requestStoragePermission(false)
                }
                items[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun getRealPathFromUri(contentUri: Uri?): String {
        var cursor: Cursor? = null
        return try {
            val proj =
                arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri!!, proj, null, null, null)
            if (BuildConfig.DEBUG && cursor == null) {
                error("Assertion failed")
            }
            val columnIndex: Int = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)!!
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    private fun saveDATA() {
        val user = User()
        val userName = userNameET.text.toString()
        val userPhone = userPhoneET.text.toString()
        user.name = userName
        user.phone = userPhone
        user.pp = selectedPhotoUri.toString()
        userModule.addUser(realm, user)
        Toast.makeText(this, "new user have been added", Toast.LENGTH_SHORT).show()
        clear()
        Log.d(tag, "name is: $userName")
        Log.d(tag, "phone is: $userPhone")
        Log.d(tag, "new user have been added")
    }

    private fun clear() {
        userNameET.setText("")
        userPhoneET.setText("")
    }

    private fun requestStoragePermission(isCamera: Boolean) {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        } else {
                            dispatchGalleryIntent()
                        }
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener {
                Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT)
                    .show()
            }
            .onSameThread()
            .check()
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }

    private fun showSettingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Needs Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, _ ->
            dialog.dismiss()
            openSetting()
        }
        builder.setNegativeButton("Canel") { dialog, _ ->
            dialog.dismiss()

        }
        builder.show()
    }

    private fun openSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 1001)
    }

    private fun dispatchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE) {
                try {
                    mPhotoFile = mCompressor.compressToFile(mPhotoFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Picasso.get()
                .load(mPhotoFile)
                .fit()
                .into(userPP)
        } else if (requestCode == IMAGE_CAPTURE_CODE) {
            val selectedImage = data?.data
            try {
                mPhotoFile = mCompressor.compressToFile(File(getRealPathFromUri(selectedImage)))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            Picasso.get()
                .load(mPhotoFile)
                .fit()
                .into(userPP)
        }
    }


}