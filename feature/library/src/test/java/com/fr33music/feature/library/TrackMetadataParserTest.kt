package com.fr33music.feature.library

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackMetadataParserTest {

    @Test
    fun parse_vacations_fooling_you_official_lyric_video() {
        val (title, artist) = TrackMetadataParser.parse(
            rawTitle = "Vacations - Fooling You (Official Lyric Video)",
            rawArtist = "Vacations"
        )
        assertEquals("Fooling You", title)
        assertEquals("Vacations", artist)
    }

    @Test
    fun parse_bad_bunny_dakiti_feat_protects_collaboration() {
        val (title, artist) = TrackMetadataParser.parse(
            rawTitle = "Bad Bunny - DÁKITI (feat. Jhay Cortez) (Official Video)",
            rawArtist = "Bad Bunny"
        )
        assertEquals("DÁKITI (feat. Jhay Cortez)", title)
        assertEquals("Bad Bunny", artist)
    }

    @Test
    fun parse_rosalia_con_collaboration_preserved() {
        val (title, artist) = TrackMetadataParser.parse(
            rawTitle = "Rosalía - Con Altura (con J Balvin ft. El Guincho) [Official Video]",
            rawArtist = "Rosalía"
        )
        assertEquals("Con Altura (con J Balvin ft. El Guincho)", title)
        assertEquals("Rosalía", artist)
    }

    @Test
    fun parse_queen_bohemian_rhapsody_oficial_with_trailing_spaces() {
        val (title, artist) = TrackMetadataParser.parse(
            rawTitle = "Queen - Bohemian Rhapsody (Oficial )",
            rawArtist = "Queen"
        )
        assertEquals("Bohemian Rhapsody", title)
        assertEquals("Queen", artist)
    }

    @Test
    fun parse_audio_oficial_brackets_removed() {
        val (title, artist) = TrackMetadataParser.parse(
            rawTitle = "Song Name [Audio Oficial]",
            rawArtist = "Artist"
        )
        assertEquals("Song Name", title)
        assertEquals("Artist", artist)
    }

    @Test
    fun parse_title_with_no_artist_extracts_from_dash() {
        val (title, artist) = TrackMetadataParser.parse(
            rawTitle = "Vacations - Fooling You (Official Video)",
            rawArtist = "Desconocido"
        )
        assertEquals("Fooling You", title)
        assertEquals("Vacations", artist)
    }

    @Test
    fun parse_artist_not_duplicated() {
        val (title, artist) = TrackMetadataParser.parse(
            rawTitle = "Vacations - Fooling You",
            rawArtist = "Vacations"
        )
        assertEquals("Fooling You", title)
        assertEquals("Vacations", artist)
    }
}
