package com.spinnertech.loopfreight.model

data class StatResponseItem(
        val author: Author?,
        val total: Int,
        val weeks: List<Week>
){
    constructor():this (
         null , -10  , emptyList()
    )
}