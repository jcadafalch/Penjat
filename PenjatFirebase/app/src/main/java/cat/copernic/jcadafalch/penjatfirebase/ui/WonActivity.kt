package cat.copernic.jcadafalch.penjatfirebase.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import cat.copernic.jcadafalch.penjatfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_over.*
import kotlinx.android.synthetic.main.activity_won.*
import kotlinx.android.synthetic.main.activity_won.logOutButton
import kotlinx.android.synthetic.main.activity_won.novaPartidaButton
import java.io.OutputStream
import android.os.Environment
import android.text.format.DateFormat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.jar.Manifest


@SuppressLint("StaticFieldLeak")
private val db = Firebase.firestore

class WonActivity : AppCompatActivity() {
    private lateinit var username: String
    private lateinit var secretWord: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_won)
        val bundle = intent.extras
        username = bundle?.getString("username").toString()
        secretWord = bundle?.getString("secretWord").toString()
        setup()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
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
            R.id.share -> {
                /*val file: File? = saveImage()
                if (file != null) share(file)*/
                share()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

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

    private fun showAuth() {
        val authIntent = Intent(this, AuthActivity::class.java).apply { }
        startActivity(authIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun setup() {
        title = ""
        usernameWonText.text = username
        secretWordText2.text = getString(R.string.secreta,secretWord)
        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
            showAuth()
        }

        novaPartidaButton.setOnClickListener {
            showHome(username)
        }
    }


    private fun share() {
        val string1: String = getString(R.string.msgTitol)+"\n"
        val string2: String = getString(R.string.msgSecreta, secretWord)+"\n"
        val string3: String = getString(R.string.msgUsername, username)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, string1 + string2 + string3)
        startActivity(Intent.createChooser(intent, getString(R.string.compart)))
    }

//TODO TOP--------------------------------


    /*private fun share(file: File) {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "$packageName.provider", file)
        } else {
            Uri.fromFile(file)
        }
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Screenshot")
        intent.putExtra(Intent.EXTRA_TEXT, "Hello Word")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share using"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            b.performClick()
        } else Toast.makeText(this, "Permis denegat", Toast.LENGTH_SHORT).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun saveImage(): File? {
        if (CheckPermission()) return null
        try {
            val path = Environment.getExternalStorageDirectory().toString() + "/AppName"
            val fileDir: File = File(path)
            if (!fileDir.exists()) fileDir.mkdir()
            val mPath = path + "/ScreenShot " + Date().time + ".png"
            val bitmap = screenshot()
            val file: File = File(mPath)
            val fOut: FileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            Toast.makeText(this, "Imatge guardada correctament", Toast.LENGTH_SHORT).show()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    private fun screenshot(): Bitmap {
        val v = findViewById<View>(R.id.rootView)
        val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        v.draw(canvas)
        return bitmap
    }
    private fun CheckPermission(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
            return false
        }
        return true
    }*/
*/
//TODO BOTTOM-------------------------

    private fun showHome(username: String) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("username", username)
        }
        newSecretWord()
        Handler().postDelayed(
            {
                startActivity(homeIntent)
            },
            500
        )
    }

    private fun newSecretWord() {
        changedataTxt3.text = ""
        db.collection("words").document("arrWords").get().addOnSuccessListener { document ->
            if (document != null) {
                changedataTxt3.text = randomWord(document.get("arrayW").toString())
                createNewRound()
            }
        }
    }

    private fun createNewRound() {
        val secret = changedataTxt3.text.length
        var xWord = ""
        for (i in 0 until secret) {
            xWord += "x"
        }
        db.collection("joc").document(username).set(
            hashMapOf(
                "maxErrors" to 6,
                "numErrors" to 0,
                "secretWord" to changedataTxt3.text.toString(),
                "secretWordL" to changedataTxt3.text.length,
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