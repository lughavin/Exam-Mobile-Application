package com.yorbax.EMA.Utils;

import com.yorbax.EMA.Constants.Constants;
import com.yorbax.EMA.Model.UserModel;
import com.yorbax.EMA.admin.model.LectureModel;

import io.paperdb.Paper;

import static com.yorbax.EMA.Constants.Constants.KEY_CURRENT_USER;

public class HelperMethods {
    public static void saveUserData(UserModel currentUser) {
        Paper.book().write(KEY_CURRENT_USER, currentUser);
        Constants.currentUser = currentUser;

    }
    public static void saveUserLecturerData(LectureModel currentUser) {
        Paper.book().write(KEY_CURRENT_USER, currentUser);
        Constants.lectureCurrentUser = currentUser;

    }


}
