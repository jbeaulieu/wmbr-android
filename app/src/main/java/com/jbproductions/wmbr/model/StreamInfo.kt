package com.jbproductions.wmbr.model

data class StreamInfo (
    var time: String, var showName: String, var hosts: String,
    var id: Int, var url: String?) {

    constructor() : this("", "", "", 0, null)
}