package com.jbproductions.wmbr.model

data class Show(
    var id: Int, var day: Int, var time: Int, var length: Int,
    var name: String, var hosts: String, var description:String?, var url: String?, var emailPrefix: String?,
    var alternates: Boolean) {

    constructor() : this(0, 0, 0, 0, "", "")
    constructor(id: Int, day: Int, time: Int, length: Int, name: String, hosts: String) :
            this(id, day, time, length, name, hosts, null, null, null, false)

    val timeAsString: String
    get() {
       if(time == 0) return "12:00AM"
       return when (time) {
           0 -> "12:00 AM"
           1200 -> "12:00 PM"
           else -> {
               val hours = if (time < 1200) time/60 else (time/60 - 12)
               val minutes = if (time%60 == 0) "00" else time%60
               val suffix = if (time < 1200) "AM" else "PM"
               "$hours:$minutes $suffix"
           }
       }
    }

    val email: String?
    get() {
       if (emailPrefix == null) return null
       if (emailPrefix!!.contains("@")) {
           return emailPrefix
       } else if ("" != emailPrefix) {
           return "$emailPrefix@wmbr.org"
       }
       return ""
    }
}