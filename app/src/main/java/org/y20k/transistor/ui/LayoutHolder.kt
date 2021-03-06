/*
 * LayoutHolder.kt
 * Implements the LayoutHolder class
 * A LayoutHolder hold references to the main views
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-20 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.y20k.transistor.Keys
import org.y20k.transistor.R
import org.y20k.transistor.core.Station
import org.y20k.transistor.helpers.DateTimeHelper
import org.y20k.transistor.helpers.LogHelper
import org.y20k.transistor.helpers.PreferencesHelper
import org.y20k.transistor.helpers.UiHelper


/*
 * LayoutHolder class
 */
data class LayoutHolder(var rootView: View) {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(LayoutHolder::class.java)


    /* Main class variables */
    var swipeRefreshLayout: SwipeRefreshLayout
    var recyclerView: RecyclerView
    val layoutManager: LinearLayoutManager
    private var bottomSheet: ConstraintLayout
    private var sheetMetadataViews: Group
    var sleepTimerRunningViews: Group
    private var downloadProgressIndicator: ProgressBar
    private var stationImageView: ImageView
    private var stationNameView: TextView
    private var metadataView: TextView
    var playButtonView: ImageView
    private var sheetStreamingLinkView: TextView
    private var sheetMetadataHistoryView: TextView
    var sheetNextMetadataView: ImageView
    var sheetPreviousMetadataView: ImageView
    var sheetSleepTimerStartButtonView: ImageView
    var sheetSleepTimerCancelButtonView: ImageView
    private var sheetSleepTimerRemainingTimeView: TextView
    private var onboardingLayout: ConstraintLayout
    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var metadataHistory: MutableList<String>
    private var metadataHistoryPosition: Int


    /* Init block */
    init {
        // find views
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        recyclerView = rootView.findViewById(R.id.station_list)
        bottomSheet = rootView.findViewById(R.id.bottom_sheet)
        sheetMetadataViews = rootView.findViewById(R.id.sheet_metadata_views)
        sleepTimerRunningViews = rootView.findViewById(R.id.sleep_timer_running_views)
        downloadProgressIndicator = rootView.findViewById(R.id.download_progress_indicator)
        stationImageView = rootView.findViewById(R.id.station_icon)
        stationNameView = rootView.findViewById(R.id.player_station_name)
        metadataView = rootView.findViewById(R.id.player_station_metadata)
        playButtonView = rootView.findViewById(R.id.player_play_button)
        sheetStreamingLinkView = rootView.findViewById(R.id.sheet_streaming_link)
        sheetMetadataHistoryView = rootView.findViewById(R.id.sheet_metadata_history)
        sheetNextMetadataView = rootView.findViewById(R.id.sheet_next_metadata_button)
        sheetPreviousMetadataView = rootView.findViewById(R.id.sheet_previous_metadata_button)
        sheetSleepTimerStartButtonView = rootView.findViewById(R.id.sleep_timer_start_button)
        sheetSleepTimerCancelButtonView = rootView.findViewById(R.id.sleep_timer_cancel_button)
        sheetSleepTimerRemainingTimeView = rootView.findViewById(R.id.sleep_timer_remaining_time)
        onboardingLayout = rootView.findViewById(R.id.onboarding_layout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        metadataHistory = PreferencesHelper.loadMetadataHistory(rootView.context)
        metadataHistoryPosition = metadataHistory.size - 1

        // set up RecyclerView
        layoutManager = CustomLayoutManager(rootView.context)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        // set up metadata history next and previous buttons
        sheetPreviousMetadataView.setOnClickListener {
            if (metadataHistory.isNotEmpty()) {
                if (metadataHistoryPosition > 0) {
                    metadataHistoryPosition -= 1
                } else {
                    metadataHistoryPosition = metadataHistory.size - 1
                }
                sheetMetadataHistoryView.text = metadataHistory[metadataHistoryPosition]
            }
        }
        sheetNextMetadataView.setOnClickListener {
            if (metadataHistory.isNotEmpty()) {
                if (metadataHistoryPosition < metadataHistory.size - 1) {
                    metadataHistoryPosition += 1
                } else {
                    metadataHistoryPosition = 0
                }
                sheetMetadataHistoryView.text = metadataHistory[metadataHistoryPosition]
            }
        }

        // set layout for player
        setupBottomSheet()
    }


    /* Updates the player views */
    fun updatePlayerViews(context: Context, station: Station, metadataHistory: ArrayList<String> = arrayListOf()) {
        // update name
        stationNameView.text = station.name

        // update cover
        if (station.imageColor != -1) {
            stationImageView.setBackgroundColor(station.imageColor)
        }
        stationImageView.setImageURI(Uri.parse(station.smallImage))
        stationImageView.contentDescription = "${context.getString(R.string.descr_player_station_image)}: ${station.name}"

        // update streaming link
        sheetStreamingLinkView.text = station.getStreamUri()

        // update click listeners
        sheetStreamingLinkView.setOnClickListener{
            val clip: ClipData = ClipData.newPlainText("simple text", sheetStreamingLinkView.text)
            val cm: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(clip)
            Toast.makeText(context, R.string.toastmessage_copied_to_clipboard_url, Toast.LENGTH_LONG).show()
        }
        sheetMetadataHistoryView.setOnClickListener {
            val clip: ClipData = ClipData.newPlainText("simple text", sheetMetadataHistoryView.text)
            val cm: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(clip)
            Toast.makeText(context, R.string.toastmessage_copied_to_clipboard_metadata, Toast.LENGTH_LONG).show()        }
    }


    /* Updates the metadata views */
    fun updateMetadata(metadataHistoryList: MutableList<String>, stationName: String, playbackState: Int) {
        if (metadataHistoryList.isNotEmpty()) {
            metadataHistory = metadataHistoryList
            if (metadataHistory.last() != metadataView.text) {
                metadataHistoryPosition = metadataHistory.size - 1
                val metadataString = metadataHistory[metadataHistoryPosition]
                when (playbackState) {
                    PlaybackStateCompat.STATE_PLAYING -> metadataView.text = metadataString
                    else -> metadataView.text = stationName
                }
                sheetMetadataHistoryView.text = metadataString
                sheetMetadataHistoryView.isSelected = true
            }
        }
    }


    /* Updates sleep timer views */
    fun updateSleepTimer(context: Context, timeRemaining: Long = 0L) {
        when (timeRemaining) {
            0L -> {
                sleepTimerRunningViews.visibility = View.GONE
            }
            else -> {
                sleepTimerRunningViews.visibility = View.VISIBLE
                val sleepTimerTimeRemaining = DateTimeHelper.convertToMinutesAndSeconds(timeRemaining)
                sheetSleepTimerRemainingTimeView.text = sleepTimerTimeRemaining
                sheetSleepTimerRemainingTimeView.contentDescription = "${context.getString(R.string.descr_expanded_player_sleep_timer_remaining_time)}: ${sleepTimerTimeRemaining}"            }
        }
    }


    /* Toggles play/pause button */
    fun togglePlayButton(playbackState: Int) {
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                playButtonView.setImageResource(R.drawable.ic_stop_symbol_white_36dp)
            }
            else -> {
                playButtonView.setImageResource(R.drawable.ic_play_symbol_white_36dp)
                metadataView.text
            }
        }
    }


    /* Toggles visibility of player depending on playback state - hiding it when playback is stopped (not paused or playing) */
//    fun togglePlayerVisibility(context: Context, playbackState: Int): Boolean {
//        when (playbackState) {
//            PlaybackStateCompat.STATE_STOPPED -> return hidePlayer(context)
//            PlaybackStateCompat.STATE_NONE -> return hidePlayer(context)
//            PlaybackStateCompat.STATE_ERROR -> return hidePlayer(context)
//            else -> return showPlayer(context)
//        }
//    }


    /* Toggles visibility of the download progress indicator */
    fun toggleDownloadProgressIndicator(context: Context) {
        when (PreferencesHelper.loadActiveDownloads(context)) {
            Keys.ACTIVE_DOWNLOADS_EMPTY -> downloadProgressIndicator.visibility = View.GONE
            else -> downloadProgressIndicator.visibility = View.VISIBLE
        }
    }


    /* Toggles visibility of the onboarding screen */
    fun toggleOnboarding(context: Context, collectionSize: Int): Boolean {
        if (collectionSize == 0) {
            onboardingLayout.visibility = View.VISIBLE
            hidePlayer(context)
            return true
        } else {
            onboardingLayout.visibility = View.GONE
            showPlayer(context)
            return false
        }
    }



    /* Initiates the rotation animation of the play button  */
    fun animatePlaybackButtonStateTransition(context: Context, playbackState: Int) {
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                val rotateClockwise = AnimationUtils.loadAnimation(context, R.anim.rotate_clockwise_slow)
                rotateClockwise.setAnimationListener(createAnimationListener(playbackState))
                playButtonView.startAnimation(rotateClockwise)
            }

            else -> {
                val rotateCounterClockwise = AnimationUtils.loadAnimation(context, R.anim.rotate_counterclockwise_fast)
                rotateCounterClockwise.setAnimationListener(createAnimationListener(playbackState))
                playButtonView.startAnimation(rotateCounterClockwise)
            }

        }
    }


    /* Shows player */
    private fun showPlayer(context: Context): Boolean {
        UiHelper.setViewMargins(context, swipeRefreshLayout, 0,0,0, Keys.BOTTOM_SHEET_PEEK_HEIGHT)
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN && onboardingLayout.visibility == View.GONE) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return true
    }


    /* Hides player */
    private fun hidePlayer(context: Context): Boolean {
        UiHelper.setViewMargins(context, swipeRefreshLayout, 0,0,0, 0)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        return true
    }


    /* Creates AnimationListener for play button */
    private fun createAnimationListener(playbackState: Int): Animation.AnimationListener {
        return object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                // set up button symbol and playback indicator afterwards
                togglePlayButton(playbackState)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        }
    }


    /* Sets up the player (BottomSheet) */
    private fun setupBottomSheet() {
        // show / hide the small player
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(view: View, slideOffset: Float) {
                if (slideOffset < 0.25f) {
                    // showPlayerViews()
                    // todo
                } else {
                    // hidePlayerViews()
                    // todo
                }
            }
            override fun onStateChanged(view: View, state: Int) {
                when (state) {
                    // todo
                    BottomSheetBehavior.STATE_COLLAPSED -> Unit // do nothing
                    BottomSheetBehavior.STATE_DRAGGING -> Unit // do nothing
                    BottomSheetBehavior.STATE_EXPANDED -> Unit // do nothing
                    BottomSheetBehavior.STATE_HALF_EXPANDED ->  Unit // do nothing
                    BottomSheetBehavior.STATE_SETTLING -> Unit // do nothing
                    BottomSheetBehavior.STATE_HIDDEN -> showPlayer(rootView.context)
                }
            }
        })
        // toggle collapsed state on tap
        bottomSheet.setOnClickListener { toggleBottomSheetState() }
        stationImageView.setOnClickListener { toggleBottomSheetState() }
        stationNameView.setOnClickListener { toggleBottomSheetState() }
        metadataView.setOnClickListener { toggleBottomSheetState() }
    }


    /* Toggle expanded/collapsed state of bottom sheet */
    private fun toggleBottomSheetState() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }


    /*
     * Inner class: Custom LinearLayoutManager
     */
    private inner class CustomLayoutManager(context: Context): LinearLayoutManager(context, VERTICAL, false) {
        override fun supportsPredictiveItemAnimations(): Boolean {
            return true
        }
    }
    /*
     * End of inner class
     */


}