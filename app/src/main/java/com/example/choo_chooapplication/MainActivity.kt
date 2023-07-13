package com.example.choo_chooapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var navView: NavigationView
    private var isTeamLeader = false
    private lateinit var sharedPref: SharedPreferences;
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("personInfo", Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        // Saving data on user's phone
        editor.apply {
//            putString("playerCode", null)
            apply()
        }
        val playerCode = sharedPref.getString("playerCode",null)
        val playerName = sharedPref.getString("playerName","Loading...")
        val teamCode = sharedPref.getString("teamCode","")
        if (playerCode!=null && playerName!=null && teamCode!=null) {
            showApp(playerCode)
            Log.d("Testing", "Player code '$playerCode' found. Entering app.")
        }
        else {
            showLogin()
            Log.d("Testing", "No Player code found. Showing login screen.")
        }
    }

    private fun showLogin() {
        setContentView(R.layout.fragment_login)
        val etPlayerCode = findViewById<EditText>(R.id.etPlayerCode)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val playerCode = etPlayerCode.text.toString()
            updatePlayerInfo(playerCode)
        }
    }

    private fun showApp(playerCode: Any) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView= binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_overview, R.id.nav_gallery, R.id.nav_location_sharing
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                updateLoop(playerCode.toString())
                mainHandler.postDelayed(this, 15000)
            }
        })
        val navHeaderView = navView.getHeaderView(0)
        // Personalize navbar header with team and player name
        val tvPlayerNameNav = navHeaderView.findViewById<TextView>(R.id.tvPlayerName)
        tvPlayerNameNav.text = sharedPref.getString("playerName","Loading...")
        val tvTeamNameNav = navHeaderView.findViewById<TextView>(R.id.tvTeamName)
        tvTeamNameNav.text = sharedPref.getString("teamName", "Team loading...")
    }

    private fun updatePlayerInfo(playerCode: String) {
        Log.d("Testing", "Function getPlayerInfo called.")
        val scriptUrl = "https://script.google.com/macros/s/AKfycbw6ho9pCuGK1F1hflkohW9h0hvGPU2NrJfvo84S08JC7TfAvyAFIRb_FK8lV93ab2ofFQ/exec"
        scope.launch {
            try {
                Log.d("Testing", "Scope launched.")
                val client = HttpClient(CIO)
                Log.d("Testing", "HttpClient created.")
                val response: HttpResponse = client.get(scriptUrl) {
                    parameter("action", "getPlayerInfo")
                    parameter("playerCode", playerCode)
                }
                Log.d("Testing", "HttpRequest sent.")
                val responseBody = response.body() as String
                Log.d("DATABASE", responseBody)

                Log.d("Testing", "Response logged.")
                var toastMsg = "";
                when (responseBody) {
                    "playerNotFound" -> {
                        toastMsg = "This player does not exist."
                    }

                    else -> {
                        val playerInfo = responseBody.split(",")
                        val alreadyLoggedIn = playerInfo[0]
                        if (alreadyLoggedIn == "true") {
                            toastMsg = "This player code has already been used."
                        } else {
                            val playerName = playerInfo[1]
                            val teamCode = playerInfo[2]

                            editor.apply {
                                putString("playerCode", playerCode)
                                putString("playerName", playerName)
                                putString("teamCode", teamCode)
                                apply()
                            }
                            showApp(playerCode)
                        }
                    }
                }
                if (toastMsg != "") {
                    Toast.makeText(this@MainActivity, toastMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Throwable) {
                Log.d("Testing", "HttpRequest failed with error code: ${e.javaClass.name}")
                Toast.makeText(this@MainActivity, "No internet connection", Toast.LENGTH_SHORT).show()
                return@launch
            }
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

    private fun updateLoop(playerCode: String) {
        Log.d("updateLoop", "updateLoop called.")
        val navHeaderView = navView.getHeaderView(0)
        val tvPlayerNameNav = navHeaderView.findViewById<TextView>(R.id.tvPlayerName)
        val tvTeamNameNav = navHeaderView.findViewById<TextView>(R.id.tvTeamName)
        var sharedPref = getSharedPreferences("personInfo", Context.MODE_PRIVATE)
        val client = HttpClient(CIO)
        Log.d("updateLoop", "HttpClient created.")
        var scriptUrl =
            "https://script.google.com/macros/s/AKfycbw6ho9pCuGK1F1hflkohW9h0hvGPU2NrJfvo84S08JC7TfAvyAFIRb_FK8lV93ab2ofFQ/exec"
        scope.launch {
            try {
                var responsePlayer: HttpResponse = client.get(scriptUrl) {
                    parameter("action", "getPlayerInfo")
                    parameter("playerCode", playerCode)
                }
                Log.d("updateLoop", "HttpRequest sent.")
                var responsePlayerBody = responsePlayer.body() as String;
                Log.d("DATABASE", responsePlayerBody)
                if (responsePlayerBody == "playerNotFound") {
                    val editor = sharedPref.edit()
                    editor.apply {
                        putString("playerCode", null)
                        apply()
                    }
                    showLogin()
                } else {
                    // Update player info
                    var playerInfo = responsePlayerBody.split(",")
                    var playerName = playerInfo[1];
                    var teamCode = playerInfo[2];
                    editor.apply {
                        putString("playerName", playerName)
                        putString("teamCode", teamCode)
                        apply()
                    }
                    tvPlayerNameNav.text =
                        if (!isTeamLeader) playerName else "$playerName \uD83D\uDC51"

                    // Update team info

                    var responseTeam = client.get(scriptUrl) {
                        parameter("action", "getTeamInfo")
                        parameter("teamCode", teamCode)
                    }
                    var responseTeamBody = responseTeam.body() as String
                    var teamInfo = responseTeamBody.split(",")
                    var teamName = teamInfo[0]
                    var teamMembers = teamInfo[1]
                    var teamLeader = teamInfo[2]
                    var curCoins = teamInfo[3]

                    editor.apply {
                        putString("teamName", teamName)
                        putString("teamMembers", teamMembers)
                        putString("teamLeader", teamLeader)
                        putString("curCoins", curCoins)
                        apply()
                    }

                    if (tvTeamNameNav.text != teamName) {
                        tvTeamNameNav.text = "$teamName"
                    }
                    isTeamLeader = playerCode == teamLeader
                    tvPlayerNameNav.text =
                        if (!isTeamLeader) playerName else "$playerName \uD83D\uDC51"
                    Log.d("Testing", "$isTeamLeader Player: $playerCode | Team leader: $teamLeader")

                }
            } catch (e: Exception) {
                Log.d("Testing", "HttpRequest failed with error code: ${e.javaClass.name}")
                Toast.makeText(this@MainActivity, "No internet connection", Toast.LENGTH_SHORT).show()
                return@launch
            }
        }
    }

    private fun saveNewPlayer(name: Any, birthday: Any, country: Any) {
        Log.d("Testing", "Function saveNewPlayer called.")
        var scriptUrl = "https://script.google.com/macros/s/AKfycbw6ho9pCuGK1F1hflkohW9h0hvGPU2NrJfvo84S08JC7TfAvyAFIRb_FK8lV93ab2ofFQ/exec"
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