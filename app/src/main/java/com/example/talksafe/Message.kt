package com.example.talksafe

class Message {
    var message: String? = null
    var senderID: String? = null

    constructor() {}

    constructor(message: String?, senderID: String?) {
        this.message = message
        this.senderID = senderID
    }
}