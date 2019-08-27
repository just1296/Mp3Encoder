package com.jiazhu.demo.encoder;

public class Mp3EncoderTwo {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native static int init(String pcmFile, int audioChannels, int bitRate, int sampleRate, String mp3Path);

    public native static void encode();

    public native static void destroy();
}
