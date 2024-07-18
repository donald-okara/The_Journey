package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun requestNewCommunity(community: Community) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("CommunityViewModel", "User ID: $userId")
            Log.d("CommunityViewModel", "Community Request: $community")

            viewModelScope.launch {
                db.collection("communities").add(community)
                    .addOnSuccessListener {
                        Log.d("CommunityViewModel", "DocumentSnapshot added with ID: ${it.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("CommunityViewModel", "Error adding document", e)
                    }
            }
        } else {
            Log.e("CommunityViewModel", "User not authenticated")
        }
    }
}
