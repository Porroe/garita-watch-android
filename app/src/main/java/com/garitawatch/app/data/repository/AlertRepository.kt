package com.garitawatch.app.data.repository

import com.garitawatch.app.data.local.dao.AlertDao
import com.garitawatch.app.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val alertDao: AlertDao
) {
    val allAlerts: Flow<List<AlertEntity>> = alertDao.getAllAlerts()

    suspend fun getAlertById(id: Int): AlertEntity? = alertDao.getAlertById(id)

    suspend fun insertAlert(alert: AlertEntity) = alertDao.insertAlert(alert)

    suspend fun updateAlert(alert: AlertEntity) = alertDao.updateAlert(alert)

    suspend fun deleteAlert(alert: AlertEntity) = alertDao.deleteAlert(alert)
}
