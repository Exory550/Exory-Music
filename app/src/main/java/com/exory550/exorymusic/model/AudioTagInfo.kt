package com.exory550.exorymusic.model

import org.jaudiotagger.tag.FieldKey

class AudioTagInfo(
    val filePaths: List<String>?,
    val fieldKeyValueMap: Map<FieldKey, String>?,
    val artworkInfo: ArtworkInfo?
)
