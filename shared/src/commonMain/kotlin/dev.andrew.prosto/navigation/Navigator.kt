package dev.andrew.prosto.navigation

interface Navigator<T> {
    var onDestinationChanged: (state: T) -> Unit
    var onBackPressed: () -> Unit
    fun navigateTo(state: T)
    fun navigateBack()
}