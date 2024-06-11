package com.example.fyp_coursework_test.Models

import java.sql.Timestamp

data class CommentModel(
    var userId: String = "n/a",
    var comment: String = "n/a",
    var timestamp: Long = 0
)
