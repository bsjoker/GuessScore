package com.sameluck.qrla

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        btn_football.setOnClickListener(this)
        btn_hokey.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_football -> startActivity(Intent(this@StartActivity, GameActivity::class.java).putExtra("game", "football"))
            R.id.btn_hokey -> startActivity(Intent(this@StartActivity, GameActivity::class.java).putExtra("game", "hokey"))
        }
    }
}
