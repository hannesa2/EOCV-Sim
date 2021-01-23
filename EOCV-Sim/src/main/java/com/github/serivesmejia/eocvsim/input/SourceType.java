/*
 * Copyright (c) 2021 Sebastian Erives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.github.serivesmejia.eocvsim.input;

import com.github.serivesmejia.eocvsim.input.source.CameraSource;
import com.github.serivesmejia.eocvsim.input.source.ImageSource;
import com.github.serivesmejia.eocvsim.input.source.VideoSource;

public enum SourceType {

    IMAGE(ImageSource.class, "Image"),
    CAMERA(CameraSource.class, "Camera"),
    VIDEO(VideoSource.class, "Video"),
    UNKNOWN(null, "Unknown");

    public final Class<? extends InputSource> klazz;
    public final String coolName;

    SourceType(Class<? extends InputSource> klazz, String coolName) {
        this.klazz = klazz;
        this.coolName = coolName;
    }

    public static SourceType fromClass(Class<? extends InputSource> clazz) {
        for(SourceType sourceType : values()) {
            if(sourceType.klazz == clazz) {
                return sourceType;
            }
        }
        return UNKNOWN;
    }

    public static SourceType fromCoolName(String coolName) {
        for(SourceType sourceType : values()) {
            if(sourceType.coolName == coolName) {
                return sourceType;
            }
        }
        return UNKNOWN;
    }

}
