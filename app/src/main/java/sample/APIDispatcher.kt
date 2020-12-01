package sample

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val APIDispatcher: CoroutineDispatcher = Dispatchers.Default