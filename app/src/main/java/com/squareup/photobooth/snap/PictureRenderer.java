package com.squareup.photobooth.snap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.media.ExifInterface;
import android.util.SparseArray;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.squareup.photobooth.R;
import com.squareup.photobooth.snap.graphic.FaceGraphic;
import com.squareup.photobooth.snap.graphic.GraphicFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import timber.log.Timber;

import static android.support.media.ExifInterface.ORIENTATION_UNDEFINED;
import static android.support.media.ExifInterface.TAG_ORIENTATION;

public class PictureRenderer {

  private static final int PRINTED_WIDTH_INCH = 86;
  private static final int PRINTED_HEIGHT_INCH = 54;
  private static final int PRINTER_DPI = 300;

  /** Ratio of the KC-36IP cards, 54x86mm */
  public static final float PRINTED_CARD_RATIO = (PRINTED_WIDTH_INCH * 1.0f) / PRINTED_HEIGHT_INCH;

  private final Context context;
  private final Paint textPaint;
  private final Paint textShadowPaint;

  public PictureRenderer(Context context) {
    this.context = context.getApplicationContext();
    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setColor(context.getResources().getColor(R.color.white));
    textPaint.setTextAlign(Paint.Align.RIGHT);
    textShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textShadowPaint.setColor(context.getResources().getColor(R.color.grey));
    textShadowPaint.setTextAlign(Paint.Align.RIGHT);
  }

  public Bitmap render(byte[] data, int graphicIndex, float requiredRatio) {
    FaceGraphic faceGraphic = GraphicFactory.values()[graphicIndex].create(context);
    InputStream inputStream = new ByteArrayInputStream(data);
    int orientation;
    try {
      ExifInterface exif = new ExifInterface(inputStream);
      orientation = exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_UNDEFINED);
    } catch (IOException unexpected) {
      Timber.d(unexpected);
      orientation = ORIENTATION_UNDEFINED;
    }

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;
    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
    bitmap = rotateBitmap(bitmap, orientation);
    if (!bitmap.isMutable()) {
      // If rotation created an immutable bitmap
      Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
      bitmap.recycle();
      bitmap = mutableBitmap;
    }

    FaceDetector detector = new FaceDetector.Builder(context) //
        .setLandmarkType(FaceDetector.ALL_LANDMARKS) //
        .setClassificationType(FaceDetector.NO_CLASSIFICATIONS) //
        .setTrackingEnabled(false) //
        .setMode(FaceDetector.ACCURATE_MODE).build();

    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
    SparseArray<Face> faces = detector.detect(frame);
    Canvas canvas = new Canvas(bitmap);
    for (int i = 0; i < faces.size(); i++) {
      int key = faces.keyAt(i);
      Face face = faces.get(key);
      faceGraphic.updateFace(face);
      faceGraphic.draw(canvas, Scaler.ISO);
      faceGraphic.forgetFace();
    }

    detector.release();

    int height = bitmap.getHeight();
    int width = bitmap.getWidth();
    if (height > width) {
      requiredRatio = 1f / requiredRatio;
    }

    float bitmapRatio = (width * 1f) / height;

    boolean bitmapHasLargerWidth = bitmapRatio > requiredRatio;

    if (bitmapHasLargerWidth) {
      int requiredWidth = (int) (requiredRatio * height);
      int offset = (width - requiredWidth) / 2;
      Bitmap oldBitmap = bitmap;
      bitmap = Bitmap.createBitmap(bitmap, offset, 0, requiredWidth, height);
      oldBitmap.recycle();
    } else {
      int requiredHeight = (int) (width / requiredRatio);
      int offset = (height - requiredHeight) / 2;
      Bitmap oldBitmap = bitmap;
      bitmap = Bitmap.createBitmap(bitmap, 0, offset, width, requiredHeight);
      oldBitmap.recycle();
    }

    int newWidth = bitmap.getWidth();
    int newHeight = bitmap.getHeight();

    int smallestSide = Math.min(newWidth, newHeight);
    //int watermarkSize = smallestSide / 4;
    canvas = new Canvas(bitmap);

    int textSize = smallestSide / 16;
    int textTopMargin = textSize;
    int textRightMargin = textSize /2;
    textPaint.setTextSize(textSize);
    textShadowPaint.setTextSize(textSize);

    //canvas.drawBitmap(watermark, null, rect, null);
    int x = newWidth - textRightMargin;
    canvas.drawText("squ.re/SquickPic", x + 4, textTopMargin + 4, textShadowPaint);
    canvas.drawText("squ.re/SquickPic", x, textTopMargin, textPaint);

    int maxHeight = PRINTER_DPI * PRINTED_HEIGHT_INCH;
    if (smallestSide > maxHeight) {
      float scaleRatio = (maxHeight * 1f) / smallestSide;
      int scaledWidth = (int) (newWidth * scaleRatio);
      int scaledHeight = (int) (newHeight * scaleRatio);
      Bitmap oldBitmap = bitmap;
      bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
      oldBitmap.recycle();
    }

    return bitmap;
  }

  // https://stackoverflow.com/a/20480741/703646
  private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
    Matrix matrix = new Matrix();
    switch (orientation) {
      case ExifInterface.ORIENTATION_NORMAL:
        return bitmap;
      case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
        matrix.setScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        matrix.setRotate(180);
        break;
      case ExifInterface.ORIENTATION_FLIP_VERTICAL:
        matrix.setRotate(180);
        matrix.postScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_TRANSPOSE:
        matrix.setRotate(90);
        matrix.postScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_ROTATE_90:
        matrix.setRotate(90);
        break;
      case ExifInterface.ORIENTATION_TRANSVERSE:
        matrix.setRotate(-90);
        matrix.postScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_ROTATE_270:
        matrix.setRotate(-90);
        break;
      default:
        return bitmap;
    }
    Bitmap rotatedBitmap =
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    bitmap.recycle();

    return rotatedBitmap;
  }
}
