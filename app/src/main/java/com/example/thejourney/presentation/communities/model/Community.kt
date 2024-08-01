
package com.example.thejourney.presentation.communities.model

import com.google.firebase.firestore.PropertyName


data class Community(
    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "",

    @get:PropertyName("members")
    @set:PropertyName("members")
    var members: List<Map<String /*userId*/, String /*role*/>> = emptyList(),


    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "Pending",

    @get:PropertyName("aboutUs")
    @set:PropertyName("aboutUs")
    var aboutUs: String? = null,


    @get:PropertyName("communityBannerUrl")
    @set:PropertyName("communityBannerUrl")
    var communityBannerUrl: String? = null,

    @get:PropertyName("profileUrl")
    @set:PropertyName("profileUrl")
    var profileUrl: String? = null
) {
    constructor() : this("", "", emptyList(), "", null, null)
}
