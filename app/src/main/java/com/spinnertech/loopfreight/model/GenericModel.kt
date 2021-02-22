package com.spinnertech.loopfreight.model

data class GenericModel(
        var status_code : Int  ,
        var msg : String ,
        var data : List<StatResponseItem> ,
        var highestContributor : StatResponseItem?,
        var highestContributorScore: Week?
){
    constructor():this (
         -1  , "" , emptyList() , null , null
    )
}
