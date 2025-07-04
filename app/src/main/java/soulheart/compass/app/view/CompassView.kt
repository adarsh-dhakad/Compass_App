package soulheart.compass.app.view

import soulheart.compass.app.R
import soulheart.compass.app.databinding.CompassViewBinding
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import androidx.annotation.AnyRes
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import soulheart.compass.app.model.Azimuth
import kotlin.math.roundToInt

class CompassView(context: Context, attributes: AttributeSet) : ConstraintLayout(context, attributes) {

    @IdRes
    private val center = R.id.compass_rose_image

    private var binding: CompassViewBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = CompassViewBinding.inflate(layoutInflater, this, true)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        visibility = INVISIBLE
        updateStatusDegreesTextSize(width * getFloat(R.dimen.status_degrees_text_size_factor))
        updateStatusCardinalDirectionTextSize(width * getFloat(R.dimen.status_cardinal_direction_text_size_factor))
        updateCardinalDirectionTextSize(width * getFloat(R.dimen.cardinal_direction_text_size_factor))
        updateDegreeTextSize(width * getFloat(R.dimen.degree_text_size_factor))
    }

    private fun updateStatusDegreesTextSize(textSize: Float) {
        binding.statusDegreesText.setTextSize(COMPLEX_UNIT_PX, textSize)
    }

    private fun updateStatusCardinalDirectionTextSize(textSize: Float) {
        binding.statusCardinalDirectionText.setTextSize(COMPLEX_UNIT_PX, textSize)
    }

    private fun updateCardinalDirectionTextSize(textSize: Float) {
        binding.cardinalDirectionNorthText.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.cardinalDirectionEastText.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.cardinalDirectionSouthText.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.cardinalDirectionWestText.setTextSize(COMPLEX_UNIT_PX, textSize)
    }

    private fun updateDegreeTextSize(textSize: Float) {
        binding.degree0Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree30Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree60Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree90Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree120Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree150Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree180Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree210Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree240Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree270Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree300Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        binding.degree330Text.setTextSize(COMPLEX_UNIT_PX, textSize)
    }

    fun setAzimuth(azimuth: Azimuth) {
        updateStatusDegreesText(azimuth)
        updateStatusDirectionText(azimuth)

        val rotation = azimuth.degrees.unaryMinus()
        rotateCompassRoseImage(rotation)
        rotateCompassRoseTexts(rotation)

        visibility = VISIBLE
    }

    private fun updateStatusDegreesText(azimuth: Azimuth) {
        binding.statusDegreesText.text = context.getString(R.string.degrees, azimuth.degrees.roundToInt())
    }

    private fun updateStatusDirectionText(azimuth: Azimuth) {
        binding.statusCardinalDirectionText.text = context.getString(azimuth.cardinalDirection.labelResourceId)
    }

    private fun rotateCompassRoseImage(rotation: Float) {
        binding.compassRoseImage.rotation = rotation
    }

    private fun rotateCompassRoseTexts(rotation: Float) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        rotateCardinalDirectionTexts(constraintSet, rotation)
        rotateDegreeTexts(constraintSet, rotation)

        constraintSet.applyTo(this)
    }

    private fun rotateCardinalDirectionTexts(constraintSet: ConstraintSet, rotation: Float) {
        val radius = calculateTextRadius(getFloat(R.dimen.cardinal_direction_text_ratio))

        constraintSet.constrainCircle(R.id.cardinal_direction_north_text, center, radius, rotation)
        constraintSet.constrainCircle(R.id.cardinal_direction_east_text, center, radius, rotation + 90)
        constraintSet.constrainCircle(R.id.cardinal_direction_south_text, center, radius, rotation + 180)
        constraintSet.constrainCircle(R.id.cardinal_direction_west_text, center, radius, rotation + 270)
    }

    private fun rotateDegreeTexts(constraintSet: ConstraintSet, rotation: Float) {
        val radius = calculateTextRadius(getFloat(R.dimen.degree_text_ratio))

        constraintSet.constrainCircle(R.id.degree_0_text, center, radius, rotation)
        constraintSet.constrainCircle(R.id.degree_30_text, center, radius, rotation + 30)
        constraintSet.constrainCircle(R.id.degree_60_text, center, radius, rotation + 60)
        constraintSet.constrainCircle(R.id.degree_90_text, center, radius, rotation + 90)
        constraintSet.constrainCircle(R.id.degree_120_text, center, radius, rotation + 120)
        constraintSet.constrainCircle(R.id.degree_150_text, center, radius, rotation + 150)
        constraintSet.constrainCircle(R.id.degree_180_text, center, radius, rotation + 180)
        constraintSet.constrainCircle(R.id.degree_210_text, center, radius, rotation + 210)
        constraintSet.constrainCircle(R.id.degree_240_text, center, radius, rotation + 240)
        constraintSet.constrainCircle(R.id.degree_270_text, center, radius, rotation + 270)
        constraintSet.constrainCircle(R.id.degree_300_text, center, radius, rotation + 300)
        constraintSet.constrainCircle(R.id.degree_330_text, center, radius, rotation + 330)
    }

    private fun getFloat(@AnyRes id: Int): Float {
        val tempValue = TypedValue()
        resources.getValue(id, tempValue, true)
        return tempValue.float
    }

    private fun calculateTextRadius(ratio: Float): Int {
        return width / 2 - (width * ratio).toInt()
    }
}
