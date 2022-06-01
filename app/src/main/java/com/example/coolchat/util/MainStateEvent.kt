package com.example.coolchat.util

sealed class MainStateEvent {
    object GetUserEvents: MainStateEvent()
    object None:MainStateEvent()
}