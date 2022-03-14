package com.yorbax.EMA

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.yorbax.EMA.lecturer.model.MCQsModel
import kotlinx.android.synthetic.main.activity_exam_list.*
import java.lang.Exception

class ExamListActivity : AppCompatActivity() {

    lateinit var myRef : DatabaseReference
    lateinit var progressDialog: ProgressDialog
    lateinit var list : ArrayList<MCQsModel>
    lateinit var adapter : MyAdapterExams
    lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_list)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.please_wait))
        myRef = FirebaseDatabase.getInstance().getReference("Exam");
        progressDialog.show()
        list = ArrayList()
        adapter = MyAdapterExams(this,list)
        exam_list.layoutManager = LinearLayoutManager(this)
        exam_list.adapter = adapter
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    list.clear()
                    for(i in snapshot.children){
                        val mcqsDetail = i.getValue(MCQsModel::class.java)
                       try {
                           val submitedString = i.child("submitedString").getValue(String::class.java)
                           mcqsDetail!!.isSubmited = submitedString!!.contains(firebaseUser.uid)
                       }catch (e : Exception){
                           e.printStackTrace()
                           mcqsDetail!!.isSubmited = false
                       }
                        list.add(mcqsDetail!!)
                    }
                    progressDialog.dismiss()
                    adapter.notifyDataSetChanged()

                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
            }
        })

    }
}