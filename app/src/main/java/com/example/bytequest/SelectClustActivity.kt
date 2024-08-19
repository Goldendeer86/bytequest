package com.example.bytequest

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class SelectClustActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    // SID 값을 저장할 변수를 추가합니다
    private val selected_clusterIDs = ArrayList<String>()

    // 선택된 버튼을 추적하는 Map을 추가합니다
    private val selectedButtons = mutableMapOf<String, Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_sub)

        // Button에 접근합니다
        val selectBtn = findViewById<Button>(R.id.select_btn)

        // 인텐트를 가져옵니다
        val intent = getIntent()

        // 인텐트에서 추가 데이터를 가져올 수 있습니다
        val username = intent.getStringExtra("username")
        val SID = intent.getStringExtra("SID")

        // LinearLayout에 접근합니다
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)

        // 코루틴을 사용하여 백그라운드에서 데이터를 가져옵니다
        CoroutineScope(Dispatchers.Main).launch {
            val clusters = withContext(Dispatchers.IO) { fetchClusters(SID) }


            // 가져온 데이터의 수만큼 반복합니다
            for (i in 0 until clusters.length()) {
                // 각 JSONObject에서 SID와 Sname 값을 가져옵니다
                val subject = clusters.getJSONObject(i)
                val clusterID = subject.getString("clusterID")
                val cluster = subject.getString("cluster")

                // 새로운 버튼을 생성합니다
                val button = Button(this@SelectClustActivity).apply {
                    id = View.generateViewId() // 고유한 ID를 생성합니다
                    text = "$clusterID: $cluster" // clusterID와 cluster 값을 텍스트로 설정합니다
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 16.dp
                    }

                    // 버튼 클릭 리스너를 추가합니다
                    setOnClickListener {
                        if (selectedButtons.containsKey(clusterID)) {
                            // 버튼이 이미 선택되어 있으면 선택을 해제합니다
                            selectedButtons.remove(clusterID)
                            setBackgroundColor(Color.WHITE) // 원래의 배경색으로 변경합니다
                            selected_clusterIDs.remove(clusterID)
                        } else {
                            // 버튼이 선택되지 않았으면 선택합니다
                            selectedButtons[clusterID] = this
                            setBackgroundColor(Color.GREEN) // 선택된 배경색으로 변경합니다
                            selected_clusterIDs.add(clusterID)
                        }
                    }
                }


                // LinearLayout에 버튼을 추가합니다
                linearLayout.addView(button)
            }
        }

        selectBtn.setOnClickListener {
            val intent = Intent(this, QuizQuestionsActivity::class.java)
            // Intent에 SID 값을 추가합니다
            intent.putExtra("clusterIDs", selected_clusterIDs.joinToString(","))
            // Intent에 username 값을 추가합니다
            intent.putExtra("username", username)
            intent.putExtra("SID", SID)
            startActivity(intent)
        }

    }

    // dp를 픽셀로 변환하는 확장 함수입니다
    private val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    // 서버에서 클러스터 데이터를 가져오는 함수입니다
    private suspend fun fetchClusters(SID: String?): JSONArray {
        val requestBody = FormBody.Builder()
            .add("SID", SID ?: "")
            .build()

        val request = Request.Builder()
            .url("http://54.79.97.174/select_cluster.php")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val jsonData = response.body?.string()
            return JSONArray(jsonData)
        }
    }
}