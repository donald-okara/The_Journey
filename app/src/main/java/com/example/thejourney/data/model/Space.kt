package com.example.thejourney.data.model

import com.google.firebase.firestore.PropertyName

data class Space (
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String,

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name : String,

    @get:PropertyName("parentCommunity")
    @set:PropertyName("parentCommunity")
    var parentCommunity : String,

    @get:PropertyName("ProfileUri")
    @set:PropertyName("profileUri")
    var profileUri : String?,

    @get:PropertyName("bannerUri")
    @set:PropertyName("bannerUri")
    var bannerUri : String?,

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description : String?,

    @get:PropertyName("approvalStatus")
    @set:PropertyName("approvalStatus")
    var approvalStatus : String,

    @PropertyName("membersRequireApproval")
    @set:PropertyName("membersRequireApproval")
    var membersRequireApproval : Boolean,

    @get:PropertyName("members")
    @set:PropertyName("members")
    var members: List<Map<String /*userId*/, String /*role*/>>,

    @get:PropertyName("membersApprovalStatus")
    @set:PropertyName("membersApprovalStatus")
    var membersApprovalStatus : List<Map<String/*userId*/, String/*memberApprovalStatus*/>>,
){
    constructor() : this(
        "",
        "",
        "",
        "",
        null,
        null,
        "pending",
        false,
        emptyList(),
        emptyList(),
    )
}