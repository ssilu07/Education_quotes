package com.royal.edunotes._activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.royal.edunotes.R
import com.royal.edunotes.Utility.ScreenCheck

class SelectionActivity : AppCompatActivity(), View.OnClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val box1: CardView = findViewById(R.id.vocab)
        val box2: CardView = findViewById(R.id.idiom)
        val box3: CardView = findViewById(R.id.box3)
        val box4: CardView = findViewById(R.id.box4)

        // Set click listeners
        box1.setOnClickListener(this)
        box2.setOnClickListener(this)
        box3.setOnClickListener(this)
        box4.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.vocab ->{
                ScreenCheck = "Vocab"

                startActivity(
                    Intent(
                        this@SelectionActivity,
                        MainActivity::class.java
                    )
                )
            }

            R.id.idiom ->{
                ScreenCheck = "Idiom"

                startActivity(
                    Intent(
                        this@SelectionActivity,
                        IdiomPhrasesActivity::class.java
                    )
                )

            }
            R.id.box3 -> showToast("Box 3 Clicked")
            R.id.box4 -> showToast("Box 4 Clicked")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}