package com.example.realmpractise.ui.mainActivity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realmpractise.db.UserModule
import com.example.realmpractise.util.NavigatorImpl
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    private var navigator = NavigatorImpl(this)
    private var userModule: UserModule = UserModule()
    val realm: Realm = Realm.getDefaultInstance()
    private var adapter = Adapter(this)
    private val permission = 100
    private var speech: SpeechRecognizer? = null
    private lateinit var recognizerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        setUpRV()
        refresh_btn.setOnClickListener {
            setUpRV()
        }
        add_btn.setOnClickListener {
            navigator.openUserActivity()
        }
        mic_btn.setOnClickListener {
            getSpeechInput()
        }
        search_btn.setOnClickListener {
            searchUser()
        }
    }

    private fun getSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "RU-ru")
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 10)
        } else {
            Toast.makeText(
                this,
                "Your Device Doesn't Support Speech Input",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun searchUser() {
        try {
            val userName = searchBar.text.toString()
            val getUser = userModule.getUser(this.realm, userName)
            adapter.getAllUsers(getUser)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "User found", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpRV() {
        val userList = userModule.getAllUsers(realm)
        adapter.getAllUsers(userList)
    }


    /* private fun swipeToDelete(){
         val swipeHandler = object : SwipeToDeleteCallback(this) {
             override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 val adapter = recyclerView.adapter as Adapter
                 adapter.deleteUser(viewHolder.adapterPosition)
                 userModule.deleteUser(realm, viewHolder.adapterPosition)
             }
         }
         val itemTouchHelper = ItemTouchHelper(swipeHandler)
         itemTouchHelper.attachToRecyclerView(recyclerView)
     }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permission -> if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {
                speech?.startListening(recognizerIntent)
            } else {
                PackageManager.PERMISSION_DENIED
                Toast.makeText(
                    this@MainActivity, "Permission Denied!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                searchBar.setText(result[0])
                searchUser()
            }
        } else {
            setUpRV()
        }
    }
}