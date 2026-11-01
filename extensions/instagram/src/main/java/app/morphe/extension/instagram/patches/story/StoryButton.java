/*
 * Copyright (C) 2026 rex <https://github.com/xboyrex/rex-ig-lite>
 *
 * See the included NOTICE file for GPLv3 §7(b) terms that apply to this code.
*/


package app.morphe.extension.instagram.patches.story;

import static app.morphe.extension.instagram.utils.IgStr.str;

import java.util.ArrayList;
import android.content.Context;

import app.morphe.extension.instagram.utils.Pref;
import app.morphe.extension.instagram.settings.SettingsStatus;
import app.morphe.extension.instagram.entity.MediaData;
import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;
import app.morphe.extension.instagram.patches.download.DownloadUtils;
import app.morphe.extension.rex.ObjectBrowser;
import app.morphe.extension.instagram.entity.MediaData;

import com.instagram.common.session.UserSession;

public class StoryButton {
    private static boolean VIEW_STORY_MENTIONS;
    private static boolean ENABLE_DOWNLOAD;
    private static boolean ENABLE_DIRECT_DOWNLOAD;
    private static boolean DEBUG;

    static{
        VIEW_STORY_MENTIONS = Pref.viewStoryMentions() && SettingsStatus.viewStoryMentions;
        ENABLE_DOWNLOAD = Pref.enableDownload() && SettingsStatus.downloadMedia;
        ENABLE_DIRECT_DOWNLOAD = Pref.enableDirectDownload() && SettingsStatus.downloadMedia;
        DEBUG = Pref.rexDebug();
    }

    public static ArrayList addButtons(ArrayList buttonList){
        if(DEBUG){
            buttonList.add(str("rex_debug"));
        }
        if(VIEW_STORY_MENTIONS){
            buttonList.add(str("rex_view_story_mentions"));
        }
        if(ENABLE_DOWNLOAD){
            if(ENABLE_DIRECT_DOWNLOAD){
                buttonList.add(str("rex_category_download_media"));
            }else{
                buttonList.add(str("rex_download_options"));
            }
        }
        if(Pref.downloadWithExternalDownloader()){
            buttonList.add(str("rex_download_with_external_downloader"));
        }

        return buttonList;
    }

    public static boolean storyButtonAction(CharSequence buttonText, Context ctx, Object mediaObject){
        try {
            if (buttonText.equals(str("rex_view_story_mentions"))) {
                ViewStoryMentionsPatch.viewMentions(ctx, mediaObject);
                return true;
            } else if (buttonText.equals(str("rex_download_options")) || buttonText.equals(str("rex_category_download_media"))) {
                DownloadUtils.downloadPost(ctx,null,mediaObject,0);
                return true;
            } else if (buttonText.equals(str("rex_debug"))) {
                ObjectBrowser.browseObject(ctx, new MediaData(mediaObject));
                return true;
            } else if (buttonText.equals(str("rex_download_with_external_downloader"))) {
                DownloadUtils.externalDownloader(mediaObject,0);

                return true;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "Failed storyButtonAction", ex);
        }
        return false;
    }
}