package it.polito.madlab5.model.Task

import it.polito.madlab5.model.Profile.Profile

data class History(
    val taskHistory: String,
    val status: String,
    val date: String,
    val user: String
)