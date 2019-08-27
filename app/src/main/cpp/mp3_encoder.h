//
// Created by zhaojiazhu on 2019-08-24.
//

#ifndef AUDIO_MP3_ENCODER_H
#define AUDIO_MP3_ENCODER_H


#include <cstdio>
#include <lame.h>

class mp3_encoder {
private:
    FILE* pcmFile;
    FILE* mp3File;
    lame_t lameClient;
public:
    mp3_encoder();
    ~mp3_encoder();
    int Init(const char* pcmFilePath, const char *mp3File, int sampleRate, int channel, int bitRate);
    void Encode();
    void Destroy();

};


#endif //AUDIO_MP3_ENCODER_H
