package com.example.bytequest

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class SelectSubActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var selectedSID: String? = null
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

        // LinearLayout에 접근합니다
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)

        // 코루틴을 사용하여 백그라운드에서 데이터를 가져옵니다
        CoroutineScope(Dispatchers.Main).launch {
            val subjects = withContext(Dispatchers.IO) { fetchSubjects() }

            // 가져온 데이터의 수만큼 반복합니다
            for (i in 0 until subjects.length()) {
                // 각 JSONObject에서 SID와 Sname 값을 가져옵니다
                val subject = subjects.getJSONObject(i)
                val SID = subject.getString("SID")
                val Sname = subject.getString("Sname")

                // 새로운 버튼을 생성합니다
                val button = Button(this@SelectSubActivity).apply {
                    id = View.generateViewId() // 고유한 ID를 생성합니다
                    text = "$SID: $Sname" // SID와 Sname 값을 텍스트로 설정합니다
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 16.dp
                    }


                    // 버튼 클릭 리스너를 추가합니다
                    setOnClickListener {
                        // 이전에 선택된 버튼이 있다면, 그 버튼의 상태를 초기화합니다
                        selectedButtons[selectedSID]?.setBackgroundColor(Color.WHITE)

                        if (selectedSID == SID) {
                            // 버튼이 이미 선택되어 있으면 선택을 해제합니다
                            selectedButtons.remove(SID)
                            selectedSID = null
                        } else {
                            // 버튼이 선택되지 않았으면 선택합니다
                            selectedButtons[SID] = this
                            setBackgroundColor(Color.GREEN) // 선택된 배경색으로 변경합니다
                            selectedSID = SID
                        }
                    }
                }


                // LinearLayout에 버튼을 추가합니다
                linearLayout.addView(button)
            }
        }

        selectBtn.setOnClickListener {
            val intent = Intent(this, SelectClustActivity::class.java)
            // Intent에 SID 값을 추가합니다
            intent.putExtra("SID", selectedSID)
            // Intent에 username 값을 추가합니다
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    // dp를 픽셀로 변환하는 확장 함수입니다
    private val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    // 서버에서 과목 데이터를 가져오는 함수입니다
    private suspend fun fetchSubjects(): JSONArray {
        val request = Request.Builder()
            .url("http://54.79.97.174/select_sub.php")
            .build()

        client.newCall(request).execute().use { response ->
            val jsonData = response.body?.string()
            if (jsonData != null) {
                Log.d("ServerResponse", jsonData)
            }
            return JSONArray(jsonData)
        }
    }
}