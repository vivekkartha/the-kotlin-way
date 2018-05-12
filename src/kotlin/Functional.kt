package kotlin

class Functional {
    val songList = mutableListOf<Song>()

    /**
     *Non-functional tedious approach*
     */
    fun getSongsByArtist(artist:String=""):MutableList<Song>{
        val filteredSongs= mutableListOf<Song>()
        for(song in songList){
            if (song.name==artist){
                filteredSongs.add(song)
            }
        }
        return filteredSongs
    }

    /**The Kotlin way of doing it. As simple as doing it in a single line.
     *  Makes your code cleaner,readable and less-prone to bugs
     */
    fun getSongsByArtistK(artist:String)= songList.filter { it.name == artist }

}