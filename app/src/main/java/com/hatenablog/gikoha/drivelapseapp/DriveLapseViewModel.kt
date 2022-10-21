package com.hatenablog.gikoha.drivelapseapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DriveLapseViewModel @Inject constructor() : ViewModel()
{
    private val _buttontitle = MutableLiveData("SUBMIT")
    val buttontitle: LiveData<String> = _buttontitle

    private val _items = MutableStateFlow<List<DriveLapse>?>(null)
    val state = _items.map {
        DriveLapseViewState(it)
    }

    fun changeTitle(title: String)
    {
        _buttontitle.value = title
    }

    fun clearData()
    {
        _items.value = null
    }

    fun loadData(callback: () -> Unit)
    {
        val getapi = apiBuilder().create(DriveLapseGet::class.java)

        // repo access is suspended function, so run in CoroutineScope

        viewModelScope.launch {
            val response = getapi.getItems()
            if (response.isSuccessful)
            {
                // success
                val data = response.body()!!
                _items.update { data.toList() }
                callback()
            }
        }

    }

    fun postData(direction: String,
                 depTime: String,
                 p1Time: String,
                 p2Time: String,
                 p3Time: String,
                 arrTime: String,
                 route: String,
                 callback: () -> Unit)
    {
        val postapi = apiBuilder().create(DriveLapsePost::class.java)

        val d = DriveLapsePostJson(BuildConfig.driveapikey, direction, depTime,
                                   p1Time, p2Time, p3Time,arrTime, route,"晴れ")

        viewModelScope.launch {

            // repo access is suspended function, so run in CoroutineScope
            val response = postapi.postItem(d)
            if (response.isSuccessful)
            {
                // success
                callback()
            }
        }
    }


    // api builder utility function for retrofit

    private fun apiBuilder(): Retrofit
    {
        // access API
        val client = buildOkHttp()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.driveserverurl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // okhttp build utility function

    private fun buildOkHttp(): OkHttpClient
    {
        val client = OkHttpClient.Builder()
        client.connectTimeout(20, TimeUnit.SECONDS)
        client.readTimeout(15, TimeUnit.SECONDS)
        client.writeTimeout(15, TimeUnit.SECONDS)
        return client.build()
    }

}
