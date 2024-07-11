    package com.example.thejourney

    import android.app.Application
    import com.google.firebase.FirebaseApp

    class JourneyApplication: Application() {
        override fun onCreate() {
            super.onCreate()
            FirebaseApp.initializeApp(this)
        }
    }
