package com.porroe.garitawatch.domain.util

object PortNavigation {
    private val portMapsIntents = mapOf(
        "070801" to "geo:0,0?q=Thousand+Islands+Bridge,+Alexandria+Bay,+NY+13607,+USA",
        "300401" to "geo:0,0?q=Pacific+Highway+Port+of+Entry,+Blaine,+WA+98230,+USA",
        "300402" to "geo:0,0?q=Peace+Arch+Port+of+Entry,+Blaine,+WA+98230,+USA",
        "300403" to "geo:0,0?q=Point+Roberts+Port+of+Entry,+Point+Roberts,+WA+98281,+USA",
        "090104" to "geo:0,0?q=Lewiston-Queenston+Bridge+US+POE,+Lewiston,+NY+14092,+USA",
        "090101" to "geo:0,0?q=Peace+Bridge+US+POE,+Buffalo,+NY+14213,+USA",
        "090102" to "geo:0,0?q=Rainbow+Bridge+US+POE,+Niagara+Falls,+NY+14303,+USA",
        "090103" to "geo:0,0?q=Whirlpool+Bridge+US+POE,+Niagara+Falls,+NY+14303,+USA",
        "011501" to "geo:0,0?q=Ferry+Point+US+POE,+Calais,+ME+04619,+USA",
        "011502" to "geo:0,0?q=Milltown+US+POE,+Calais,+ME+04619,+USA",
        "011503" to "geo:0,0?q=International+Avenue+US+POE,+Calais,+ME+04619,+USA",
        "071201" to "geo:0,0?q=Champlain+US+POE,+Champlain,+NY+12919,+USA",
        "020901" to "geo:0,0?q=Derby+Line+I-91+US+POE,+Derby+Line,+VT+05830,+USA",
        "380001" to "geo:0,0?q=Ambassador+Bridge+US+POE,+Detroit,+MI+48211,+USA",
        "380002" to "geo:0,0?q=Detroit-Windsor+Tunnel+US+POE,+Detroit,+MI+48226,+USA",
        "250301" to "geo:0,0?q=1699+E+Carr+Rd,+Calexico,+CA+92231,+USA",
        "250302" to "geo:0,0?q=200+E+1st+St,+Calexico,+CA+92231,+USA",
        "250401" to "geo:0,0?q=San+Ysidro+US+POE,+San+Ysidro,+CA+92173,+USA",
        "250609" to "geo:0,0?q=Otay+Mesa+US+POE,+San+Diego,+CA+92154,+USA",
        "240201" to "geo:0,0?q=BOTA+Bridge+US+POE,+El+Paso,+TX+79901,+USA",
        "240202" to "geo:0,0?q=Paso+Del+Norte+US+POE,+El+Paso,+TX+79901,+USA",
        "240203" to "geo:0,0?q=Ysleta+US+POE,+El+Paso,+TX+79907,+USA",
        "240204" to "geo:0,0?q=Stanton+US+POE,+El+Paso,+TX+79907,+USA",
        "250501" to "geo:0,0?q=Tecate+US+POE,+Tecate,+CA+91980,+USA",
        "240801" to "geo:0,0?q=Santa+Teresa+US+POE,+Santa+Teresa,+NM+88008,+USA",
        "230301" to "geo:0,0?q=Eagle+Pass+US+POE,+Eagle+Pass,+TX+78852,+USA",
        "230302" to "geo:0,0?q=Eagle+Pass+US+POE,+Eagle+Pass,+TX+78852,+USA",
        "230401" to "geo:0,0?q=Laredo+Bridge+I+US+POE,+Laredo,+TX+78043,+USA",
        "230402" to "geo:0,0?q=Laredo+Bridge+II+US+POE,+Laredo,+TX+78043,+USA",
        "230403" to "geo:0,0?q=Colombia+Solidarity+US+POE,+Laredo,+TX+78045,+USA",
        "230404" to "geo:0,0?q=World+Trade+Bridge+US+POE,+Laredo,+TX+78045,+USA",
        "260401" to "geo:0,0?q=Nogales-DeConcini+US+POE,+Nogales,+AZ+85621,+USA",
        "260402" to "geo:0,0?q=Nogales-Mariposa+US+POE,+Nogales,+AZ+85621,+USA",
        "260403" to "geo:0,0?q=Nogales-Morley+Gate+US+POE,+Nogales,+AZ+85621,+USA"
    )

    fun getMapsIntent(portNumber: String): String? {
        return portMapsIntents[portNumber]
    }
}
