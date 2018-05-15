package kotlinstuff

class Functional {
    val songList = mutableListOf<Song>()

    /**
     *Non-functional tedious approach*
     */
    fun getSongsByArtist(artist: String =""):MutableList<Song>{
        val filteredSongs= mutableListOf<Song>()
        for(song in songList){
            if (song.artist==artist){

                filteredSongs.add(song)
            }
        }
        return filteredSongs
    }

    /**The Kotlin way of doing it.
     */
    fun getSongsByArtistK(artist: String)= songList.filter { it.artist == artist }

}