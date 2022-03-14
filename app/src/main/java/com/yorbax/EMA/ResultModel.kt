package com.yorbax.EMA

import java.io.Serializable

class ResultModel : Serializable {
    var correctAnsCount = ""
    var examId = ""
    var lecturerId = ""
    var studentId = ""
    var answerList = ArrayList<AnswerList>()
    var ispublished = false

}