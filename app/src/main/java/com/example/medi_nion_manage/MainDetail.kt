package com.example.medi_nion_manage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_item.view.*
import org.json.JSONArray

class MainDetail : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) { //프레그먼트로 생길 문제들은 추후에 생각하기,,
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_detail)

        refresh_layout.setColorSchemeResources(R.color.color5) //새로고침 색상 변경
        refresh_layout.setOnRefreshListener {

            try {
                //TODO 액티비티 화면 재갱신 시키는 코드
                val intent = intent
                finish() //현재 액티비티 종료 실시
                overridePendingTransition(0, 0) //인텐트 애니메이션 없애기
                startActivity(intent) //현재 액티비티 재실행 실시
                overridePendingTransition(0, 0) //인텐트 애니메이션 없애기
//                commentDetailadapter = CommentDetailListAdapter(commentDetail_items)
//                Commentadapter = CommentListAdapter(comment_items)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            refresh_layout.isRefreshing = false //새로고침 없애기
        }

// ================================================= 변수 ==================================================================
        //MainActivity.kt에서 MainDetail.kt로 데이터 intent
        var id = intent.getStringExtra("id") //인증 요청한 유저의 아이디
        val identity = intent.getStringExtra("identity") //인증
        val image = intent.getStringExtra("image") //게시물 사진

        val id_textView = findViewById<TextView>(R.id.id_TextView)
        val identity_textView = findViewById<TextView>(R.id.identity_TextView)
        val check_button = findViewById<Button>(R.id.check_Btn)
        val nocheck_button = findViewById<Button>(R.id.nocheck_Btn)
        val image_imageView = findViewById<ImageView>(R.id.image)
        image_imageView.visibility = View.VISIBLE
        val bitmap: Bitmap? = StringToBitmaps(image)

        var intent = Intent(this, MainActivity::class.java)

        id_textView.setText(id) // 아이디
        identity_textView.setText(identity) // 신분증 text
        image_imageView.setImageBitmap(bitmap) // opencv 이미지

        check_button.setOnClickListener {
            CheckRequest()
            Toast.makeText(applicationContext, "승인되었습니다.", Toast.LENGTH_SHORT).show()
        }

        nocheck_button.setOnClickListener {
            Toast.makeText(applicationContext, "반려되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }
    }

    fun CheckRequest() {
        val url = "http://seonho.dothome.co.kr/Check_Identity.php"

        var id = intent?.getStringExtra("id").toString() //요청한 사람의 아이디
        var identity = intent?.getStringExtra("identity").toString() //요청한 사람의 민증 번호
        var image = intent?.getStringExtra("image").toString() //opencv 사진
        var identity_check = "false"

        val jsonArray: JSONArray

        val request = Login_Request(
            Request.Method.POST,
            url,
            { response ->
                if (response != "check success") {
                    val jsonArray = JSONArray(response)

                    for (i in 0 until jsonArray.length()) {

                        val item = jsonArray.getJSONObject(i)

                        id = item.getString("id")
                        identity_check = item.getString("identity_check")

                        Log.d("IDENTITY", "$id, $identity_check")
                    }
                }

            }, { Log.d("like Failed", "error......${error(applicationContext)}") },
            hashMapOf(
                "id" to id,
                "identity_check" to identity_check
            )
        )
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    // 이미지 : String -> Bitmap 변환 =====================================================================================================
    fun StringToBitmaps(image: String?): Bitmap? {
        try {
            val encodeByte = Base64.decode(image, Base64.DEFAULT)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            return bitmap
        } catch (e: Exception) {
            e.message
            return null
        }
    }
}
