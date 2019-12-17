package com.example.demoapplication;

import android.content.Context;
import id.zelory.compressor.Compressor;

import java.io.File;

public class PictureUtils {

    public static String compressPic(Context context,int maxWidth, int maxHeight, String path){
        try {
            return new Compressor(context)
                    .setMaxWidth(maxWidth)
                    .setMaxHeight(maxHeight)
                    .compressToFile(new File(path)).getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
}
