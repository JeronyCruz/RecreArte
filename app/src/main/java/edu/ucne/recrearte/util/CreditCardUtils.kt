package edu.ucne.recrearte.util


fun String.isValidCreditCardNumber(): Boolean {
    val cleanNumber = this.replace("\\s+".toRegex(), "")

    if (!cleanNumber.matches("^[0-9]{13,16}$".toRegex())) {
        return false
    }

    var sum = 0
    var alternate = false
    for (i in cleanNumber.length - 1 downTo 0) {
        var digit = cleanNumber[i].digitToIntOrNull() ?: return false

        if (alternate) {
            digit *= 2
            if (digit > 9) {
                digit = (digit % 10) + 1
            }
        }

        sum += digit
        alternate = !alternate
    }

    return sum % 10 == 0
}