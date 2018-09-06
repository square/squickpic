package com.squareup.photobooth.snap.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import com.google.android.gms.vision.CameraSource;
import com.squareup.photobooth.snap.Graphic;
import com.squareup.photobooth.snap.Scaler;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview).  The creator can add graphics objects, update the objects, and
 * update them, triggering the appropriate drawing and invalidation within the view.<p>
 *
 * Supports scaling and mirroring of the graphics relative the camera's preview properties.  The
 * idea is that detection items are expressed in terms of a preview size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.<p>
 */
public class GraphicOverlay extends View implements Scaler {
  private final Object lock = new Object();
  private final Set<Graphic> graphics = new LinkedHashSet<>();
  private int previewWidth;
  private float widthScaleFactor = 1.0f;
  private int previewHeight;
  private float heightScaleFactor = 1.0f;
  private int facing = CameraSource.CAMERA_FACING_BACK;

  @Override public float scaleHorizontal(float horizontal) {
    return horizontal * widthScaleFactor;
  }

  @Override public float scaleVertical(float vertical) {
    return vertical * heightScaleFactor;
  }

  @Override public boolean isFrontFacing() {
    return facing == CameraSource.CAMERA_FACING_FRONT;
  }

  @Override public float translateX(float x) {
    if (facing == CameraSource.CAMERA_FACING_FRONT) {
      return getWidth() - scaleHorizontal(x);
    } else {
      return scaleHorizontal(x);
    }
  }

  @Override public float translateY(float y) {
    return scaleVertical(y);
  }

  public GraphicOverlay(Context context) {
    super(context);
  }

  /**
   * Removes all graphics from the overlay.
   */
  public void clear() {
    synchronized (lock) {
      graphics.clear();
    }
    postInvalidate();
  }

  /**
   * Adds a graphic to the overlay.
   */
  public void update(Graphic graphic) {
    synchronized (lock) {
      graphics.add(graphic);
    }
    postInvalidate();
  }

  /**
   * Removes a graphic from the overlay.
   */
  public void forget(Graphic graphic) {
    synchronized (lock) {
      graphics.remove(graphic);
    }
    postInvalidate();
  }

  /**
   * Sets the camera attributes for size and facing direction, which informs how to transform
   * image coordinates later.
   */
  public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
    synchronized (lock) {
      this.previewWidth = previewWidth;
      this.previewHeight = previewHeight;
      this.facing = facing;
    }
    postInvalidate();
  }

  /**
   * Draws the overlay with its associated graphic objects.
   */
  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    synchronized (lock) {
      if ((previewWidth != 0) && (previewHeight != 0)) {
        widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
        heightScaleFactor = (float) canvas.getHeight() / (float) previewHeight;
      }

      for (Graphic graphic : graphics) {
        graphic.draw(canvas);
      }
    }
  }
}
