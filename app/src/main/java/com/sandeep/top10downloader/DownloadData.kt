package com.sandeep.top10downloader

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

private const val TAG = "DownloadData"

class DownloadData(private val callBack: DownloaderCallBack) : AsyncTask<String, Void, String>() {

    interface DownloaderCallBack {
        fun onDataAvailable(data: List<FeedEntry>)
    }


    override fun onPostExecute(result: String) {

        val parseApplications = ParseApplications()
        if (result.isNotEmpty()) {
            parseApplications.parse(result)
        }

        callBack.onDataAvailable(parseApplications.applications)
    }

    override fun doInBackground(vararg url: String): String {
        // Log.d(TAG, "doInBackground starts with ${url[0]}")

        val rssFeed = downloadXML(url[0])

        if (rssFeed.isEmpty()) {
            Log.e(TAG, "doInBackground : error in downloading")
        }
        return rssFeed
    }

    private fun downloadXML( urlpath: String) : String {
        try {
            return URL(urlpath).readText()
        } catch (e: MalformedURLException){
            Log.d(TAG, "downloadXml: invalid url ${e.message}")
        } catch (e: IOException){
            Log.d(TAG, "downloadXml: IOException url ${e.message}")
        } catch (e: SecurityException){
            Log.d(TAG, "downloadXml: Security exception, need permissions ${e.message}")
        }
        return ""

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