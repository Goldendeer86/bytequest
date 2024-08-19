package com.example.bytequest
data class Question(
    val id: String,
    val questionText: String,
    val alternatives: ArrayList<String>,
    val correctAnswers: ArrayList<Boolean>  // 각 선택지가 정답인지 아닌지를 나타냅니다.
)
