/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event bus for remote control commands sent from a phone or laptop via the local web companion.
 */
object RemoteControlSignals {

    sealed interface RemoteCommand {
        object Up : RemoteCommand
        object Down : RemoteCommand
        object Left : RemoteCommand
        object Right : RemoteCommand
        object Select : RemoteCommand
        object Back : RemoteCommand
        object Home : RemoteCommand
        object PlayPause : RemoteCommand
        object VolumeUp : RemoteCommand
        object VolumeDown : RemoteCommand
        object Mute : RemoteCommand
        object ChannelUp : RemoteCommand
        object ChannelDown : RemoteCommand
        object NavLive : RemoteCommand
        object NavMovies : RemoteCommand
        object NavShows : RemoteCommand
        data class CastMedia(val url: String, val title: String, val isLive: Boolean = true) : RemoteCommand
        data class Number(val digit: Int) : RemoteCommand
        data class SearchText(val query: String) : RemoteCommand
        data class PlayChannel(val channelId: Long) : RemoteCommand
        object MouseClick : RemoteCommand
        object MouseBack : RemoteCommand
        object MouseActivate : RemoteCommand
        object MouseHide : RemoteCommand
    }

    private val _commands = MutableSharedFlow<RemoteCommand>(extraBufferCapacity = 32)
    val commands: SharedFlow<RemoteCommand> = _commands.asSharedFlow()

    fun send(command: RemoteCommand) {
        _commands.tryEmit(command)
    }
}
