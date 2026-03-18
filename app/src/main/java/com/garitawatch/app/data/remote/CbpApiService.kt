package com.garitawatch.app.data.remote

import com.garitawatch.app.data.remote.model.BorderWaitTimes
import retrofit2.http.GET

interface CbpApiService {
    @GET("xml/bwt.xml")
    suspend fun getBorderWaitTimes(): BorderWaitTimes

    companion object {
        const val BASE_URL = "https://bwt.cbp.gov/"
    }
}
