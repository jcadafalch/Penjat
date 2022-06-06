package cat.copernic.jcadafalch.penjatfirebase.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import cat.copernic.jcadafalch.penjatfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_auth.emailEditText
import kotlinx.android.synthetic.main.activity_auth.passwordEditText
import kotlinx.android.synthetic.main.activity_registre.*
import java.sql.Timestamp
import java.time.Instant
import java.util.regex.Pattern

class RegistreActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var username: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registre)


        supportActionBar?.hide()

        setup()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setup() {
        title = ""
        singUpButtonR.setOnClickListener {
            if (datavalids(
                    emailEditText.text.toString(), passwordEditTextR.text.toString(),
                    passwordEditTextRepeated.text.toString()
                )
            ) {

                val email = emailEditText.text.toString()
                username = extractUsernameFromEmail(emailEditText.text.toString())
                val password = passwordEditTextR.text.toString()
                makeregister(email, password)
            }
        }

        logInButtonR.setOnClickListener {
            showAuth()
        }

    }

    private fun datavalids(email: String, password: String, password2: String): Boolean {
        var errorMessage = ""
        var bool = true

        if (email.isEmpty()) {
            errorMessage += getString(R.string.falta1)
            bool = false
        } else if (!checkEmailFormat(email)) {
            errorMessage += getString(R.string.falta2)
            bool = false
        }

        if (password.isEmpty()) {
            errorMessage += getString(R.string.falta3)
            bool = false
        }
        if (password2.isEmpty()) {
            errorMessage += getString(R.string.falta4)
            bool = false
        }

        if (password.length < 6) {
            errorMessage += getString(R.string.errormsg1)
            bool = false
        }

        if (password2.length < 6) {
            errorMessage += getString(R.string.errormsg2)
            bool = false
        }

        if (password.isNotEmpty() && password2.isNotEmpty()) {
            if (password.length >= 6 && password2.length >= 6) {
                if (password != password2) {
                    errorMessage += getString(R.string.errormsg3)
                    bool = false
                }
            }
        }

        if (errorMessage != "") {
            showAlert("¡¡¡ERROR!!!", errorMessage)
        }

        return bool

    }

    private fun checkEmailFormat(email: String): Boolean {
        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    private fun showAlert(title: String, msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.acceptar), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showAuth() {
        val authIntent = Intent(this, AuthActivity::class.java).apply { }
        startActivity(authIntent)
    }

    private fun showHome(email: String) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("username", extractUsernameFromEmail(email))
        }
        newSecretWord()
        Handler().postDelayed(
            {
                startActivity(homeIntent)
            },
            500
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeregister(email: String, passwd: String) {
        var bool1 = false
        var bool2 = false

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            emailEditText.text.toString(),
            passwordEditTextR.text.toString()

        ).addOnCompleteListener {
            if (it.isSuccessful) {
                bool2 = true
            } else {
                showAlert(
                    getString(R.string.err1), getString(R.string.err2) +
                            getString(R.string.err3) +
                            getString(R.string.err4)
                )
            }
        }

        db.collection("users").document(username).set(
            hashMapOf(
                "email" to email,
                "username" to username,
                "password" to passwd,
                "points" to 0,
                "date" to Timestamp.from(
                    Instant.now()
                )
            )
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                bool1 = true
            } else {
                showAlert("Error!", getString(R.string.errorhora))
            }
        }

        val secret = changedataTxt5.text.length
        var xWord = ""
        for (i in 0 until secret) {
            xWord += "X"
        }
        newSecretWord()
        if (bool1 && bool2) {
            showAuth()


        }
    }

    private fun extractUsernameFromEmail(email: String): String {
        val parts: List<String> = email.split('@')
        return parts[0]
    }


    private fun newSecretWord() {
        changedataTxt5.text = ""
        db.collection("words").document("arrWords").get().addOnSuccessListener { document ->
            if (document != null) {
                changedataTxt5.text = randomWord(document.get("arrayW").toString())
                createNewRound()
            }
        }
    }

    private fun createNewRound() {
        val secret = changedataTxt5.text.length
        var xWord = ""
        for (i in 0 until secret) {
            xWord += "x"
        }
        db.collection("joc").document(username).set(
            hashMapOf(
                "maxErrors" to 6,
                "numErrors" to 0,
                "secretWord" to changedataTxt5.text.toString(),
                "secretWordL" to changedataTxt5.text.length,
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
        val rt = (0 until 10).random()
        return arrWords[0][rt]
    }


}