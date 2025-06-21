import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.network.models.Salary

fun Salary?.format(
    getString: (Int, Array<String>) -> String, getCurrencyIcon: (String) -> String
): String {
    return this?.let {
        when {
            it.from != null && it.to != null -> getString(
                R.string.salary_range, arrayOf(
                    it.from.toString(), it.to.toString(), getCurrencyIcon(it.currency.toString())
                )
            )

            it.from != null -> getString(
                R.string.salary_from, arrayOf(
                    it.from.toString(), getCurrencyIcon(it.currency.toString())
                )
            )

            it.to != null -> getString(
                R.string.salary_to, arrayOf(
                    it.to.toString(), getCurrencyIcon(it.currency.toString())
                )
            )

            else -> getString(R.string.salary_not_specified, arrayOf())
        }
    } ?: getString(R.string.salary_not_specified, arrayOf())
}
