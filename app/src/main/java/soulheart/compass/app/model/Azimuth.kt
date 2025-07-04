package soulheart.compass.app.model

class Azimuth(_degrees: Float) {

    operator fun plus(degrees: Float) = Azimuth(this.degrees + degrees)

    val degrees = normalizeAngle(_degrees)

    val cardinalDirection: CardinalDirection = when (degrees) {
        in 22.5f until 67.5f -> CardinalDirection.NORTHEAST
        in 67.5f until 112.5f -> CardinalDirection.EAST
        in 112.5f until 157.5f -> CardinalDirection.SOUTHEAST
        in 157.5f until 202.5f -> CardinalDirection.SOUTH
        in 202.5f until 247.5f -> CardinalDirection.SOUTHWEST
        in 247.5f until 292.5f -> CardinalDirection.WEST
        in 292.5f until 337.5f -> CardinalDirection.NORTHWEST
        else -> CardinalDirection.NORTH
    }

    private fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        if (degrees != other.degrees) return false

        return true
    }

    override fun hashCode(): Int {
        return degrees.hashCode()
    }

    override fun toString(): String {
        return "(degrees=$degrees)"
    }
}

private data class SemiClosedFloatRange(val fromInclusive: Float, val toExclusive: Float)

private operator fun SemiClosedFloatRange.contains(value: Float) = fromInclusive <= value && value < toExclusive
private infix fun Float.until(to: Float) = SemiClosedFloatRange(this, to)
