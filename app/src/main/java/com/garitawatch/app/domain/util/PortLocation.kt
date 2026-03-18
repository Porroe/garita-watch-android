package com.garitawatch.app.domain.util

data class PortCoordinates(val latitude: Double, val longitude: Double)

object PortLocation {
    private val coordinates = mapOf(
        "070801" to PortCoordinates(44.3486, -75.9172), // Alexandria Bay
        "300401" to PortCoordinates(49.0020, -122.7355), // Blaine Pacific Hwy
        "300402" to PortCoordinates(49.0020, -122.7555), // Blaine Peace Arch
        "300403" to PortCoordinates(48.9915, -123.0675), // Blaine Point Roberts
        "090104" to PortCoordinates(43.1566, -79.0414), // Buffalo Lewiston
        "090101" to PortCoordinates(42.9069, -78.9058), // Buffalo Peace Bridge
        "090102" to PortCoordinates(43.0915, -79.0708), // Buffalo Rainbow
        "090103" to PortCoordinates(43.1092, -79.0583), // Buffalo Whirlpool
        "011501" to PortCoordinates(45.1892, -67.2825), // Calais Ferry Point
        "011502" to PortCoordinates(45.1650, -67.2986), // Calais Milltown
        "011503" to PortCoordinates(45.1511, -67.2450), // Calais Intl Ave
        "071201" to PortCoordinates(45.0083, -73.4533), // Champlain
        "020901" to PortCoordinates(45.0058, -72.0983), // Derby Line
        "380001" to PortCoordinates(42.3122, -83.0744), // Detroit Ambassador
        "380002" to PortCoordinates(42.3286, -83.0403), // Detroit Windsor Tunnel
        "250301" to PortCoordinates(32.6735, -115.3879), // Calexico East
        "250302" to PortCoordinates(32.6653, -115.4966), // Calexico West
        "250401" to PortCoordinates(32.5436, -117.0297), // San Ysidro
        "250609" to PortCoordinates(32.5503, -116.9385), // Otay Mesa
        "240201" to PortCoordinates(31.7641, -106.4500), // El Paso BOTA
        "240202" to PortCoordinates(31.7483, -106.4850), // El Paso PDN
        "240203" to PortCoordinates(31.6911, -106.3361), // El Paso Ysleta
        "240204" to PortCoordinates(31.7483, -106.4833), // El Paso Stanton
        "250501" to PortCoordinates(32.5761, -116.6264), // Tecate
        "240801" to PortCoordinates(31.7839, -106.6781), // Santa Teresa
        "230301" to PortCoordinates(28.7061, -100.5133), // Eagle Pass I
        "230302" to PortCoordinates(28.6917, -100.5017), // Eagle Pass II
        "230401" to PortCoordinates(27.5011, -99.5075), // Laredo I
        "230402" to PortCoordinates(27.5000, -99.5033), // Laredo II
        "230403" to PortCoordinates(27.6983, -99.7461), // Laredo Colombia
        "230404" to PortCoordinates(27.6008, -99.5317), // Laredo World Trade
        "260401" to PortCoordinates(31.3325, -110.9444), // Nogales DeConcini
        "260402" to PortCoordinates(31.3289, -110.9633), // Nogales Mariposa
        "260403" to PortCoordinates(31.3331, -110.9411)  // Nogales Morley Gate
    )

    fun getCoordinates(portNumber: String): PortCoordinates? {
        return coordinates[portNumber]
    }
}
