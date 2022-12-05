package coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * flow是异步流的抽象
 */
fun simple() = flow { // flow builder
    for (i in 1..3) {
        delay(100) // pretend we are doing something useful here
        emit(i) // emit next value, 注意同步是yield
    }
}

fun main() = runBlocking {

    // Launch a concurrent coroutine to check if the main thread is blocked
    launch {
        for (k in 1..3) {
            println("I'm not blocked $k")
            delay(100)
        }
    }
    // Collect the flow
    // 异步流, 在这里说明不会阻塞下面的同步操作
    simple().collect { value -> println(value) }
}
