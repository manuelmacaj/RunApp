package com.manuelmacaj.bottomnavigation.View

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.manuelmacaj.bottomnavigation.R
import com.manuelmacaj.bottomnavigation.View.accountPackage.EditPasswordActivity
import com.manuelmacaj.bottomnavigation.View.accountPackage.EditProfileActivity
import com.manuelmacaj.bottomnavigation.View.accountPackage.PersonalAccountFragment
import com.manuelmacaj.bottomnavigation.View.activitiesPackage.ActivitiesFragment
import com.manuelmacaj.bottomnavigation.View.loginPackage.LoginActivity
import com.manuelmacaj.bottomnavigation.View.runpackage.RunFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val mFirebaseAuth =
        FirebaseAuth.getInstance() //istanza firebase, sezione autenticazione

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val activitiesFragment = ActivitiesFragment()
        val runFragment = RunFragment()
        val accountFragment = PersonalAccountFragment()

        setCurrentFragment(activitiesFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.activities -> setCurrentFragment(activitiesFragment)
                R.id.run -> setCurrentFragment(runFragment)
                R.id.account -> setCurrentFragment(accountFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //facciamo l'inflate della risorsa di menu nel Menu fornito nella callback
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    //Metodo chiamato ogni volta che viene selezionato un elemento dall'option menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.account -> {
                Toast.makeText(this, getString(R.string.edit_your_account), Toast.LENGTH_SHORT).show()
                openEditProfileActivity()
            }
            R.id.password -> {
                Toast.makeText(this, getString(R.string.edit_your_password), Toast.LENGTH_SHORT).show()
                openEditPasswordActivity()
            }
            R.id.logout -> {
                Toast.makeText(this, getString(R.string.signout_app), Toast.LENGTH_SHORT).show()
                signOut()
            }
        }
        return true
    }

    private fun openEditProfileActivity() {
        val intent = Intent(this@MainActivity, EditProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun openEditPasswordActivity() {
        val intent = Intent(this@MainActivity, EditPasswordActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun signOut() {
        mFirebaseAuth.signOut()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}