package com.spinnertech.loopfreight.model

data class repoResponse(
    val incomplete_results: Boolean,
    val items: List<Item>,
    val total_count: Int
){
    constructor() : this ( false , emptyList() , -11) // creating a empty constructor
}
