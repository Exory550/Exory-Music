package com.exory550.exorymusic.glide.audiocover;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AudioFileCoverUtils {

  public static final String[] FALLBACKS = {
    "cover.jpg", "album.jpg", "folder.jpg", "cover.png", "album.png", "folder.png"
  };

  public static InputStream fallback(String path) throws FileNotFoundException {
    try {
      MP3File mp3File = new MP3File(path);
      if (mp3File.hasID3v2Tag()) {
        Artwork art = mp3File.getTag().getFirstArtwork();
        if (art != null) {
          byte[] imageData = art.getBinaryData();
          return new ByteArrayInputStream(imageData);
        }
      }
    } catch (ReadOnlyFileException | InvalidAudioFrameException | TagException | IOException | CannotReadException ignored) {
    }

    final File parent = new File(path).getParentFile();
    for (String fallback : FALLBACKS) {
      File cover = new File(parent, fallback);
      if (cover.exists()) {
        return new FileInputStream(cover);
      }
    }
    return null;
  }
}
