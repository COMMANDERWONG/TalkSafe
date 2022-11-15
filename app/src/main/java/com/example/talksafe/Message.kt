package com.example.talksafe

class Message {
    var message: String? = null
    var senderID: String? = null
    var timed:Boolean? = null
    var timeLimit: Int? = null


    constructor() {}

    constructor(message: String?, senderID: String?, timed:Boolean?) {
        this.message = message
        this.senderID = senderID
        this.timed = timed
    }
}