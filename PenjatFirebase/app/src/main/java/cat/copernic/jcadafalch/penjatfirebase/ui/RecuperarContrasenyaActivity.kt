package cat.copernic.jcadafalch.penjatfirebase.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cat.copernic.jcadafalch.penjatfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_recuperar_contrasenya.*
import java.util.regex.Pattern

class RecuperarContrasenyaActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_contrasenya)
        supportActionBar?.hide()
        btnContinuar.setOnClickListener {
            if (dataValid(
                    passwordEditTextC.text.toString(),
                    passwordEditTextRepeatedC.text.toString(),
                    emailEditTextC.text.toString()
                )
            ) {
                val psswd = passwordEditTextC.text.toString()
                val email = emailEditTextC.text.toString()
                changePassword(email,extractUsernameFromEmail(email), psswd)
            }
        }
    }

    private fun dataValid(psswd1: String, psswd2: String, email: String): Boolean {
        var bool = true
        var errorMessage = ""

        if (!checkEmailFormat(email)){
            errorMessage+=getString(R.string.format)
            bool = false
        }

        if (psswd1.isEmpty()) {
            errorMessage += getString(R.string.intro)
            bool = false
        }
        if (psswd2.isEmpty()) {
            errorMessage += getString(R.string.repe)
            bool = false
        }


        if (psswd1.isNotEmpty() && psswd2.isNotEmpty()) {
            if (psswd1 != psswd2) {
                errorMessage += getString(R.string.coinci)
                bool = false
            }
        }

        if (errorMessage != "") {
            showAlert(errorMessage)
        }

        return bool
    }

    private fun changePassword(email: String, username: String, psswd: String) {
        var bool = false
        db.collection("users").get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.id == username) {
                    bool = true
                    val password = document.get("password").toString()
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        email,
                        password,
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val currentUser = auth.currentUser
                            currentUser?.updatePassword(psswd)?.addOnSuccessListener {
                                db.collection("users").document(username).update(
                                    hashMapOf(
                                        "password" to psswd
                                    ) as Map<String, Any>
                                )
                                auth.signOut()
                                Toast.makeText(
                                    this,
                                    getString(R.string.canvi),
                                    Toast.LENGTH_SHORT
                                ).show()
                                showAuth()
                                finish()
                            }
                        } else {
                            showAlert(getString(R.string.errorinici))
                        }
                    }
                }
            }
            if (!bool) {
                showAlert(getString(R.string.regist))
            }
        }

    }
    private fun checkEmailFormat(email: String): Boolean{
        val email_address_pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return email_address_pattern.matcher(email).matches()
    }

    private fun extractUsernameFromEmail(email: String): String {
        val parts: List<String> = email.split('@')
        return parts[0]
    }

    private fun showAlert(msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR")
        builder.setMessage(msg)
        builder.setPositiveButton("Acceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showAuth() {
        val authIntent = Intent(this, AuthActivity::class.java).apply { }
        startActivity(authIntent)
    }
}