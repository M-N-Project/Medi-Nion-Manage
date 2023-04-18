package com.example.medi_nion_manage

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manage_login.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

private var items = ArrayList<MainItem>()
private var all_items = ArrayList<MainItem>()
private val item_count = 20 // 초기 20개의 아이템만 불러오게 하고, 스크롤 시 더 많은 아이템 불러오게 하기 위해
private var scroll_count = 1
//val viewModel = BoardViewModel()
//lateinit var adapter : BoardListAdapter
private var adapter = MainListAdapter(items)
private var scrollFlag = false
private var itemIndex = ArrayList<Int>()


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart() //프레그먼트로 생길 문제들은 추후에 생각하기,,
        fetchData()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refresh_layout.setColorSchemeResources(R.color.color5) //새로고침 색상 변경

        items.clear()
        all_items.clear()

        mainRecyclerView.setLayoutManager(mainRecyclerView.layoutManager);

        var id = intent.getStringExtra("id")

        mainRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(scrollFlag==false){
                    if (!mainRecyclerView.canScrollVertically(-1)) { //맨 위

                        refresh_layout.setOnRefreshListener { //새로고침
                            Log.d("omg", "hello refresh")

                            try {
                                //TODO 액티비티 화면 재갱신 시키는 코드
                                val intent = intent
                                finish() //현재 액티비티 종료 실시
                                overridePendingTransition(0, 0) //인텐트 애니메이션 없애기
                                startActivity(intent) //현재 액티비티 재실행 실시
                                overridePendingTransition(0, 0) //인텐트 애니메이션 없애기

                                refresh_layout.isRefreshing = false //새로고침 없애기
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    } else if (!mainRecyclerView.canScrollVertically(1)) { //맨 아래
                        //로딩
                        if(all_items.size > 20){
                            scrollFlag = true

                            Log.d("attention", "let it be")
                            var progressBar : ProgressBar = findViewById(R.id.progressBar2)
                            progressBar.visibility = View.VISIBLE

                            Handler(Looper.getMainLooper()).postDelayed({
                                progressBar.visibility = View.INVISIBLE
                            }, 2500)


                            if((all_items.size - item_count*scroll_count) > 20){
                                for (i in (item_count * scroll_count) + (item_count-1)  downTo   (item_count * scroll_count) + 0) {
                                    items.add(all_items[i])
//                                    itemIndex.add(all_items[i].id) //앞에다가 추가.
                                }

                                var recyclerViewState = mainRecyclerView.layoutManager?.onSaveInstanceState()
                                var new_items = ArrayList<MainItem>()
                                new_items.addAll(items)
                                adapter = MainListAdapter(new_items)
                                mainRecyclerView.adapter = adapter
                                adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
                                mainRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)

                                scrollFlag = false
                            }
                            else{
                                for (i  in all_items.size-1  downTo   (item_count* scroll_count)) {
                                    items.add(all_items[i])
//                                    itemIndex.add(all_items[i].num) //앞에다가 추가.

                                }
                                var recyclerViewState = mainRecyclerView.layoutManager?.onSaveInstanceState()
                                var new_items = ArrayList<MainItem>()
                                new_items.addAll(items)
                                adapter = MainListAdapter(new_items)
                                mainRecyclerView.adapter = adapter
                                adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
                                mainRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
                            }

                            scroll_count ++
                        }
                    }
                }
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchData() {
        // url to post our data
        val identity_check = "false"
        val urlBoard = "http://seonho.dothome.co.kr/Identity_Select.php"
        val urlDetail = "http://seonho.dothome.co.kr/Identity_Detail.php"

        val request = Main_Request (
            Request.Method.POST,
            urlBoard,
            { response ->
                val jsonArray = JSONArray(response)
                items.clear()
                all_items.clear()

                for (i in jsonArray.length()-1  downTo  0) {
                    val item = jsonArray.getJSONObject(i)

                    val id = item.getString("id")
                    val identity = item.getString("identity")
                    val time = item.getString("time")
                    val image = item.getString("image")

                    val simpleTime = timeDiff(time)

                    val boardItem = MainItem(id, identity, time, image)

                    if(i >= jsonArray.length() - item_count*scroll_count){
                        items.add(boardItem)
//                        itemIndex.add(num) //앞에다가 추가.
                    }

                    all_items.add(boardItem)
                }
                var recyclerViewState = mainRecyclerView.layoutManager?.onSaveInstanceState()
                var new_items = ArrayList<MainItem>()
                new_items.addAll(items)
                adapter = MainListAdapter(new_items)
                mainRecyclerView.adapter = adapter
                adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
                mainRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState);


                var detailId : String = ""
                var detailIdentity : String = ""
                var detailTime : String = ""
                var detailImg : String = ""

                //게시판 상세
                adapter.setOnItemClickListener(object : MainListAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, data: MainItem, pos: Int) {

                        val request = Main_Request (
                            Request.Method.POST,
                            urlBoard,
                            { responseDetail ->
                                 val jsonArray = JSONArray(responseDetail)
                                items.clear()
                                for (i in jsonArray.length()-1  downTo  0) {
                                    val item = jsonArray.getJSONObject(i)


                                    detailId = item.getString("id")
                                    detailIdentity = item.getString("identity")
                                    detailTime = item.getString("time")
                                    detailImg= item.getString("image")
                                    Log.d("DETAIL", "$detailId, $detailIdentity")

                                    val intent = Intent(applicationContext, MainDetail::class.java)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //인텐트 플래그 설정
                                    intent.putExtra("id", data.id)
                                    intent.putExtra("identity", data.identity)
                                    intent.putExtra("time", data.time)
                                    intent.putExtra("image", data.image)
                                    startActivity(intent)
                                }

                            }, { Log.d("login failed", "error......${error(applicationContext)}") },
                            hashMapOf(
                                "id" to data.id,
                                "identity_check" to identity_check
                            )
                        )
                        val queue = Volley.newRequestQueue(applicationContext)
                        queue.add(request)
                    }

                })
            }, { Log.d("login failed", "error......${error(applicationContext)}") },
            hashMapOf(
                "identity_check" to identity_check
            )
        )

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Millis(postTime : String) : Long {
        // YY-MM-DD HH:MM:SS

        //val formatter = DateTimeFormatter.ofPattern("yyyy-mm-dd, hh:mm:ss")
        //val date = LocalDateTime.parse(dateString, formatter)

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date1: Date = simpleDateFormat.parse(postTime)
        return date1.time
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun timeDiff(postTime : String): String {
        var SEC = 60
        var MIN = 60
        var HOUR = 24
        var DAY = 30
        var MONTH = 12

        val curTime = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ko", "KR"))
        val cur: String = simpleDateFormat.format(Date(curTime))

        val newPostTime = Millis(postTime)
        var diffTime = (curTime - newPostTime)/1000
        var msg: String = ""

        if (diffTime  < SEC) {
            msg = "방금 전";
        } else if ((diffTime / SEC) < MIN) {
            msg = (diffTime / SEC).toString() + "분 전";
        } else if (((diffTime / SEC) / MIN) < HOUR) {
            msg = ((diffTime / SEC) / MIN).toString() + "시간 전";
        } else if ((((diffTime / SEC) / MIN) / HOUR) < DAY) {
            msg = (((diffTime / SEC) / MIN) / HOUR).toString() + "일 전";
        } else if (((((diffTime / SEC) / MIN) / HOUR) / DAY) < MONTH) {
            msg = ((((diffTime / SEC) / MIN) / HOUR) / DAY).toString() + "달 전";
        } else {
            msg = (((((diffTime / SEC) / MIN) / HOUR) / DAY) / MONTH ).toString() + "년 전"
        }

        return msg
    }
}

