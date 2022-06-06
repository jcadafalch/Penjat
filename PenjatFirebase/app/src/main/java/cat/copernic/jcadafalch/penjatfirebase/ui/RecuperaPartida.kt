package cat.copernic.jcadafalch.penjatfirebase.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.get
import cat.copernic.jcadafalch.penjatfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_recupera_partida.*


@SuppressLint("StaticFieldLeak")
private val db = Firebase.firestore

class RecuperaPartida : AppCompatActivity() {
    private lateinit var username:  String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recupera_partida)

        val bundle = intent.extras
        username = bundle?.getString("username").toString()
        setup()

        //Connexion(this).isInternetAvailable(this)
        //Connexion(this).getConnectionState()
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

    private fun showRanking(){
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

    private fun setup(){
        title = ""

        recuperaPartidabutton.setOnClickListener {
            recoverGame()
        }
        novaPartidabutton.setOnClickListener {
            newGame()
        }
    }

    private fun newGame() {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("username", username)
            putExtra("started", true)
        }
        newSecretWord()

        Handler().postDelayed(
                {
                    startActivity(homeIntent)
                },
            500
        )
    }

    private fun recoverGame() {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("username", username)
            putExtra("started", true)
        }
        finish()
        startActivity(homeIntent)
    }


    private fun newSecretWord() {
        changedataTxt2.text = ""
        db.collection("words").document("arrWords").get().addOnSuccessListener { document ->
            if (document != null) {
                changedataTxt2.text = randomWord(document.get("arrayW").toString())
                createNewRound()
            }
        }
    }

    private fun createNewRound() {
        val secret = changedataTxt2.text.length
        var xWord = ""
        for (i in 0 until secret) {
            xWord += "x"
        }
        db.collection("joc").document(username).set(
            hashMapOf(
                "maxErrors" to 6,
                "numErrors" to 0,
                "secretWord" to changedataTxt2.text.toString(),
                "secretWordL" to changedataTxt2.text.length,
                "started" to true,
                "tryedLetters" to "",
                "wordLenght" to 0,
                "xWord" to xWord,
                "points" to 70
            )
        )
    }

    private fun randomWord(word: String): String {
        val replace = word.replace("[", "")
        val replace1 = replace.replace("]", "")
        val arrWords = listOf(replace1.split(", ").toTypedArray())
        val rt = (0 until 109).random()
        return arrWords[0][rt]
    }
}