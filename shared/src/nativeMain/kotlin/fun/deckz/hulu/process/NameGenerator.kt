package `fun`.deckz.hulu.process

object NameGenerator {
    private val alphaPool = ('a'..'z')

    fun randomAlphaName(count: Int): String {
        val result = ""
        repeat(count) {
            result + alphaPool.random()
        }
        return result
    }
}