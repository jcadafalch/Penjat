package cat.copernic.jcadafalch.penjatfirebase.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import cat.copernic.jcadafalch.penjatfirebase.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.StringBuilder
import java.sql.Timestamp
import java.time.Instant
import kotlin.collections.hashMapOf as hashMapOf
import android.os.Vibrator
import android.os.Build


@SuppressLint("StaticFieldLeak")
private val db = Firebase.firestore


class HomeActivity : AppCompatActivity() {
    private lateinit var username: String
    private var tryedLetters = ""
    private var secretWord = ""
    private var xWord = ""
    private var maxErrors: Int = 6
    private var numErrors: Int = 0
    private var wordLength: Int = 0
    private var finishRound = false
    private var points = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bundle = intent.extras
        username = bundle?.getString("username").toString()
        setup()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu?.get(0)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logOutIcon -> {
                FirebaseAuth.getInstance().signOut()
                finish()
                showAuth()
                true
            }
            R.id.ranking -> {
                showRanking()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun showAuth() {
        val authIntent = Intent(this, AuthActivity::class.java).apply {
        }
        startActivity(authIntent)
    }

    private fun showRanking() {
        val rankingIntent = Intent(this, RankingActivity::class.java).apply {
        }
        startActivity(rankingIntent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        FirebaseAuth.getInstance().signOut()
        showAuth()
        finish()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setup() {
        title = ""
        val user = getString(R.string.benvigut,username).toString()
        emailTextView.text = user

        getAllValues()

        Handler().postDelayed(
            {

            },
            500
        )

        enviaButton.setOnClickListener {
            when {
                editTextTextPersonName.toString().isEmpty() -> {
                    Toast.makeText(this, getString(R.string.nope), Toast.LENGTH_SHORT)
                        .show()
                    vibracioN(false)
                }
                editTextTextPersonName.toString().isNotEmpty() -> {
                    if (oneCharacter()) {
                        val c = editTextTextPersonName.text[0].toString().uppercase()
                        tryedLetters += c
                        txtTryedLetters.text = tryedLetters
                        updateTryedLetters()
                        var guessCorrectWord = false
                        var i = 0
                        while (i < secretWord.length) {
                            if (secretWord[i].toString() == c) {
                                xWord =
                                    StringBuilder(xWord).also { it.setCharAt(i, c[0]) }.toString()
                                guessCorrectWord = true
                                wordLength++
                            }
                            updateWordLength()
                            i++
                        }
                        i = 0
                        var rightLetters = ""
                        while (i < secretWord.length) {
                            rightLetters += xWord[i]
                            i++
                        }
                        vibracioN(guessCorrectWord)
                        updateXWord()
                        if (!guessCorrectWord) {
                            Toast.makeText(
                                this,
                                getString(R.string.noconte,c),
                                Toast.LENGTH_SHORT
                            ).show()
                            numErrors++
                            points -= 10
                            setImageErrors(numErrors)
                            updateNumErrors()
                            updatePoints()
                        }
                    }
                    else{
                        vibracioN(false)
                    }
                    editTextTextPersonName.setText("")
                    getAllValues()

                    if (maxErrors - numErrors == 0) {
                        setStartedFalse()
                        setUserPoint()
                        finishRound = true
                        showOver()
                    }
                    if (wordLength == secretWord.length) {
                        setStartedFalse()
                        setUserPoint()
                        finishRound = true
                        showWon()
                    }
                }
            }
        }
    }

    private fun oneCharacter(): Boolean {
        return if(!editTextTextPersonName.text.isNullOrEmpty())  {
            if (Character.isLetter(editTextTextPersonName.text[0])){
                val c = editTextTextPersonName.text[0].toString().uppercase()
                if (repeatedLetter(c)) {
                    Toast.makeText(this,getString(R.string.provat) , Toast.LENGTH_SHORT).show()
                    false
                } else {
                    true
                }
            }else{
                Toast.makeText(this, getString(R.string.noes), Toast.LENGTH_SHORT).show()
                false
            }
        }else{
            Toast.makeText(this, getString(R.string.noes), Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun repeatedLetter(c: String): Boolean {
        var repeatedLetter = false
        var i = 0
        while (i < tryedLetters.length) {
            if (tryedLetters[i].toString() == c) {
                repeatedLetter = true
            }
            i++
        }

        return repeatedLetter
    }

    private fun getAllValues() {
        db.collection("joc").document(username).get().addOnSuccessListener { document ->
            if (document != null) {
                wordLength = document.get("wordLenght").hashCode()
                maxErrors = document.get("maxErrors").hashCode()
                numErrors = document.get("numErrors").hashCode()
                txtNumErrors.text = document.get("numErrors").hashCode().toString()
                secretWord = document.get("secretWord").toString()
                xWord = document.get("xWord").toString()
                textViewPIniciada.text = document.get("xWord").toString()
                tryedLetters = document.get("tryedLetters").toString()
                txtTryedLetters.text = document.get("tryedLetters").toString()
                points = document.get("points").hashCode()
            }
        }
    }

    private fun setImageErrors(errors: Int) {
        when (errors) {
            0 -> penjat.setImageResource(R.mipmap.penjat0_foreground)
            1 -> penjat.setImageResource(R.mipmap.penjat1_foreground)
            2 -> penjat.setImageResource(R.mipmap.penjat2_foreground)
            3 -> penjat.setImageResource(R.mipmap.penjat3_foreground)
            4 -> penjat.setImageResource(R.mipmap.penjat4_foreground)
            5 -> penjat.setImageResource(R.mipmap.penjat5_foreground)
            6 -> penjat.setImageResource(R.mipmap.penjat6_foreground)
            else -> Toast.makeText(this, getString(R.string.errornum), Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateNumErrors() {
        db.collection("joc").document(username).update(
            hashMapOf(
                "numErrors" to numErrors
            ) as Map<String, Any>
        )
    }

    private fun updatePoints() {
        db.collection("joc").document(username).update(
            hashMapOf(
                "points" to points
            ) as Map<String, Any>
        )
    }

    private fun updateWordLength() {
        db.collection("joc").document(username).update(
            hashMapOf(
                "wordLenght" to wordLength
            ) as Map<String, Any>
        )
    }

    private fun updateTryedLetters() {
        db.collection("joc").document(username).update(
            hashMapOf(
                "tryedLetters" to tryedLetters
            ) as Map<String, Any>
        )
    }

    private fun updateXWord() {
        db.collection("joc").document(username).update(
            hashMapOf(
                "xWord" to xWord
            ) as Map<String, Any>
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUserPoint() {
        db.collection("users").document(username).get().addOnSuccessListener { document ->
            if (document != null) {
                db.collection("users").document(username).update(
                    hashMapOf(
                        "points" to document.get("points").hashCode() + points,
                        "date" to Timestamp.from(Instant.now())
                    ) as Map<String, Any>
                )
            }
        }
        Handler().postDelayed(
            {

            },
            100
        )

    }

    private fun setStartedFalse() {
        db.collection("joc").document(username).update(
            hashMapOf("started" to false) as Map<String, Any>
        )
    }

    private fun showWon() {
        val wonIntent = Intent(this, WonActivity::class.java).apply {
            putExtra("username", username)
            putExtra("secretWord", secretWord)
        }

        startActivity(wonIntent)
    }

    private fun showOver() {
        val overIntent = Intent(this, OverActivity::class.java).apply {
            putExtra("username", username)
            putExtra("secretWord", secretWord)
        }

        startActivity(overIntent)
    }

    private fun vibracioN(value : Boolean) {
        var sonido : MediaPlayer? = null
        if(value){
            sonido = MediaPlayer.create(this, R.raw.acert)
        } else{
            sonido = MediaPlayer.create(this, R.raw.error)
        }
        sonido.start()
        val v = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 1000, 300)
        v.vibrate(pattern, -1)
        v.cancel()
    }
}