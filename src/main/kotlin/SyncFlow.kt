import kotlin.system.measureTimeMillis

/**
 * Sequence是一种新的抽象, 表示一种序列流.
 * 它和list其实是不一样的.List表示已经确定的一个列表.
 * 而Sequence表示来种序列流,不知道内容是什么.只是一个一个的产生而已.
 *
 * 以下这种是同步流,如果迭代, 会阻塞调用线程
 *
 */
fun simple(): Sequence<Int> = sequence { // sequence builder
    for (i in 1..3) {
        Thread.sleep(1000) // pretend we are computing it
        yield(i) // yield next value
    }
}

fun main() {
    val execTime = measureTimeMillis {
        simple().forEach { println(it) }
    }

    println(execTime)
}