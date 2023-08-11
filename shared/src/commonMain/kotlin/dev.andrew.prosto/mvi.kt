package dev.andrew.prosto

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


interface Reducer<in S, in E, out T> {
    val state: T
    fun emitEvent(event: E)
    fun setState(state: S)
    fun reduce(state: S, event: E)
}

abstract class FlowReducer<S, E>(initial: S) : Reducer<S, E, StateFlow<S>> {
    private val mutableState = MutableStateFlow(initial)
    override val state: StateFlow<S>
        get() = mutableState

    override fun emitEvent(event: E) {
        reduce(mutableState.value, event)
    }

    override fun setState(state: S) {
        mutableState.tryEmit(state)
    }

    abstract override fun reduce(state: S, event: E)
}

abstract class StateUIController<S, E>(initial: S) : FlowReducer<S, E>(initial)

inline fun <S, E> StateUIController<S, E>.updateState(block: S.() -> S) {
    setState(state.value.block())
}

inline fun <S, E> StateUIController<S, E>.withState(block: S.() -> Unit) {
    with(state.value, block)
}
