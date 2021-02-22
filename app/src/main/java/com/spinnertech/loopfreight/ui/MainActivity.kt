package com.spinnertech.loopfreight.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinnertech.loopfreight.Adapter.mainRecycerAdapter
import com.spinnertech.loopfreight.databinding.ActivityMainBinding
import com.spinnertech.loopfreight.model.GenericModel
import com.spinnertech.loopfreight.model.repoResponse
import com.spinnertech.loopfreight.model.resultList
import com.spinnertech.loopfreight.repository.DataRepository
import kotlinx.coroutines.*
import java.util.*


class MainActivity : AppCompatActivity(),
    mainRecycerAdapter.Interactions {
    var currentItems = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var mainAdapter: mainRecycerAdapter
    private lateinit var binding: ActivityMainBinding
    private var apiJob: Job? = null
    private var total: Int = 0
    private var page: Int = 1
    private var items = mutableListOf<resultList>()
    private var isScrolling: Boolean = false
    private var searchQuery: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        linearLayoutManager = LinearLayoutManager(this)
        mainAdapter = mainRecycerAdapter(mutableListOf(), this@MainActivity)
        // Adapter = mainListAdapter(this)
        binding.list.apply {
            layoutManager = linearLayoutManager
            adapter = mainAdapter
        }


        binding.serachView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    total = 0
                    page = 1
                    // also clearing the adapter
                    mainAdapter.items.clear()
                    mainAdapter.notifyDataSetChanged()
                    if (query != null) {
                        // removing the old  obsverber to avoid data duploicaiotn
                        DataRepository.searchResultsLiveData.removeObservers(this@MainActivity)
                        searchQuery = query
                        binding.pbar.visibility = View.VISIBLE
                        startSearching(searchQuery)
                    }


                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

            }
        )

        binding.searchButton.setOnClickListener {
            total = 0
            page = 1
            // removing the old  obsverber to avoid data duploicaiotn
            DataRepository.searchResultsLiveData.removeObservers(this@MainActivity)
            // also clearing the adapter
            mainAdapter.items.clear()
            mainAdapter.notifyDataSetChanged()

            searchQuery = binding.serachView.query.toString()
            binding.pbar.visibility = View.VISIBLE
            startSearching(searchQuery)

        }

       initScrollListener()

    }

    private fun startSearching(searchQuery: String?) {
        apiJob?.cancel() // cancelling the long job
        CoroutineScope(Dispatchers.IO).launch {
            DataRepository.getSearchResults(searchQuery, page)
            withContext(Dispatchers.Main) {
                DataRepository.searchResultsLiveData.observe(
                    this@MainActivity,
                    androidx.lifecycle.Observer { model: repoResponse? ->
                        run {
                            if (model != null) {
                                total = model.total_count
                                GetStateANDsetData(model)
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Api Returned With Error,May Be your limit is ended!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.pbar.visibility = View.INVISIBLE
                            }

                        }

                    }
                )

            }


        }
    }

    /*
        # use Case
        As github api will return a  http code = 202 where it need  to start caching the data
        but will return a empty json  object .
        * As after some time if we hit the same api it will return  the cache with a  json list of all the state
        # My solution
        My initial plan is to use a generic data model to create my own model so that i can control the flow
        with response code and update the UI accordingly
     */

    private fun GetStateANDsetData(model: repoResponse) {
        // we will try to loop it
        // and request to get the stats for the repo
        // and will  maintain a new list and  will search for the highest contributor
        if (model.total_count >= 0) {
            Log.d("OLDSIZE", "${model.items.size}")
            model.items.forEach { oldItem ->
              apiJob =   CoroutineScope(Dispatchers.IO).launch {
                    // now we  we will request for  the stat data
                    val name: String = oldItem.name
                    val autherName: String = oldItem.owner.login
                    DataRepository.getStatOfTheRepo(autherName, name)
                    withContext(Dispatchers.Main) {

                        DataRepository.statResultLiveData.observe(
                            this@MainActivity,
                            androidx.lifecycle.Observer { statList: GenericModel ->
                                run {
                                    Log.d(
                                        "TRACKING-> ",
                                        "\nold data name : " + oldItem.name + " auther " + (statList.highestContributorScore?.a
                                                )
                                    )
                                    items.add(resultList(oldItem, statList))
                                    // removing the observer to avoid data duplication
                                    DataRepository.statResultLiveData.removeObservers(this@MainActivity)

                                }


                            }
                        )
                        mainAdapter.items =
                            items // could be improved by useing async difflist or normal difflist
                        mainAdapter.notifyDataSetChanged()
                        isScrolling = false
                        binding.pbar.visibility = View.INVISIBLE

                    }


                }


            }
        }


    }

    override fun onItemSelected(position: Int, item: resultList) {
        val repoLink: String = item.data.html_url
        val uri: Uri = Uri.parse(repoLink)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun loadMore() {
        // 1st check the item count and cehck the loading state
        if (mainAdapter.itemCount < total) {
            page++
            Toast.makeText(applicationContext, "Loading More ", Toast.LENGTH_SHORT).show()
            binding.pbar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                DataRepository.getSearchResults(searchQuery, page)
            }
        } else {
            isScrolling = false
            Toast.makeText(applicationContext, "You Are At the last Page ", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initScrollListener() {

        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { // scroll down
                    currentItems = linearLayoutManager.childCount
                    totalItems = linearLayoutManager.itemCount
                    scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition()
                    if (isScrolling && currentItems + scrollOutItems == totalItems) {
                        isScrolling = false
                        loadMore()
                    }
                }
            }
        })

    }
}




