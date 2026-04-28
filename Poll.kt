package com.example.caquickpoll

data class Poll(
    var pollName: String = "",
    var id: String = "",
    var createdBy: String = "",
    var questions: List<HashMap<String, Any>> = listOf(),
    var votedUsers: List<String> = listOf(),
    var expiryTime: Long = 0,
    var imageUrl: String = "", // ✅ FIXED
    var type: String = "" // ✅ IMPORTANT
) : java.io.Serializable