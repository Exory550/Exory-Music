package com.exory550.exorymusic.util;

public class TempUtils {

  public static final int TEMPO_STROLL = 0;
  public static final int TEMPO_WALK = 1;
  public static final int TEMPO_LIGHT_JOG = 2;
  public static final int TEMPO_JOG = 3;
  public static final int TEMPO_RUN = 4;
  public static final int TEMPO_SPRINT = 5;
  public static final int TEMPO_UNKNOWN = 6;

  public static int getTempoFromBPM(int bpm) {

    if (bpm < 60) {
      return TEMPO_STROLL;
    }

    else if (bpm < 70 || bpm >= 120 && bpm < 140) {
      return TEMPO_WALK;
    }

    else if (bpm < 80 || bpm >= 140 && bpm < 160) {
      return TEMPO_LIGHT_JOG;
    }

    else if (bpm < 90 || bpm >= 160 && bpm < 180) {
      return TEMPO_JOG;
    }

    else if (bpm < 100 || bpm >= 180 && bpm < 200) {
      return TEMPO_RUN;
    }

    else if (bpm < 120) {
      return TEMPO_SPRINT;
    }

    else {
      return TEMPO_UNKNOWN;
    }
  }

  public static int getTempoFromBPM(String bpm) {
    try {
      return getTempoFromBPM(Integer.parseInt(bpm.trim()));
    } catch (NumberFormatException nfe) {
      return TEMPO_UNKNOWN;
    }
  }
}
