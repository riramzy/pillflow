package com.riramzy.pillfllow.utils

data class Country(
    val code: String,
    val name: String,
    val dialCode: String,
    val flag: String,
    val minLength: Int = 8,
    val maxLength: Int = 12
)

fun getCountryFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🌐"
    val chars = countryCode.uppercase()
    val high1 = ((0x1F1E6 + (chars[0] - 'A') - 0x10000) shr 10) + 0xD800
    val low1 = ((0x1F1E6 + (chars[0] - 'A') - 0x10000) and 0x3FF) + 0xDC00
    val high2 = ((0x1F1E6 + (chars[1] - 'A') - 0x10000) shr 10) + 0xD800
    val low2 = ((0x1F1E6 + (chars[1] - 'A') - 0x10000) and 0x3FF) + 0xDC00
    return "${high1.toChar()}${low1.toChar()}${high2.toChar()}${low2.toChar()}"
}

fun createCountry(
    code: String,
    name: String,
    dialCode: String,
    minLength: Int = 8,
    maxLength: Int = 11
) = Country(code, name, dialCode, getCountryFlagEmoji(code), minLength, maxLength)

val allCountries = listOf(
    createCountry("AF", "Afghanistan", "+93", 9, 9),
    createCountry("AL", "Albania", "+355", 9, 9),
    createCountry("DZ", "Algeria", "+213", 9, 9),
    createCountry("AR", "Argentina", "+54", 10, 11),
    createCountry("AU", "Australia", "+61", 9, 9),
    createCountry("AT", "Austria", "+43", 10, 11),
    createCountry("BH", "Bahrain", "+973", 8, 8),
    createCountry("BD", "Bangladesh", "+880", 10, 10),
    createCountry("BE", "Belgium", "+32", 9, 9),
    createCountry("BR", "Brazil", "+55", 10, 11),
    createCountry("CA", "Canada", "+1", 10, 10),
    createCountry("CN", "China", "+86", 11, 11),
    createCountry("CO", "Colombia", "+57", 10, 10),
    createCountry("DK", "Denmark", "+45", 8, 8),
    createCountry("EG", "Egypt", "+20", 10, 11),
    createCountry("FR", "France", "+33", 9, 9),
    createCountry("DE", "Germany", "+49", 10, 11),
    createCountry("GR", "Greece", "+30", 10, 10),
    createCountry("IN", "India", "+91", 10, 10),
    createCountry("ID", "Indonesia", "+62", 9, 12),
    createCountry("IQ", "Iraq", "+964", 10, 10),
    createCountry("IE", "Ireland", "+353", 9, 9),
    createCountry("IT", "Italy", "+39", 9, 10),
    createCountry("JP", "Japan", "+81", 10, 10),
    createCountry("JO", "Jordan", "+962", 9, 9),
    createCountry("KW", "Kuwait", "+965", 8, 8),
    createCountry("LB", "Lebanon", "+961", 7, 8),
    createCountry("LY", "Libya", "+218", 9, 9),
    createCountry("MY", "Malaysia", "+60", 9, 10),
    createCountry("MX", "Mexico", "+52", 10, 10),
    createCountry("MA", "Morocco", "+212", 9, 9),
    createCountry("NL", "Netherlands", "+31", 9, 9),
    createCountry("NZ", "New Zealand", "+64", 8, 10),
    createCountry("NG", "Nigeria", "+234", 10, 10),
    createCountry("NO", "Norway", "+47", 8, 8),
    createCountry("OM", "Oman", "+968", 8, 8),
    createCountry("PK", "Pakistan", "+92", 10, 10),
    createCountry("PS", "Palestine", "+970", 9, 9),
    createCountry("PH", "Philippines", "+63", 10, 10),
    createCountry("PL", "Poland", "+48", 9, 9),
    createCountry("PT", "Portugal", "+351", 9, 9),
    createCountry("QA", "Qatar", "+974", 8, 8),
    createCountry("RU", "Russia", "+7", 10, 10),
    createCountry("SA", "Saudi Arabia", "+966", 9, 9),
    createCountry("SG", "Singapore", "+65", 8, 8),
    createCountry("ZA", "South Africa", "+27", 9, 9),
    createCountry("KR", "South Korea", "+82", 9, 11),
    createCountry("ES", "Spain", "+34", 9, 9),
    createCountry("SD", "Sudan", "+249", 9, 9),
    createCountry("SE", "Sweden", "+46", 9, 9),
    createCountry("CH", "Switzerland", "+41", 9, 9),
    createCountry("SY", "Syria", "+963", 9, 9),
    createCountry("TN", "Tunisia", "+216", 8, 8),
    createCountry("TR", "Turkey", "+90", 10, 10),
    createCountry("AE", "United Arab Emirates", "+971", 9, 9),
    createCountry("GB", "United Kingdom", "+44", 10, 10),
    createCountry("US", "United States", "+1", 10, 10),
    createCountry("YE", "Yemen", "+967", 9, 9)
)