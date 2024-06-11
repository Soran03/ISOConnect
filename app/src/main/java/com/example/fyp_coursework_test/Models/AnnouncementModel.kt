package com.example.fyp_coursework_test.Models

import java.util.Date

data class AnnouncementModel(
    var announcementId: String = "n/a",
    var adminId: String = "n/a",
    var announcementTitle: String = "n/a",
    var description: String = "n/a",
    var announcementDate: Date = Date(2000,1,1),
    var announcementTime: Long = 0L,  // Consider using a suitable time format
    var announcementLocation: String = "n/a",
    var timestamp: Long = 0,
    var imageUrl: String = "n/a")
