package models

object Utils {
  def round(n: Float): Double = {
    BigDecimal(n).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def round(n: Double): Double = {
    BigDecimal(n).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}
