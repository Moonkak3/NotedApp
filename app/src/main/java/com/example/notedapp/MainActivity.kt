package com.example.notedapp

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.notedapp.databinding.ActivityMainBinding
import com.example.notedapp.login.SplashScreenActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var fabButtonClick: FabButtonClick
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        viewModel.firstTimeSetup()

        val hView = binding.navView.getHeaderView(0)
        hView.findViewById<TextView>(R.id.name).text = viewModel.getCurrUserName()
        hView.findViewById<TextView>(R.id.email).text =
            viewModel.getCurrUserEmail(formatted = false)

        binding.appBarMain.fab.setOnClickListener { view ->
            fabButtonClick.onFabClicked()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_tasks, R.id.nav_diary, R.id.nav_tags
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.menu.findItem(R.id.sign_out).setOnMenuItemClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle("Are you sure you want to Sign-out?")
            builder.setPositiveButton("Sign-out") { dialog, which ->
                logout()
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, which ->

            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(
                    applicationContext,
                    "there are no things to set :P",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            R.id.action_contact_developer -> {
                intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:")
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("yuebin0303@gmail.com"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "Noted App - Contact Developer")
                intent.putExtra(Intent.EXTRA_TEXT, "yo what is up my dude")
                startActivity(intent)
                Toast.makeText(applicationContext, "Drafting email...", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_color_mode -> {
                val sp = getSharedPreferences("settings", MODE_PRIVATE)
                when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {

                    Configuration.UI_MODE_NIGHT_YES -> {
                        val myEdit = sp.edit()
                        myEdit.putBoolean("darkMode", false)
                        myEdit.apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        Toast.makeText(
                            applicationContext,
                            "Switched to light-mode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Configuration.UI_MODE_NIGHT_NO -> {
                        val myEdit = sp.edit()
                        myEdit.putBoolean("darkMode", true)
                        myEdit.apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        Toast.makeText(
                            applicationContext,
                            "Switched to dark-mode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return true
            }
            else -> {}
        }

        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setListener(fragmentIF: FabButtonClick) {
        fabButtonClick = fragmentIF
    }

    private fun logout() {
        Firebase.auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
        startActivity(Intent(this, SplashScreenActivity::class.java))
    }
}

interface FabButtonClick {
    fun onFabClicked()
}