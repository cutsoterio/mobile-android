package zw.co.guava.soterio.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.fragment_home.*
import zw.co.guava.soterio.R
import zw.co.guava.soterio.ui.main.getinfo.exposure.ExposureNotifications
import zw.co.guava.soterio.ui.main.getinfo.hospitals.HospitalsActivity
import zw.co.guava.soterio.ui.main.getinfo.testing.TestingCenters


class HomeFragment : Fragment(R.layout.fragment_home) {
    lateinit var lottieViewSonar: LottieAnimationView;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //lottieViewSonar = findViewById(R.id.sonarAnimationView)
        //lottieViewSonar.playAnimation()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root =  inflater.inflate(R.layout.fragment_home, container, false)
        initRefsAndListeners(root)

        return root
    }

    private fun initRefsAndListeners(root: View) {
        val hospitals = root.findViewById<MaterialCardView>(R.id.hospitalsButton)
        val exposureNotificationsButton = root.findViewById<MaterialCardView>(R.id.exposureNotificationsButton)

        hospitals.setOnClickListener {
            val hospitalIntent = Intent(activity, HospitalsActivity::class.java)
            startActivity(hospitalIntent)
        }

        testingCentresButton.setOnClickListener{
            val testingCentresIntent = Intent(activity, TestingCenters::class.java)
            startActivity(testingCentresIntent)
        }

        exposureNotificationsButton.setOnClickListener {
            val exposureNotificationsIntent = Intent(activity, ExposureNotifications::class.java)
            startActivity(exposureNotificationsIntent)
        }
    }
}