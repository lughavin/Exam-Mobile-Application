package com.yorbax.EMA

import android.content.Context
import android.content.Intent
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.yorbax.EMA.Model.UserModel
import com.yorbax.EMA.lecturer.model.MCQsModel
import java.lang.Exception

import java.util.*

class MyAdapterResult(var context: Context, var data: ArrayList<ResultModel>) : RecyclerView.Adapter<MyAdapterResult.MyViewHolder>() {
    var TAG = "***Adapter"
    val sharedPreferences = context.getSharedPreferences("MYPREF", AppCompatActivity.MODE_PRIVATE)
    val myRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("users");

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name : TextView
        var questions : TextView
        var submitted : TextView
        var see_results : Button


        init {
            //                sideImage=view.findViewById(R.id.side_image);

            name = view.findViewById(R.id.name)
            questions = view.findViewById(R.id.questions)
            submitted = view.findViewById(R.id.submitted)
            see_results = view.findViewById(R.id.see_results)


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.exam_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        myRef.child(data[position].lecturerId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    val userModel = snapshot.getValue(UserModel::class.java)
                    holder.name.setText("Lecture Name : "+userModel!!.username)
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
//        if(data[position].isSubmited){
//            holder.submitted.visibility = View.VISIBLE
//        }else{
//
//        }


        if(data[position].ispublished)
            holder.submitted.text = "Correct : "+data[position].correctAnsCount + " out of : "+data[position].answerList.size
        else
            holder.submitted.text = "Result not published yet"
        holder.questions.setText("Total Question : "+data[position].answerList.size)

        holder.see_results.visibility = View.GONE

//        val issubmited = sharedPreferences.getBoolean(data[position].name,false)
//        val ispublished = sharedPreferences.getBoolean("result_publish",false)
//        if(issubmited){
//            holder.submitted.visibility = View.VISIBLE
//        }else{
//            holder.submitted.visibility = View.GONE
//        }
//        if(ispublished){
//            holder.see_results.visibility = View.VISIBLE
//        }else{
//            holder.see_results.visibility = View.GONE
//        }
//        holder.see_results.setOnClickListener {
//            context.startActivity(Intent(context,SeeResultsActivity::class.java).putExtra("questions",data[position].questions).putExtra("user_name",userName))
//
//        }
//        holder.name.setText(data[position].name)
//        Log.e("examopt",data[position].questions.size.toString())
////        for(i in 0 until data[position].questions.size){
////
////            Log.e("examopt",data[position].questions[i].subname)
////            Log.e("examopt",data[position].questions[i].question)
////            Log.e("examopt",data[position].questions[i].option1)
////        }
//        holder.questions.setText(data[position].questions.size.toString())


    }

    override fun getItemCount(): Int {
//        return  5;
        return data.size

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }



}