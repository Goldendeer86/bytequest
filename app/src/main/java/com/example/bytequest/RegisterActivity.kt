package com.example.bytequest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val pwch = findViewById<EditText>(R.id.pwcheck)
        val checkUsernameButton = findViewById<Button>(R.id.check_username)

        checkUsernameButton.setOnClickListener {
            val username = usernameEditText.text.toString()

            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("username", username)
                .build()
            val request = Request.Builder()
                .url("http://54.79.97.174/check_username.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "에러: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "$responseBody", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "응답 실패: $responseBody", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        findViewById<Button>(R.id.register).setOnClickListener {
            val username = usernameEditText.text.toString()
            val userpw = passwordEditText.text.toString()
            val pwch = pwch.text.toString()

            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("username", username)
                .add("userpw", userpw)
                .add("userpw_ch", pwch)
                .add("submit", "submit") // 'submit' 파라미터 추가
                .build()
            val request = Request.Builder()
                .url("http://54.79.97.174//register.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() // 응답 내용 확인
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "회원가입 성공: $responseBody", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "회원가입 실패: $responseBody", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }
}
