package cat.copernic.jcadafalch.penjatfirebase.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.jcadafalch.penjatfirebase.adapter.CustomAdapter
import cat.copernic.jcadafalch.penjatfirebase.R
import cat.copernic.jcadafalch.penjatfirebase.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_ranking.*

class RankingActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<User>
    private lateinit var customAdapter: CustomAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        title = ""

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()

        customAdapter = CustomAdapter(userArrayList)

        recyclerView.adapter = customAdapter

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    eventChangeListener("points")
                } else if (position == 1) {
                    eventChangeListener("date")
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        })

        eventChangeListener("points")
    }

    private fun eventChangeListener(attr: String) {
        userArrayList.clear()
        db.collection("users").orderBy(attr, Query.Direction.DESCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            userArrayList.add(dc.document.toObject(User::class.java))
                        }
                    }
                    customAdapter.notifyDataSetChanged()
                }
            })
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
}