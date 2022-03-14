package com.yorbax.EMA.Constants;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.yorbax.EMA.Model.UserModel;
import com.yorbax.EMA.admin.model.LectureModel;

public class Constants {
    public static FirebaseAuth mAuth;
    public static DatabaseReference mDatabase;
    public static UserModel currentUser;
    public static LectureModel lectureCurrentUser;
    public static String KEY_CURRENT_USER = "KEY_CURRENT_USER ";
}
