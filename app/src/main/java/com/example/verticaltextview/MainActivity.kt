package com.example.verticaltextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ls.library_verticaltextview.VerticalTextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val verticalTextView = findViewById<VerticalTextView>(R.id.verticalTextView)

        verticalTextView.setText("回家啊觉得富士\n康借鸡生蛋翻了翻吉林省地方；计算机房姐姐撒到了冬季斯洛伐克将阿隆索飞机阿萨德开理发店师傅")
    }
}