package com.garitawatch.app.remote

import com.garitawatch.app.data.remote.model.BorderWaitTimes
import com.tickaroo.tikxml.TikXml
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CbpXmlParsingTest {

    private val tikXml = TikXml.Builder()
        .exceptionOnUnreadXml(false)
        .build()

    @Test
    fun `parse border_wait_time root`() {
        val xml = """
            <border_wait_time>
                <port>
                    <port_number>250401</port_number>
                    <border>Canadian Border</border>
                    <port_name>Buffalo</port_name>
                    <crossing_name>Peace Bridge</crossing_name>
                    <port_status>Open</port_status>
                    <passenger_vehicle_lanes>
                        <standard_lanes>
                            <update_time>10:00 am EST</update_time>
                            <operational_status>no delay</operational_status>
                            <delay_minutes>0</delay_minutes>
                            <lanes_open>3</lanes_open>
                        </standard_lanes>
                    </passenger_vehicle_lanes>
                </port>
            </border_wait_time>
        """.trimIndent()

        val buffer = Buffer().writeUtf8(xml)
        val response = tikXml.read(buffer, BorderWaitTimes::class.java)

        assertNotNull(response)
        assertNotNull(response.ports)
        assertEquals(1, response.ports?.size)
        val port = response.ports?.first()
        assertEquals("250401", port?.portNumber)
        assertEquals("Buffalo", port?.portName)
        assertEquals(0, port?.passengerVehicleLanes?.standardLanes?.delayMinutes?.filter { it.isDigit() }?.toIntOrNull() ?: -1)
    }
}
