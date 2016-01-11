package com.sts.model;

import com.sts.model.enums.AudioFormatEnum;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicrophoneModel {

    private String description;
    private String name;
    private AudioFormatEnum recordFormat = AudioFormatEnum.WAVE;

    public AudioFormatEnum getRecordFormat() {
        return recordFormat;
    }

    public void setRecordFormat(AudioFormatEnum recordFormat) {
        this.recordFormat = recordFormat;
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
