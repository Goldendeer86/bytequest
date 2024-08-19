package com.example.bytequest

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class QuizQuestionsActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private var userName: String? = null
    private var clusterIDs: String? = null

    private lateinit var questionsList: ArrayList<Question>

    private var currentQuestionIndex = 0;
    private var selectedAlternativeIndex = -1;
    private var isAnswerChecked = false;
    private var totalScore = 0;
    private val alternativesIds = arrayOf(R.id.optionOne, R.id.optionTwo, R.id.optionThree, R.id.optionFour)

    private var tvQuestion: TextView? = null
    private var progressBar: ProgressBar? = null
    private var tvProgress: TextView? = null
    private var btnSubmit: Button? = null
    private var tvAlternatives: ArrayList<TextView>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)

        userName = intent.getStringExtra("username")
        clusterIDs = intent.getStringExtra("clusterIDs")
        val SID = intent.getStringExtra("SID")
        // 코루틴을 사용하여 백그라운드에서 데이터를 가져옵니다
        CoroutineScope(Dispatchers.Main).launch {
            questionsList = withContext(Dispatchers.IO) { fetchQuestions(SID) }
            updateQuestion()
            // 나머지 코드...
        }

        tvQuestion = findViewById(R.id.tvQuestion)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvAlternatives = arrayListOf(
            findViewById(R.id.optionOne),
            findViewById(R.id.optionTwo),
            findViewById(R.id.optionThree),
            findViewById(R.id.optionFour),
        )


        btnSubmit?.setOnClickListener {
            if (!isAnswerChecked) {
                val anyAnswerIsChecked = selectedAlternativeIndex != -1
                if (!anyAnswerIsChecked) {
                    Toast.makeText(this, "선택지를 고르세요.", Toast.LENGTH_SHORT).show()
                } else {
                    val currentQuestion = questionsList[currentQuestionIndex]
                    val allCorrectAnswersSelected = currentQuestion.correctAnswers.indices.all { index ->
                        (currentQuestion.correctAnswers[index] && selectedAlternativeIndex == index) ||
                                (!currentQuestion.correctAnswers[index] && selectedAlternativeIndex != index)
                    }
                    if (allCorrectAnswersSelected) {
                        answerView(tvAlternatives!![selectedAlternativeIndex], R.drawable.correct_option_border_bg)
                        totalScore++
                    } else {
                        answerView(tvAlternatives!![selectedAlternativeIndex], R.drawable.wrong_option_border_bg)
                        currentQuestion.correctAnswers.forEachIndexed { index, isCorrect ->
                            if (isCorrect) {
                                answerView(tvAlternatives!![index], R.drawable.correct_option_border_bg)
                            }
                        }
                    }

                    isAnswerChecked = true
                    btnSubmit?.text = if (currentQuestionIndex == questionsList.size - 1) "끝입니다!" else "다음 질문"
                    selectedAlternativeIndex = -1
                }
            } else {
                if (currentQuestionIndex < questionsList.size - 1) {
                    currentQuestionIndex++
                    updateQuestion()
                } else {
                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("username", userName)
                    intent.putExtra("TOTAL_QUESTIONS", questionsList.size)
                    intent.putExtra("SCORE", totalScore)
                    startActivity(intent)
                    finish()
                }

                isAnswerChecked = false
            }
        }

        tvAlternatives?.let {
            for (optionIndex in it.indices) {
                it[optionIndex].let {
                    it.setOnClickListener{
                        if (!isAnswerChecked) {
                            selectedAlternativeView(it as TextView, optionIndex)
                        }
                    }
                }
            }
        }
    }

    private fun updateQuestion() {
        defaultAlternativesView()

        // Render Question Text
        tvQuestion?.text = questionsList[currentQuestionIndex].questionText
        // progressBar
        progressBar?.progress = currentQuestionIndex + 1
        // Text of progress bar
        tvProgress?.text = "${currentQuestionIndex + 1}/${questionsList.size}"

        for (alternativeIndex in questionsList[currentQuestionIndex].alternatives.indices) {
            tvAlternatives!![alternativeIndex].text = questionsList[currentQuestionIndex].alternatives[alternativeIndex]
        }

        btnSubmit?.text = if (currentQuestionIndex == questionsList.size - 1) "끝" else "선택"
    }

    private fun defaultAlternativesView() {
        for (alternativeTv in tvAlternatives!!) {
            alternativeTv.typeface = Typeface.DEFAULT
            alternativeTv.setTextColor(Color.parseColor("#7A8089"))
            alternativeTv.background = ContextCompat.getDrawable(
                this@QuizQuestionsActivity,
                R.drawable.default_option_border_bg
            )
        }
    }

    private fun selectedAlternativeView(option: TextView, index: Int) {
        defaultAlternativesView()
        selectedAlternativeIndex = index

        option.setTextColor(
            Color.parseColor("#363A43")
        )
        option.setTypeface(option.typeface, Typeface.BOLD)
        option.background = ContextCompat.getDrawable(
            this@QuizQuestionsActivity,
            R.drawable.selected_option_border_bg
        )
    }

    private fun answerView(view: TextView, drawableId: Int) {
        view.background = ContextCompat.getDrawable(
            this@QuizQuestionsActivity,
            drawableId
        )
        tvAlternatives!![selectedAlternativeIndex].setTextColor(
            Color.parseColor("#FFFFFF")
        )
    }
    // 서버에서 질문 데이터를 가져오는 함수입니다
    private suspend fun fetchQuestions(SID: String?): ArrayList<Question> {
        val requestBody = FormBody.Builder()
            .add("clusterIDs", clusterIDs ?: "")
            .add("SID", SID ?: "")
            .build()

        val request = Request.Builder()
            .url("http://54.79.97.174/select_question.php")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val jsonData = response.body?.string()
            Log.d("QuizApp", "Server response: $jsonData")  // 서버 응답을 로그에 출력합니다.
            val jsonArray = JSONArray(jsonData)
            return parseQuestions(jsonArray)
        }
    }

    private suspend fun fetchOptions(QID: String): Pair<ArrayList<String>, ArrayList<Boolean>> {
        val requestBody = FormBody.Builder()
            .add("QID", QID)
            .build()

        val request = Request.Builder()
            .url("http://54.79.97.174/select_options.php")  // 선택지를 가져오는 URL을 입력하세요.
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val jsonData = response.body?.string()
            Log.d("QuizApp", "Server response: $jsonData")  // 서버 응답을 로그에 출력합니다.
            val jsonArray = JSONArray(jsonData)
            val options = ArrayList<String>()
            val correctAnswers = ArrayList<Boolean>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                options.add(jsonObject.getString("Otext"))
                correctAnswers.add(jsonObject.getInt("TF") == 1)
            }
            return Pair(options, correctAnswers)
        }
    }

    private suspend fun parseQuestions(jsonArray: JSONArray): ArrayList<Question> {
        val questions = ArrayList<Question>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val (options, correctAnswers) = fetchOptions(jsonObject.getString("QID"))  // 선택지와 정답 정보를 가져옵니다.
            val question = Question(
                jsonObject.getString("QID"),
                jsonObject.getString("Question"),
                options,
                correctAnswers
            )
            questions.add(question)
        }

        return questions
    }

}
