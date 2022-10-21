package com.hatenablog.gikoha.drivelapseapp


import androidx.compose.runtime.Stable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// API GET

@Stable
data class DriveLapse (
    val direction: String,
    val depdate: String,
    val depday: String,
    val deptime: String,
    val arrivetime: String,
    val duration: String,
    val p1time: String?,
    val p2time: String?,
    val p3time: String?,
    val route: String?,
    val weather: String?,
)

interface DriveLapseGet
{
    @GET(BuildConfig.drivegetapi)
    suspend fun getItems(): Response<Array<DriveLapse>>
}

// API POST
@Stable
data class DriveLapsePostJson(
    val apikey: String,
    val direction: String,
    val deptime: String,
    val p1time: String,
    val p2time: String,
    val p3time: String,
    val arrtime: String,
    val route: String,
    val weather: String,
)

interface DriveLapsePost
{
    @POST(BuildConfig.drivepostapi)
    suspend fun postItem(
        @Body postdata: DriveLapsePostJson
    ): Response<Void>
}
