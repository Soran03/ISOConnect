package com.example.fyp_coursework_test.Models

import java.util.Date

data class IsocModel (
    var isocId: String = "n/a",
    var isocName: String = "n/a",
    var adminList: List<String> = listOf(),
    var memberList: List<String> = listOf(),
    var profilePic: String = "n/a"

)