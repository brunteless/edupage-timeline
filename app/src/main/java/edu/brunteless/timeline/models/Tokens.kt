package edu.brunteless.timeline.models

import kotlinx.serialization.Serializable


@Serializable
data class Tokens(
    val edid: String,
    val esid: String,
    val fromEdupage: String,
    val firstname: String,
    val lastname: String,
    val userId: String
)
