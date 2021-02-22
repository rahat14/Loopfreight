package com.spinnertech.loopfreight.repository

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.spinnertech.loopfreight.model.GenericModel
import com.spinnertech.loopfreight.model.repoResponse
import com.spinnertech.loopfreight.networking.ApiProvider
import com.spinnertech.loopfreight.networking.ApiService
import com.spinnertech.loopfreight.utils.Utils

object DataRepository {
    var apiService: ApiService? = null
    var searchResultsLiveData: MutableLiveData<repoResponse> = MutableLiveData()
    var statResultLiveData: MutableLiveData<GenericModel> = MutableLiveData()

    init {
        apiService =
            ApiProvider.createService(
                ApiService::class.java
            )
    }

    suspend fun getSearchResults(searchQuery: String?, page: Int) {
        Log.d("RES", "getStatOfTheRepo: $searchQuery $page")
        val genericDataModelLoading = repoResponse()
        searchResultsLiveData.postValue(genericDataModelLoading)
        val result = apiService?.getSearchResult(searchQuery, 5, page)
        if(result?.code()==200){
            val genericDataModel = result.body()
            searchResultsLiveData.postValue(genericDataModel)
        }else {
            searchResultsLiveData.postValue(null)
        }




    }

    suspend fun getStatOfTheRepo(autherName: String, repoName: String) {
        Log.d("RESP", "getStatOfTheRepo: $autherName $repoName")
        val genericModel = GenericModel()
        // statResultLiveData.postValue(genericModel)
        // we have to wrap this request to be safe from crash
        // as github api returns empty JSON object when the caching start
        // but it return an array of object with data when data is available
        // we could both handle the data from the api if we used some third party solutions
        try {
            val result = apiService?.getStat(autherName, repoName)
            val retrivedData = result?.body()
            val code: Int? = result?.code();

            if (result?.code() == 200) {

                genericModel.status_code = 200
                genericModel.msg = "Data Loaded"
                if (retrivedData != null && retrivedData.isNotEmpty()) { // just checking for null not important can be removed
                    genericModel.data = retrivedData
                    // get the highest contributor
                    val maxCon =
                        Utils.ranKAuther(retrivedData) // sending for rank the list and get the highest contributor
                    genericModel.highestContributor = maxCon
                    // now calculate the a,d,c of the highest  contibutor
                    genericModel.highestContributorScore = Utils.calculateScores(maxCon)


                } else {
                    genericModel.data = emptyList()
                }

            } else {
                Log.d("ERROR", "ERROR : " + result?.code()!!)
                genericModel.status_code = result?.code()!!
                genericModel.msg = "Network Error "
                genericModel.data = emptyList()
                genericModel.highestContributor = null
                genericModel.highestContributorScore = null
            }

        } catch (e: Exception) {
            // here json exceptiion will happen
            // if the stat of the contributor data is not cached
            // then it will return empty object
            // but we are expecting an array of data
            Log.d("TAG", "ERROR : ")
            genericModel.status_code = 202
            genericModel.msg = "Data Not Loaded"
            genericModel.data = emptyList()
            genericModel.highestContributor = null
            genericModel.highestContributorScore = null
        }

        statResultLiveData.postValue(genericModel)
    }


}