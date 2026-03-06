package com.porroe.garitawatch.data.remote.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "border_wait_time")
data class BorderWaitTimes(
    @PropertyElement(name = "last_updated_date")
    val lastUpdatedDate: String?,
    @PropertyElement(name = "last_updated_time")
    val lastUpdatedTime: String?,
    @Element(name = "port")
    val ports: List<PortResponse>?
)

@Xml(name = "port")
data class PortResponse(
    @PropertyElement(name = "port_number")
    val portNumber: String,
    @PropertyElement(name = "border")
    val border: String,
    @PropertyElement(name = "port_name")
    val portName: String,
    @PropertyElement(name = "crossing_name")
    val crossingName: String,
    @PropertyElement(name = "port_status")
    val portStatus: String,
    @PropertyElement(name = "date")
    val date: String?,
    @PropertyElement(name = "time")
    val time: String?,
    @Element(name = "commercial_vehicle_lanes")
    val commercialVehicleLanes: LaneCategory?,
    @Element(name = "passenger_vehicle_lanes")
    val passengerVehicleLanes: LaneCategory?,
    @Element(name = "pedestrian_lanes")
    val pedestrianLanes: LaneCategory?
)

@Xml
data class LaneCategory(
    @Element(name = "standard_lanes")
    val standardLanes: LaneInfo?,
    @Element(name = "NEXUS_SENTRI_lanes")
    val sentriLanes: LaneInfo?,
    @Element(name = "ready_lanes")
    val readyLanes: LaneInfo?,
    @Element(name = "FAST_lanes")
    val fastLanes: LaneInfo?
)

@Xml
data class LaneInfo(
    @PropertyElement(name = "update_time")
    val updateTime: String?,
    @PropertyElement(name = "operational_status")
    val operationalStatus: String?,
    @PropertyElement(name = "delay_minutes")
    val delayMinutes: String?,
    @PropertyElement(name = "lanes_open")
    val lanesOpen: String?
)
