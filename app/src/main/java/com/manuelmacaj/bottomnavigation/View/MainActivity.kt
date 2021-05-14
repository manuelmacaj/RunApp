package com.manuelmacaj.bottomnavigation.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.manuelmacaj.bottomnavigation.R
import com.manuelmacaj.bottomnavigation.View.accountPackage.PersonalAccountFragment
import com.manuelmacaj.bottomnavigation.View.activitiesPackage.ActivitiesFragment
import com.manuelmacaj.bottomnavigation.View.runpackage.RunFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
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

}