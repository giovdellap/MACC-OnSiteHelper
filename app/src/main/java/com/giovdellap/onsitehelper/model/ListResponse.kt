package com.giovdellap.onsitehelper.model

import kotlinx.serialization.Serializable

@Serializable
class ListResponse(val data: List<Project>) {
}