package com.spinnertech.loopfreight.utils

import android.util.Log
import com.spinnertech.loopfreight.model.ScoreModel
import com.spinnertech.loopfreight.model.StatResponseItem
import com.spinnertech.loopfreight.model.Week
import java.text.SimpleDateFormat

class Utils {
    companion object {
        fun ranKAuther(autherList: List<StatResponseItem>):
                StatResponseItem {
            // here we are reciving the unsorted list of authers of a repo
            // we have to sort them via a,d,c value provided by the api response
            // and return a single object that represent the highest contributor
            var heighestAuther = StatResponseItem()
            var scoreList = mutableListOf<ScoreModel>()
            if (autherList.isNotEmpty()) {
                // we will loop around the weeks and create a map an sum it
                for ((i, item) in autherList.withIndex()) {

                    val totalQuantity: Int = item.weeks.map {
                        it.a
                        it.d
                        it.c
                    }.sum()

                    // now add the the auther  pos and total value in the score list
                    scoreList.add(ScoreModel(i, totalQuantity))
                    //  Log.d("TAG", "ranKAuther: pos $i  -> $totalQuantity")
                }

                // now we have the list of all the scores and now we will sort the score list
                scoreList.sortWith(
                    compareBy {
                        it.score
                    }
                )

                //  Log.d("TAG", "sorted Score  List : " + scoreList )

                // wil  now get the auther form the  the original list

                heighestAuther =
                    autherList[scoreList[scoreList.size - 1].pos_in_list]  // as we save the postion of the auther in score model we
                // as the score list was sorted in ASC so the last item  has the highest rank
            }

            return heighestAuther

        }

        fun calculateScores(auther: StatResponseItem): Week {
            val maxAutherScore = Week(0, 0, 0, 0) // init the max Auhter score
            // now  sum up the A , D ,C  of all weeks in auther model
            val totalA = auther.weeks.sumOf {
                it.a
            }
            val totalD = auther.weeks.sumOf {
                it.d
            }
            val totalC = auther.weeks.sumOf {
                it.c
            }

            maxAutherScore.a = totalA
            maxAutherScore.d = totalD
            maxAutherScore.c = totalC

            return maxAutherScore
        }

        fun covertTime(date: String): String {
            var output: String = date
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                val formatter = SimpleDateFormat("MMM dd,yyyy")
                output = formatter.format(parser.parse(date))
            } catch (Ex: Exception) {
                Log.d("TAG", "convertDate: " + Ex.message)
            }

            return output
        }
    }
}