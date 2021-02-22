package com.spinnertech.loopfreight.networking

import com.spinnertech.loopfreight.model.StatResponseItem
import com.spinnertech.loopfreight.model.repoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    companion object {
        val BASE_URL: String = "https://api.github.com/"
    }

    @GET("search/repositories")
    suspend fun getSearchResult(
            @Query("q") query: String?,
            @Query("per_page") per_page: Int?,
            @Query("page") page: Int? ): Response<repoResponse?>

    @GET("repos/{auther_name}/{repo_name}/stats/contributors")
    suspend fun  getStat(
            @Path("auther_name") autherName: String ,
            @Path("repo_name") repo_name: String
    ) : Response<List<StatResponseItem>?>
}