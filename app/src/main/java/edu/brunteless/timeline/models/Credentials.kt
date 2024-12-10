package edu.brunteless.timeline.models

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
    val username: String,
    val password: String,
    val firstname: String,
    val lastname: String,
    val userId: String
)
