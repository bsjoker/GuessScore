package com.sameluck.qrla

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import kotlin.collections.HashMap

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private val arrayOfNums = Arrays.asList(
        R.drawable.white00,
        R.drawable.white01,
        R.drawable.white02,
        R.drawable.white03,
        R.drawable.white04,
        R.drawable.white05,
        R.drawable.white06,
        R.drawable.white07,
        R.drawable.white08,
        R.drawable.white09
    )

    private val arrayOfNumsPink = Arrays.asList(
        R.drawable.pink00,
        R.drawable.pink01,
        R.drawable.pink02,
        R.drawable.pink03,
        R.drawable.pink04,
        R.drawable.pink05,
        R.drawable.pink06,
        R.drawable.pink07,
        R.drawable.pink08,
        R.drawable.pink09
    )

    private val arrayOfFootball = Arrays.asList(
        R.drawable.football01,
        R.drawable.football02,
        R.drawable.football03,
        R.drawable.football04,
        R.drawable.football05,
        R.drawable.football06,
        R.drawable.football07,
        R.drawable.football08,
        R.drawable.football09,
        R.drawable.football10
    )

    private val arrayOfHokey = Arrays.asList(
        R.drawable.hockey01,
        R.drawable.hockey02,
        R.drawable.hockey03,
        R.drawable.hockey04,
        R.drawable.hockey05,
        R.drawable.hockey06,
        R.drawable.hockey07,
        R.drawable.hockey08,
        R.drawable.hockey09,
        R.drawable.hockey10
    )

    var teamNumL = 0
    var teamNumR = 0
    var scoreMap: HashMap<Int, Int> = HashMap(2)
    var typeGame = ""
    var isFirst = true
    var sovpadenie = true

    lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        typeGame = intent.getStringExtra("game")

        startGame()

        btn_close.setOnClickListener(this)
        btn_again.setOnClickListener(this)

        adapter = ImageAdapter(this, R.layout.list_item, arrayOfNums)
        gridview.adapter = adapter

        gridview.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            Log.d("TAG", "Position: " + position)
            if (isFirst) {
                scorePlayerL.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        arrayOfNumsPink[position]
                    )
                )
                scoreMap.put(teamNumL, position)
                isFirst = false
            } else {
                scorePlayerR.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        arrayOfNumsPink[position]
                    )
                )
                scoreMap.put(teamNumR, position)
                checkWin(scoreMap)
            }
        }
    }

    private fun startGame() {
        teamNumL = (0..9).random()
        teamNumR = (0..9).random()

        isFirst = true

        when (typeGame) {
            "football" -> {
                teamL.setImageDrawable(ContextCompat.getDrawable(this, arrayOfFootball[teamNumL]))
                teamR.setImageDrawable(ContextCompat.getDrawable(this, arrayOfFootball[teamNumR]))
            }
            "hokey" -> {
                teamL.setImageDrawable(ContextCompat.getDrawable(this, arrayOfHokey[teamNumL]))
                teamR.setImageDrawable(ContextCompat.getDrawable(this, arrayOfHokey[teamNumR]))
            }
        }
        teamL.animateWithTransform(-300.0f)
        teamR.animateWithTransform(300.0f)
    }

    private fun resetViews() {
        scoreL.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.qst))
        scoreR.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.qst))
        scorePlayerL.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.qst))
        scorePlayerR.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.qst))

        gridview.visibility = View.VISIBLE
        ivResult.visibility = View.GONE
        llAgain.visibility = View.GONE
    }

    private fun checkWin(ee: HashMap<Int, Int>) {
        gridview.visibility = View.INVISIBLE

        scoreL.setImageDrawable(ContextCompat.getDrawable(this, arrayOfNumsPink[teamNumL]))
        scoreR.setImageDrawable(ContextCompat.getDrawable(this, arrayOfNumsPink[teamNumR]))

        ee.forEach {
            if (it.key != it.value) {
                sovpadenie = false
            }
        }

        with(ivResult) {
            setImageDrawable(ContextCompat.getDrawable(this@GameActivity,
                run {
                    if (sovpadenie) R.drawable.you_guessed_ else R.drawable.you_did_not_guess_
                }
            ))
            visibility = View.VISIBLE
        }

        llAgain.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_close -> finish()
            R.id.btn_again -> {
                resetViews()
                startGame()
            }
        }
    }

    private fun ImageView.animateWithTransform(trX: Float) {
        val oa = ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat("translationX", trX, translationX),
            PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f)
        ).apply {
            duration = 500
            interpolator = FastOutSlowInInterpolator()
        }
        oa.start()
    }
}
