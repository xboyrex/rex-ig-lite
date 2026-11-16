/*
 * Copyright (C) 2026 xboyrex <https://github.com/xboyrex/rex-ig-lite>
 *
 * See the included NOTICE file for GPLv3 §7(b) terms that apply to this code.
 */


package app.morphe.extension.instagram.entity;

import app.morphe.extension.rex.downloader.MediaType;

public class VideoData extends Entity implements VideoVariant {
    public VideoData(Object obj) {
        super(obj);
    }

    public Integer getHeight() throws Exception {
        try {
            Integer height = (Integer) super.getMethod("methodname");
            return height != null ? height : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    public Integer getWidth() throws Exception {
        try {
            Integer width = (Integer) super.getMethod("methodname");
            return width != null ? width : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private Integer getCodec() throws Exception {
        try {
            Integer codec = (Integer) super.getMethod("methodname");
            return codec != null ? codec : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    public String getVariantTag() {
        try{
            return this.getHeight()+"x"+this.getWidth()+"-"+this.getCodec();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String getUrl() throws Exception {
        try {
            String url = (String) super.getMethod("methodname");
            if (url != null) return url;
        } catch (Exception ignored) {
        }
        return (String) super.getMethod("getUrl");
    }

    public MediaType getMediaType(){
        return MediaType.VIDEO;
    }

}
