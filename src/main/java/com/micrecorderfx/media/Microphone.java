package com.micrecorderfx.media;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class Microphone {

    private String description;
    private String name;
    private AudioFormatEnum format = AudioFormatEnum.WAVE;

    public AudioFormatEnum getFormat() {
        return format;
    }

    public void setFormat(AudioFormatEnum format) {
        this.format = format;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
