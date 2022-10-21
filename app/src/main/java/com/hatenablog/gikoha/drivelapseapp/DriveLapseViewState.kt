package com.hatenablog.gikoha.drivelapseapp

data class DriveLapseViewState
    (
    val items: List<DriveLapse>?
)
{
    companion object
    {
        val EMPTY = DriveLapseViewState(null)
    }
}