package com.squareup.photobooth.snap.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;
import com.squareup.photobooth.R;
import java.io.IOException;
import timber.log.Timber;

public class CameraSourcePreview extends ViewGroup {

  private final Context context;
  private final SurfaceView surfaceView;
  private final View cropLayer;
  private final GraphicOverlay graphicOverlay;

  private boolean startRequested;
  private boolean surfaceAvailable;
  private CameraSource cameraSource;
  private GraphicOverlay overlay;
  private float requiredRatio;

  public CameraSourcePreview(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    startRequested = false;
    surfaceAvailable = false;

    surfaceView = new SurfaceView(context);
    surfaceView.getHolder().addCallback(new SurfaceCallback());
    addView(surfaceView);
    cropLayer = new View(context);
    cropLayer.setBackgroundColor(context.getResources().getColor(R.color.background_color));
    addView(cropLayer);

    graphicOverlay = new GraphicOverlay(context);
    addView(graphicOverlay);
  }

  public GraphicOverlay getGraphicOverlay() {
    return graphicOverlay;
  }

  public void start(CameraSource cameraSource, GraphicOverlay overlay, float requiredRatio) {
    this.requiredRatio = requiredRatio;
    this.overlay = overlay;
    this.cameraSource = cameraSource;
    startRequested = true;
    startIfReady();
  }

  public void stop() {
    if (cameraSource != null) {
      cameraSource.stop();
    }
  }

  // Permission checking is handled by the activity.
  @SuppressLint("MissingPermission") //
  private void startIfReady() {
    if (startRequested && surfaceAvailable) {
      boolean noPreviewSize = cameraSource.getPreviewSize() == null;
      try {
        cameraSource.start(surfaceView.getHolder());
      } catch (IOException e) {
        cameraSource.release();
        Timber.d("Could not start camera source.", e);
        return;
      }
      if (noPreviewSize) {
        // We called start() so we now have a preview size, ready to layout correctly.
        requestLayout();
      }
      if (overlay != null) {
        Size size = cameraSource.getPreviewSize();
        int min = Math.min(size.getWidth(), size.getHeight());
        int max = Math.max(size.getWidth(), size.getHeight());
        if (isPortraitMode()) {
          // Swap width and height sizes when in portrait, since it will be rotated by
          // 90 degrees
          overlay.setCameraInfo(min, max, cameraSource.getCameraFacing());
        } else {
          overlay.setCameraInfo(max, min, cameraSource.getCameraFacing());
        }
        overlay.clear();
      }
      startRequested = false;
    }
  }

  private class SurfaceCallback implements SurfaceHolder.Callback {
    @Override public void surfaceCreated(SurfaceHolder surface) {
      surfaceAvailable = true;
      startIfReady();
    }

    @Override public void surfaceDestroyed(SurfaceHolder surface) {
      surfaceAvailable = false;
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int layoutWidth = right - left;
    int layoutHeight = bottom - top;

    Size size = null;
    if (cameraSource != null) {
      size = cameraSource.getPreviewSize();
    }
    if (size == null) {
      // This triggers the surface creation
      surfaceView.layout(0, 0, layoutWidth, layoutHeight);
      // We don't know the camera preview size because we haven't started yet, so we can't layout.
      return;
    }

    int previewWidth = size.getWidth();
    int previewHeight = size.getHeight();

    // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
    float requiredRatio;
    if (isPortraitMode()) {
      int tmp = previewWidth;
      //noinspection SuspiciousNameCombination
      previewWidth = previewHeight;
      previewHeight = tmp;
      requiredRatio = 1 / this.requiredRatio;
    } else {
      requiredRatio = this.requiredRatio;
    }

    // We're dealing with 3 rectangles with distinct ratios here: the layout, the camera, and
    // the printed photo.

    float previewRatio = (previewWidth * 1f) / previewHeight;
    float layoutRatio = (layoutWidth * 1f) / layoutHeight;

    boolean previewHasLargerWidth = previewRatio > requiredRatio;
    boolean layoutHasLargerWidth = layoutRatio > requiredRatio;

    int displayedSurfaceWidth;
    int displayedSurfaceHeight;
    if (layoutHasLargerWidth) {
      displayedSurfaceWidth = (int) (requiredRatio * layoutHeight);
      displayedSurfaceHeight = layoutHeight;
    } else {
      displayedSurfaceWidth = layoutWidth;
      displayedSurfaceHeight = (int) (layoutWidth / requiredRatio);
    }

    if (layoutHasLargerWidth) {
      if (previewHasLargerWidth) {
        cropLayer.layout(displayedSurfaceWidth + 1, 0, layoutWidth, displayedSurfaceHeight);
      } else {
        // no need for crop layer
        cropLayer.layout(0, 0, 0, 0);
      }
    } else {
      if (previewHasLargerWidth) {
        // no need for crop layer
        cropLayer.layout(0, 0, 0, 0);
      } else {
        cropLayer.layout(0, displayedSurfaceHeight + 1, layoutWidth, layoutHeight);
      }
    }

    if (previewHasLargerWidth) {
      int surfaceWidth = (int) (previewRatio * displayedSurfaceHeight);
      int surfaceHeight = displayedSurfaceHeight;
      int leftOffset = (surfaceWidth - displayedSurfaceWidth) / 2;
      int surfaceLeft = -leftOffset;
      int surfaceRight = surfaceLeft + surfaceWidth;
      surfaceView.layout(surfaceLeft, 0, surfaceRight, surfaceHeight);
      graphicOverlay.layout(surfaceLeft, 0, surfaceRight, surfaceHeight);
    } else {
      int surfaceWidth = displayedSurfaceWidth;
      int surfaceHeight = (int) (displayedSurfaceWidth / previewRatio);
      int topOffset = (surfaceHeight - displayedSurfaceHeight) / 2;
      int surfaceTop = -topOffset;
      int surfaceBottom = surfaceTop + surfaceHeight;
      surfaceView.layout(0, surfaceTop, surfaceWidth, surfaceBottom);
      graphicOverlay.layout(0, surfaceTop, surfaceWidth, surfaceBottom);
    }
    startIfReady();
  }

  private boolean isPortraitMode() {
    int orientation = context.getResources().getConfiguration().orientation;
    return orientation == Configuration.ORIENTATION_PORTRAIT;
  }
}
