package com.example.bytequest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signupBtn = findViewById<Button>(R.id.signup_btn)

        loginBtn.setOnClickListener {
            val client = OkHttpClient()
            val uname = findViewById<EditText>(R.id.etName).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()
            val requestBody = FormBody.Builder().add("username", uname).add("password", password).build()
            Log.d("PostRequest", "username: $uname, password: $password")
            val request = Request.Builder().url("http://54.79.97.174/login.php").post(requestBody).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("NetworkError", "Error: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {

                    if (response.isSuccessful) {
                        val responseString = response.body?.string()
                        val responseObject = try {
                            responseString?.let { it1 -> JSONObject(it1) }
                        } catch (e: JSONException) {
                            Log.e("JSONParsing", "Error parsing JSON", e)
                            null
                        }

                        runOnUiThread {
                            if (responseObject != null) {
                                if (responseObject.getString("status") == "success") {
                                    Toast.makeText(this@MainActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                                    // 로그인 성공 후 QuizQuestionsActivity로 이동
                                    val intent = Intent(this@MainActivity, SelectSubActivity::class.java)
                                    intent.putExtra("username", uname) // 닉네임 데이터 추가
                                    startActivity(intent)


                                }
                                else if(responseObject.getString("status") == "error") {
                                    Toast.makeText(this@MainActivity, "로그인 실패", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                else {
                                    Toast.makeText(this@MainActivity, "오류", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            else {
                                Log.e("ResponseError", "Response object is null")
                            }
                        }
                    }
                    else {
                        Log.e("ResponseError", "Response was not successful. HTTP status code: ${response.code}")
                    }

                }
            })
        }


        signupBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}