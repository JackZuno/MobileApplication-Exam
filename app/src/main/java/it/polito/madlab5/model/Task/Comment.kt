package it.polito.madlab5.model.Task

import it.polito.madlab5.model.Profile.Profile


data class Comment(
    var profileId: String,
    var profileName: String?,
    var taskId: String,
    var text: String,
    var data: String) {
}