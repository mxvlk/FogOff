package com.android.fogapp.ui.main

/**
 * Class to define main screen events
 */
sealed class MainScreenEvent {

    /**
     * Event fired when the button to start the tracking is clicked
     */
    data object OnStartTrackingClick: MainScreenEvent()

}