package com.yorbax.EMA

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.yorbax.EMA.lecturer.model.MCQsModel
import kotlinx.android.synthetic.main.activity_exam_list.*
import java.lang.Exception

class ResultsActivty : AppCompatActivity() {
    lateinit var myRef : DatabaseReference
    lateinit var progressDialog: ProgressDialog
    lateinit var list : ArrayList<ResultModel>
    lateinit var adapter : MyAdapterResult
    lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_list)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.please_wait))
        myRef = FirebaseDatabase.getInstance().getReference("Result");
        progressDialog.show()
        list = ArrayList()
        adapter = MyAdapterResult(this,list)
        exam_list.layoutManager = LinearLayoutManager(this)
        exam_list.adapter = adapter
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e("result","response")
                try{
                    list.clear()
                    for(i in snapshot.children){
                        val mcqsDetail = i.getValue(ResultModel::class.java)
                        if(mcqsDetail!!.studentId.equals(firebaseUser.uid))
                            list.add(mcqsDetail)
                    }
                    progressDialog.dismiss()
                    adapter.notifyDataSetChanged()

                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Log.e("result","error")
                error.toException().printStackTrace()
            }
        })
    }


}