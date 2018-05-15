package kotlinstuff

class MoreKotlin {
    private var songList = mutableListOf<Song>()

    //The usual way
    fun getFavorite(): Song {
        val song = Song()
        song.name = "Rolling in the deep"
        song.artist = "Adele"
        song.bitrate = 128f
        return song
    }

    //The Kotlin way
    fun getFav() = with(Song()) {
        name = "Rolling in the deep"
        artist = "Adele"
        bitrate = 128f
    }

    //Cleaner maps
    fun myMap() {
        //The usual map
        val myMap = HashMap<String, Int>()
        myMap.put("a1", 1)
        myMap.put("a2", 2)
        myMap.put("a3", 3)

        //The kotlin way
        val myBetterMap = mapOf(
                "a1" to 1,
                "a2" to 2,
                "a3" to 3
        )
    }

    fun concatenation() {
        val a = "alpha"
        val b = "beta"
        val c = "gamma"
        //the old way
        println("a=" + a + ",b=" + b + ",c=" + c)
        //the kotlin way
        println("a=$a,b=$b,c=$c")
    }
}