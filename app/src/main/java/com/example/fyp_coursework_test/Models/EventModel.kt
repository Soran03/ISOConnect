package com.example.fyp_coursework_test.Models

import java.util.Date

data class EventModel(
    var eventId: String = "n/a",
    var userID: String = "n/a",
    var eventName: String = "n/a",
    var description: String = "n/a",
    var address: String = "n/a",
    var eventDate: Date = Date(2000,1,1),
    var startTime: Long = 0L,  // Consider using a suitable time format
    var endTime: Long = 0L,    // Consider using a suitable time format
    var imageUrl: String = "n/a",
    var likesCount: Int = 0,
    var likedList: List<String> = listOf(),
    )