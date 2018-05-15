package kotlinstuff

private val supportedGenres = arrayListOf("pop", "rock", "hip-hop", "jazz")

/**Extension functions extend the functionality of various classes.
 * Here String class is added with an additional function isGenreAllowed()
 * without modifying the String class itself.
 * @return Boolean
 * this - is the arg object of type String.
 **/
fun String.isGenreAllowed() = supportedGenres.contains(this)

fun main(args: Array<String>) {
    if ("pop".isGenreAllowed()) {
        println("Pop is allowed to be played here")
    } else {
        println("Na")
    }
}