package soulheart.compass.app.model

import soulheart.compass.app.R
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class SensorAccuracy(@StringRes val textResourceId: Int, @DrawableRes val iconResourceId: Int) {
    NO_CONTACT(R.string.sensor_accuracy_no_contact, R.drawable.ic_sensor_no_contact),
    UNRELIABLE(R.string.sensor_accuracy_unreliable, R.drawable.ic_sensor_unreliable),
    LOW(R.string.sensor_accuracy_low, R.drawable.ic_sensor_low),
    MEDIUM(R.string.sensor_accuracy_medium, R.drawable.ic_sensor_medium),
    HIGH(R.string.sensor_accuracy_high, R.drawable.ic_sensor_high)
}
