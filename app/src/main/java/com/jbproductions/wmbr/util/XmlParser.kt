package com.jbproductions.wmbr.util

import android.util.Xml
import com.jbproductions.wmbr.model.Show
import com.jbproductions.wmbr.model.StreamInfo
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private const val INFO_URL : String = "https://wmbr.org/cgi-bin/xmlinfo"
private const val SCHEDULE_URL : String = "https://wmbr.org/cgi-bin/xmlsched"
//private const val TRACK_BLASTER_URL : String = "https://www.track-blaster.com/wmbr/pl_recent_songs.php"
private val ns : String? = null

@Throws(XmlPullParserException:: class, IOException::class)
fun parseStreamInfo() : StreamInfo {

    val inputStream = getInputStream(INFO_URL)

    inputStream.use {
        val parser : XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return getInfo(parser)
    }
}

@Throws(XmlPullParserException:: class, IOException::class)
fun parseSchedule() : List<*> {

    val inputStream = getInputStream(SCHEDULE_URL)

    inputStream.use {
        val parser : XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return buildShowList(parser)
    }
}

// Given a string representation of a URL, sets up a connection and gets
// an input stream.
@Throws(IOException::class)
private fun getInputStream(urlString: String): InputStream? {
    val url = URL(urlString)
    return (url.openConnection() as? HttpURLConnection)?.run {
        readTimeout = 10000
        connectTimeout = 15000
        requestMethod = "GET"
        doInput = true
        // Starts the query
        connect()
        inputStream
    }
}

@Throws(XmlPullParserException::class, IOException::class)
private fun getInfo(parser: XmlPullParser) : StreamInfo {

    parser.require(XmlPullParser.START_TAG, ns, "wmbrinfo")
    val streamInfo = StreamInfo()

    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        when (parser.name) {
            "time" -> streamInfo.time = readString(parser, "time")
            "showname" -> streamInfo.showName = readString(parser, "showname")
            "showhosts" -> streamInfo.hosts = readString(parser, "showhosts")
            "showid" -> streamInfo.id = readInt(parser, "showid")
            "showurl" -> streamInfo.url = readString(parser, "showurl")
            else -> skip(parser)
        }
    }

    return streamInfo
}

@Throws(XmlPullParserException::class, IOException::class)
private fun buildShowList(parser: XmlPullParser) : List<Show> {

    parser.require(XmlPullParser.START_TAG, ns, "wmbr_schedule")
    val shows = mutableListOf<Show>()

    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        // Starts by looking for the entry tag
        if (parser.name == "show") {
            shows.add(readShow(parser))
        } else {
            skip(parser)
        }
    }

    return shows
}

// Parses the contents of a show. If it encounters a valid tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
@Throws(XmlPullParserException::class, IOException::class)
private fun readShow(parser: XmlPullParser) : Show {

    parser.require(XmlPullParser.START_TAG, ns, "show")
    val newShow = Show()

    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        when (parser.name) {
            "id" -> newShow.id = readInt(parser, "id")
            "name" -> newShow.name = readString(parser, "name")
            "day" -> newShow.day = readInt(parser, "day")
            "time" -> newShow.time = readInt(parser, "time")
            "length" -> newShow.length = readInt(parser, "length")
            "hosts" -> newShow.hosts = readString(parser, "hosts")
            "description" -> newShow.description = readString(parser, "description")
            "url" -> newShow.url = readString(parser, "url")
            "email" -> newShow.emailPrefix = readString(parser, "email")
            "alternates" -> newShow.alternates = readInt(parser, "alternates") != 0
            else -> skip(parser)
        }
    }

    return newShow
}

// Processes name tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
private fun readString(parser: XmlPullParser, tag: String) : String {
    parser.require(XmlPullParser.START_TAG, ns, tag)
    val name = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, tag)
    return name
}

@Throws(IOException::class, XmlPullParserException::class)
private fun readInt(parser: XmlPullParser, tag: String) : Int {
    parser.require(XmlPullParser.START_TAG, ns, tag)
    val day = readText(parser).toInt()
    parser.require(XmlPullParser.END_TAG, ns, tag)
    return day
}

@Throws(IOException::class, XmlPullParserException::class)
private fun readText(parser : XmlPullParser) : String {
    var result =""
    if (parser.next() == XmlPullParser.TEXT) {
        result = parser.text
        parser.nextTag()
    }
    return result
}

@Throws(IOException::class, XmlPullParserException::class)
private fun skip(parser : XmlPullParser) {
    if (parser.eventType != XmlPullParser.START_TAG) {
        throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
        when (parser.next()) {
            XmlPullParser.END_TAG -> depth--
            XmlPullParser.START_TAG -> depth++
        }
    }
}