package com.sandeep.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlin.properties.Delegates

class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var title: String = ""
    var imageURL: String = ""

    override fun toString(): String {
        return """
            name = $name
            artist = $artist
            releaseDate = $releaseDate
            title = $title
            imageURL = $imageURL
        """.trimIndent()
    }
}
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var downloadData: DownloadData? = null //by lazy { DownloadData(this, xmlListView)}

    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topMovies/limit=%d/xml"
    private var feedLimit: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate called")

        downloadUrl(feedUrl.format(feedLimit))

        // val downloadData = DownloadData(this, xmlListView)

        Log.d(TAG, "onCreate done")
    }

    private fun downloadUrl(feedUrl: String) {
        Log.d(TAG, "download url")
        downloadData = DownloadData(this, xmlListView)
        downloadData?.execute(feedUrl)
        Log.d(TAG, "download url done")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feed_menu, menu)

        if (feedLimit == 10) {
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else {
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {
            R.id.mnuFree ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnuPaid ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnuSongs ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10, R.id.mnu25 -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "onItemSelected: ${item.title} setting limit to $feedLimit")
                } else {
                    Log.d(TAG, "onItemSelected: ${item.title} settign limit unchanged")
                }
            }
            else ->
                return super.onOptionsItemSelected(item)
        }

        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    companion object {
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            var propConext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init {
                propConext = context
                propListView = listView
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                // Log.d(TAG, "onPostExecute called parameter is $result")
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

                //val arrayAdapter = ArrayAdapter<FeedEntry>(propConext, R.layout.list_item, parseApplications.applications)
                //propListView.adapter = arrayAdapter

                val feedAdapter = FeedAdapter(propConext, R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdapter
            }

            override fun doInBackground(vararg url: String?): String {
                // Log.d(TAG, "doInBackground starts with ${url[0]}")

                val rssFeed = downloadXML(url[0])

                if (rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground : error in downloading")
                }
                return rssFeed
            }

            private fun downloadXML( urlpath: String?) : String {
                return URL(urlpath).readText()

            }

            private fun downloadXML1( urlpath: String?) : String {
                val xmlResult = StringBuilder()

                try {
                    val url = URL(urlpath)

                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    val response = connection.responseCode

                    Log.d(TAG, "downloadXML:  response code is $response")

                    /*val inputstream = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputstream)
                    val reader = BufferedReader(inputStreamReader)*/

                    /*var reader = BufferedReader(InputStreamReader(connection.inputStream))

                    val inputBuffer = CharArray(500)
                    var charsRead = 0
                    while (charsRead >= 0) {
                        charsRead = reader.read(inputBuffer)
                        if (charsRead > 0) {
                            xmlResult.append(String(inputBuffer, 0, charsRead))
                        }
                    }

                    reader.close()*/

                    //val stream = connection.inputStream
                    connection.inputStream.buffered().reader().use { xmlResult.append(it.readText()) }

                    Log.d(TAG, "received ${xmlResult.length} bytes")

                    return xmlResult.toString()

                /*} catch (e: MalformedURLException) {
                    Log.e(TAG, "downloadXML:  invalid url ${e.message}")
                } catch (e: IOException) {
                    Log.e(TAG, "downloadXML: IOException ${e.message}")
                } catch (e: SecurityException) {
                    Log.e(TAG, "downloadXML: SecurityException needs permission ${e.message}")
                }catch (e: Exception) {
                    Log.e(TAG, "downloadXML: unknown error ${e.message}")
                }*/

                } catch (e: Exception){
                    val errorMessage: String = when (e) {
                        is MalformedURLException -> "donloadXML:  invalid url ${e.message}"
                        is IOException -> "donloadXML:  IOException ${e.message}"
                        is SecurityException -> { e.printStackTrace()
                            "downloadXML: SecurityException needs permission ${e.message}"
                        }
                        else -> "downloadXML: unknown error ${e.message}"
                    }

                    Log.e(TAG, errorMessage)

                }
                return "" //if here, there is an issue
            }

        }
    }


}
