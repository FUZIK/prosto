package dev.andrew.prosto.navigation

class ProstoNavigator: Navigator<ProstoDestination> {
    override var onDestinationChanged = { _: ProstoDestination -> }
    override var onBackPressed = {}
    override fun navigateTo(state: ProstoDestination) {
        when (state) {
            is ProstoDestination.OnBackPressed -> navigateBack()
            else -> onDestinationChanged(state)
        }
    }
    override fun navigateBack() {
        onBackPressed()
    }
}