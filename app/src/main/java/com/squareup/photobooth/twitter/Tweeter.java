package com.squareup.photobooth.twitter;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

public class Tweeter {

  public String uploadPicture(byte[] jpegBytes) throws IOException {
    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
    MediaService mediaService = twitterApiClient.getMediaService();

    RequestBody media = RequestBody.create(MediaType.parse("image/jpeg"), jpegBytes);
    Response<Media> uploadResponse = mediaService.upload(media, null, null).execute();

    if (!uploadResponse.isSuccessful()) {
      throw new IOException("Upload not successful");
    }
    return uploadResponse.body().mediaIdString;
  }

  public void tweet(String message, String mediaId) throws IOException {
    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
    StatusesService statusesService = twitterApiClient.getStatusesService();
    Response<Tweet> tweetResponse =
        statusesService.update(message, null, false, null, null, null, null, false, mediaId)
            .execute();
    if (!tweetResponse.isSuccessful()) {
      throw new IOException("Tweet not successful");
    }
  }

  public boolean isLoggedIn() {
    return TwitterCore.getInstance().getSessionManager().getActiveSession() != null;
  }
}
