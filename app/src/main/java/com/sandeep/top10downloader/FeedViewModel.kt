package com.sandeep.top10downloader

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

private const val TAG = "FeedViewModel"
val EMPTY_FEED_LIST: List<FeedEntry> = Collections.emptyList()

class FeedViewModel: ViewModel(), DownloadData.DownloaderCallBack {

    private var downloadData: DownloadData? = null
    private var feedCachedUrl = "INVALIDATED"

    private val feed = MutableLiveData<List<FeedEntry>>()
    val feedEntries: LiveData<List<FeedEntry>>
        get() = feed

    init {
        feed.postValue(EMPTY_FEED_LIST)
    }

     fun downloadUrl(feedUrl: String) {
        Log.d(TAG, "downloadUrl: called with url $feedUrl")
        if ( feedUrl != feedCachedUrl) {
            Log.d(TAG, "download url starting async task")
            downloadData = DownloadData(this)
            downloadData?.execute(feedUrl)
            feedCachedUrl = feedUrl
            Log.d(TAG, "download url done")
        } else {
            Log.d(TAG, "download url - url not changed")
        }
    }

    fun invalidate() {
        feedCachedUrl = "INVALIDATE"
    }

    override fun onDataAvailable(data: List<FeedEntry>) {
        Log.d(TAG, "onDataAvailable called")
        feed.value = data
        Log.d(TAG, "onDataAvailable ends")
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared cancelling pending download")
        downloadData?.cancel(true)
    }
}