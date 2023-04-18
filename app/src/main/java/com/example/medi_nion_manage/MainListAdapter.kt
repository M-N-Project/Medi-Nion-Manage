package com.example.medi_nion_manage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainListAdapter(private val itemList : ArrayList<MainItem>) : RecyclerView.Adapter<MainListAdapter.ViewHolder>()  {

    interface OnItemClickListener{
        fun onItemClick(v:View, data: MainItem, pos : Int)
    }
    private var listener : OnItemClickListener? = null


    fun setOnItemClickListener(listener: MainListAdapter.OnItemClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.activity_main_item, parent, false)
        return ViewHolder(inflatedView);
    }

    override fun getItemCount(): Int = itemList.size //라이브데이터 사용할때는 datas사용, 지금은 더미 데이터라서 매개변수로 넘긴 itemList로 대체


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val safePosition = holder.absoluteAdapterPosition
        holder.bind(itemList[safePosition])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemId: TextView = itemView.findViewById(R.id.id_TextView)
        private val itemTime : TextView = itemView.findViewById(R.id.time_TextView)
        private val itemImg : ImageView = itemView.findViewById(R.id.image_imageView)


        fun bind(item: MainItem) {
            itemId.text = item.id
            itemTime.text = item.time

            if(item.image != "null"){
                val bitmap: Bitmap? = StringToBitmaps(item.image)
                itemImg.setImageBitmap(bitmap)
            }

            val pos = absoluteAdapterPosition
            if(pos!= RecyclerView.NO_POSITION)
            {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView,item,pos)
                }
                itemId.setOnClickListener {
                    listener?.onItemClick(itemView,item,pos)
                }
            }
        }
    }

    // String -> Bitmap 변환
    fun StringToBitmaps(image: String?): Bitmap? {
        try {
            val encodeByte = Base64.decode(image, Base64.DEFAULT)
            val bitmap : Bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            return bitmap
        } catch (e: Exception) {
            e.message
            return null
        }
    }

}