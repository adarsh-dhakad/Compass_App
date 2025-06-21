package soulheart.compass.app

import soulheart.compass.app.databinding.AboutAlertDialogViewBinding
import soulheart.compass.app.databinding.ActivityMainBinding
import soulheart.compass.app.databinding.SensorAlertDialogViewBinding
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.*
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.databinding.DataBindingUtil
import soulheart.compass.app.model.Azimuth
import soulheart.compass.app.model.DisplayRotation
import soulheart.compass.app.model.RotationVector
import soulheart.compass.app.model.SensorAccuracy
import soulheart.compass.app.util.MathUtils
import soulheart.compass.app.view.ObservableSensorAccuracy
import android.os.Handler
import android.os.Looper
//import com.google.android.gms.ads.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.TimeUnit
import kotlin.math.pow


const val OPTION_INSTRUMENTED_TEST = "INSTRUMENTED_TEST"

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val observableSensorAccuracy = ObservableSensorAccuracy(SensorAccuracy.NO_CONTACT)
//    lateinit var mAdView: AdView
//    lateinit var adRequest:AdRequest
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager

    private var optionsMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
//        val conf = RequestConfiguration.Builder()
//            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE).build()
//        MobileAds.setRequestConfiguration(conf)
//        MobileAds.initialize(this) {}
//        mAdView = binding.contentMain.adView
//        bannerAds()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()

        if (isInstrumentedTest()) {
            Log.i(TAG, "Skipping registration of sensor listener")
        } else {
            registerSensorListener()
        }

        Log.i(TAG, "Started compass")
    }

    private fun registerSensorListener() {
        val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            Log.w(TAG, "Rotation vector sensor not available")
            showSensorErrorDialog()
            return
        }

        val success = sensorManager.registerListener(this, rotationVectorSensor, SENSOR_DELAY_FASTEST)
        if (!success) {
            Log.w(TAG, "Could not enable rotation vector sensor")
            showSensorErrorDialog()
            return
        }
    }

    private fun showSensorErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.sensor_error_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .show()
    }

    private fun isInstrumentedTest() = intent.extras?.getBoolean(OPTION_INSTRUMENTED_TEST) ?: false

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.i(TAG, "Stopped compass")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu
        updateSensorStatusIcon()
        updateScreenRotationIcon()
        updateNightModeIcon()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sensor_status -> {
                showSensorStatusPopup()
                true
            }
            R.id.action_screen_rotation -> {
                toggleScreenRotationMode()
                true
            }
            R.id.action_night_mode -> {
                toggleNightMode()
                true
            }
            R.id.action_about -> {
                showAboutPopup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSensorStatusPopup() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        val dialogContextInflater = LayoutInflater.from(alertDialogBuilder.context)

        val dialogBinding = SensorAlertDialogViewBinding.inflate(dialogContextInflater, null, false)
        dialogBinding.sensorAccuracy = observableSensorAccuracy

        alertDialogBuilder
            .setTitle(R.string.sensor_status)
            .setView(dialogBinding.root)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun toggleScreenRotationMode() {
        when (requestedOrientation) {
            SCREEN_ORIENTATION_UNSPECIFIED -> changeScreenRotationMode(SCREEN_ORIENTATION_LOCKED)
            else -> changeScreenRotationMode(SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }

    private fun changeScreenRotationMode(screenOrientation: Int) {
        Log.d(TAG, "Setting requested orientation to value $screenOrientation")
        requestedOrientation = screenOrientation
        updateScreenRotationIcon()
    }

    private fun toggleNightMode() {
        when (getDefaultNightMode()) {
            MODE_NIGHT_NO -> changeNightMode(MODE_NIGHT_YES)
            MODE_NIGHT_YES -> changeNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            else -> changeNightMode(MODE_NIGHT_NO)
        }
    }

    private fun changeNightMode(@NightMode mode: Int) {
        Log.d(TAG, "Setting night mode to value $mode")
        setDefaultNightMode(mode)
        updateNightModeIcon()
    }

    private fun showAboutPopup() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        val dialogContextInflater = LayoutInflater.from(alertDialogBuilder.context)

        val dialogBinding = AboutAlertDialogViewBinding.inflate(dialogContextInflater, null, false)
        try {
            val pInfo: PackageInfo =
                packageManager.getPackageInfo(packageName, 0)
            dialogBinding.version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    //    dialogBinding.version = BuildConfig.VERSION_NAME
        dialogBinding.copyrightText.movementMethod = LinkMovementMethod.getInstance()
        dialogBinding.licenseText.movementMethod = LinkMovementMethod.getInstance()
        dialogBinding.sourceCodeText.movementMethod = LinkMovementMethod.getInstance()

        alertDialogBuilder
            .setTitle(R.string.app_name)
            .setView(dialogBinding.root)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> setSensorAccuracy(accuracy)
            else -> Log.w(TAG, "Unexpected accuracy changed event of type ${sensor.type}")
        }
    }

    private fun setSensorAccuracy(accuracy: Int) {
        Log.v(TAG, "Sensor accuracy value $accuracy")
        val sensorAccuracy = adaptSensorAccuracy(accuracy)
        setSensorAccuracy(sensorAccuracy)
    }

    internal fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) {
        observableSensorAccuracy.set(sensorAccuracy)
        updateSensorStatusIcon()
    }

    private fun adaptSensorAccuracy(accuracy: Int): SensorAccuracy {
        return when (accuracy) {
            SENSOR_STATUS_NO_CONTACT -> SensorAccuracy.NO_CONTACT
            SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.UNRELIABLE
            SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
            SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
            SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
            else -> {
                Log.w(TAG, "Encountered unexpected sensor accuracy value '$accuracy'")
                SensorAccuracy.NO_CONTACT
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> updateCompass(event)
            else -> Log.w(TAG, "Unexpected sensor changed event of type ${event.sensor.type}")
        }
    }

    private fun updateCompass(event: SensorEvent) {
        val rotationVector = RotationVector(event.values[0], event.values[1], event.values[2])
        val displayRotation = getDisplayRotation()
        val azimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)
        setAzimuth(azimuth)
    }

    private fun getDisplayRotation(): DisplayRotation {
        return when (getDisplayCompat()?.rotation) {
            Surface.ROTATION_90 -> DisplayRotation.ROTATION_90
            Surface.ROTATION_180 -> DisplayRotation.ROTATION_180
            Surface.ROTATION_270 -> DisplayRotation.ROTATION_270
            else -> DisplayRotation.ROTATION_0
        }
    }

    private fun getDisplayCompat(): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            windowManager.defaultDisplay
        }
    }

    internal fun setAzimuth(azimuth: Azimuth) {
        binding.contentMain.compass.setAzimuth(azimuth)
        Log.v(TAG, "Azimuth $azimuth")
    }

    private fun updateSensorStatusIcon() {
        val sensorAccuracy = observableSensorAccuracy.get() ?: SensorAccuracy.NO_CONTACT

        optionsMenu
            ?.findItem(R.id.action_sensor_status)
            ?.setIcon(sensorAccuracy.iconResourceId)
    }

    private fun updateScreenRotationIcon() {
        optionsMenu
            ?.findItem(R.id.action_screen_rotation)
            ?.setIcon(getScreenRotationIcon())
    }

    @DrawableRes
    private fun getScreenRotationIcon(): Int {
        return when (requestedOrientation) {
            SCREEN_ORIENTATION_UNSPECIFIED -> R.drawable.ic_screen_rotation
            else -> R.drawable.ic_screen_rotation_lock
        }
    }

    private fun updateNightModeIcon() {
        optionsMenu
            ?.findItem(R.id.action_night_mode)
            ?.setIcon(getNightModeIcon())
    }

    @DrawableRes
    private fun getNightModeIcon(): Int {
        return when (getDefaultNightMode()) {
            MODE_NIGHT_NO -> R.drawable.ic_night_mode_no
            MODE_NIGHT_YES -> R.drawable.ic_night_mode_yes
            else -> R.drawable.ic_night_mode_auto
        }
    }

//    private fun bannerAds() {
//        adRequest = AdRequest.Builder()
//            .build()
//        var retryAttempt = 0
//        mAdView.loadAd(adRequest)
//        mAdView.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                retryAttempt = 0
//            }
//
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)
//                // 2 power 6 = 64
//                retryAttempt++
//                val delayMillis: Long = TimeUnit.SECONDS.toMillis(
//                    2.0.pow(6.coerceAtMost(retryAttempt).toDouble()).toLong()
//                )
//
//
//                Handler(Looper.getMainLooper()).postDelayed(
//                    {
//                        mAdView.loadAd(adRequest)
//                    },
//                    delayMillis
//                )
//            }
//
//            override fun onAdOpened() {
//                //         mAdView.visibility = View.GONE
//            }
//
//            override fun onAdClicked() {
//                //        mAdView.visibility = View.GONE
//            }
//
//            override fun onAdClosed() {
//                //      mAdView.visibility = View.GONE
//            }
//        }
//
//    }
}
