package com.example.choo_chooapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.choo_chooapplication.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scope = CoroutineScope(Dispatchers.Main)
        // Saving data on user's phone
        val sharedPref = getSharedPreferences("personInfo", Context.MODE_PRIVATE)
//        val editor = sharedPref.edit()
//        editor.apply {
//            putString("playerCode", null)
//            apply()
//        }
        var playerCode = sharedPref.getString("playerCode",null) as String
        var playerName = sharedPref.getString("playerName",null) as String
        var teamCode = sharedPref.getString("teamCode",null) as String
        if (playerCode!=null) {
            showApp(scope,playerCode, playerName, teamCode)
            Log.d("Testing", "Player code '$playerCode' found. Entering app.")
        }
        else {
            showLogin(scope)
            Log.d("Testing", "No Player code found. Showing login screen.")
        }

    }

    private fun showLogin(scope: CoroutineScope) {
        setContentView(R.layout.fragment_login)
        val etPlayerCode = findViewById<EditText>(R.id.etPlayerCode)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            var playerCode = etPlayerCode.text.toString()
            updatePlayerInfo(scope, playerCode)
        }
    }

    private fun showApp(scope: CoroutineScope, playerCode: String, playerName: String, teamCode: String) {
        scope.launch {
            // update player info and get/update team info. maybe make a nice and separate update function for this that runs every so often on a coroutine
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_overview, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val navHeaderView = navView.getHeaderView(0)

        // Personalize navbar header with team and player name
        val tvPlayerNameNav = navHeaderView.findViewById<TextView>(R.id.tvPlayerName)
        tvPlayerNameNav.text = playerName
        val tvTeamNameNav = navHeaderView.findViewById<TextView>(R.id.tvTeamName)
        tvTeamNameNav.text = "Team $teamCode"
    }

    private fun updatePlayerInfo(scope: CoroutineScope, playerCode: String) {
        Log.d("Testing", "Function getPlayerInfo called.")
        var scriptUrl = "https://script.google.com/macros/s/AKfycbzDS69K05EXE5mKKltCN8r5ra2iQmQ_vNnjssSA1F6CJioDELu3J3NMSBRDUjwMNsJi/exec"
        scope.launch {
            Log.d("Testing", "Scope launched.")
            val client = HttpClient(CIO)
            Log.d("Testing", "HttpClient created.")
            val response: HttpResponse = client.get(scriptUrl) {
                parameter("action", "getPlayerInfo")
                parameter("playerCode", playerCode)
            }
            Log.d("Testing", "HttpRequest sent.")
            var responseBody = response.body() as String;
            Log.d("DATABASE", responseBody)

            Log.d("Testing", "Response logged.")
            var toastMsg = "";
            when(responseBody) {
                "alreadyLoggedIn" -> {toastMsg = "This player has already logged in."}
                "playerNotFound" -> {toastMsg = "This player does not exist."}
                else -> {
                    val playerInfo = responseBody.split(",")
                    val playerName = playerInfo[0]
                    val teamCode = playerInfo[1]

                    val sharedPref = getSharedPreferences("personInfo", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.apply{
                        putString("playerCode", playerCode)
                        putString("playerName", playerName)
                        putString("teamCode", teamCode)
                        apply()
                    }
                    showApp(scope, playerCode, playerName, teamCode)
                }
            }
            if(toastMsg!=""){Toast.makeText(this@MainActivity, toastMsg, Toast.LENGTH_SHORT).show()}
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun startUpdateLoop(sharedPref: SharedPreferences, scope: CoroutineScope, playerCode: String, teamCode: String)
    {
        Log.d("updateLoop", "startUpdateLoop called.")
        val client = HttpClient(CIO)
        Log.d("updateLoop", "HttpClient created.")
        var scriptUrl = "https://script.google.com/macros/s/AKfycbzDS69K05EXE5mKKltCN8r5ra2iQmQ_vNnjssSA1F6CJioDELu3J3NMSBRDUjwMNsJi/exec"
        scope.launch {
            val response: HttpResponse = client.get(scriptUrl) {
                parameter("action", "getPlayerInfo")
                parameter("playerCode", playerCode)
            }
            Log.d("updateLoop", "HttpRequest sent.")
            var responseBody = response.body() as String;
            Log.d("DATABASE", responseBody)
            if (responseBody == "playerNotFound")
            {
                val editor = sharedPref.edit()
                editor.apply {
                    putString("playerCode", null)
                    apply()
                }
                showLogin(scope)
            }
            Log.d("updateLoop", "Response logged.")
        }
    }

    private fun saveNewPlayer(name: Any, birthday: Any, country: Any) {
        Log.d("Testing", "Function saveNewPlayer called.")
        var scriptUrl = "https://script.google.com/macros/s/AKfycbzDS69K05EXE5mKKltCN8r5ra2iQmQ_vNnjssSA1F6CJioDELu3J3NMSBRDUjwMNsJi/exec"
        val scope = CoroutineScope(Dispatchers.Main)
        Log.d("Testing", "Scope created.")
        scope.launch {
            Log.d("Testing", "Scope launched.")
            val client = HttpClient(CIO)
            Log.d("Testing", "HttpClient created.")
            val response: HttpResponse = client.get(scriptUrl) {
                parameter("action", "createPlayer")
                parameter("name", name)
                parameter("birthday", birthday)
                parameter("country", country)
            }
            Log.d("Testing", "HttpRequest sent.")

            Log.d("DATABASE", response.body())

            Log.d("Testing", "Response logged.")
        }

    }
}