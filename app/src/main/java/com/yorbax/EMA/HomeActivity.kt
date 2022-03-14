package com.yorbax.EMA

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yorbax.EMA.R
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.yorbax.EMA.ExamListActivity
import com.google.firebase.database.DatabaseReference
import com.yorbax.EMA.Auth.SignInActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.Exception
import javax.security.auth.login.LoginException

class HomeActivity : AppCompatActivity() {
    var PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        btnCourse.setOnClickListener {
            if (!hasPermissions(this@HomeActivity, *PERMISSIONS)) {
                ActivityCompat.requestPermissions(this@HomeActivity, PERMISSIONS, 150);
            }else {
                startActivity(Intent(this@HomeActivity, ExamListActivity::class.java))
            }

        }

        btnLect.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ResultsActivty::class.java))
        }
        btnLogout.setOnClickListener {
            try{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@HomeActivity, SignInActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
            }catch ( e : Exception){
                e.printStackTrace()
            }
        }

    }

    fun tempFuntion() {
        var myRef: DatabaseReference
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 150) {
            startActivity(Intent(this@HomeActivity, ExamListActivity::class.java))
        }
    }
}