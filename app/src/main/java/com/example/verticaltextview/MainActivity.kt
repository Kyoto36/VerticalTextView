package com.example.verticaltextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ls.library_verticaltextview.VerticalTextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val verticalTextView = findViewById<VerticalTextView>(R.id.verticalTextView)

        val text = "计算发达的机房\n姐姐撒到了冬季斯\n洛伐克将阿隆索飞机阿萨德开理发店师傅"
        verticalTextView.post {
            val preLength = verticalTextView.getPreTextLength()
            Log.d("MainActivity", "preLength $preLength")
            val bool = verticalTextView.canFillFull(text)
            Log.d("MainActivity", "canFillFull $bool")
            if(bool){
                verticalTextView.setText(text)
            }
        }
    }
}