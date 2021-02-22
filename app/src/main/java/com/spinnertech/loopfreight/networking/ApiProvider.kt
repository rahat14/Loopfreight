package com.spinnertech.loopfreight.networking

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiProvider {
    private val retrofit = Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
          //  .client(getHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private fun getHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor() // loging the respose for testing purpose only
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        return httpClient.build()
    }

    fun <S> createService(serviceClass: Class<S>?): S {
        return retrofit.create(serviceClass)
    }
}