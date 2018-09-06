package com.squareup.photobooth.printer;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CloudPrintService {

  String CONNECTION_STATUS_ALL = "ALL";

  @FormUrlEncoded //
  @POST("search") //
  Call<SearchResponse> search(@Field("connection_status") String connectionStatus,
      @Field("q") String q, @Field("use_cdd") boolean useCdd);

  @Multipart //
  @POST("submit") //
  Call<SubmitResponse> submit(@Part("printerid") RequestBody printerId,
      @Part("title") RequestBody title, @Part("ticket") RequestBody ticket,
      @Part MultipartBody.Part content, @Part("contentType") RequestBody contentType);

  class SearchResponse {
    public boolean success;
    public String message;
    public List<Printer> printers;
  }

  class SubmitResponse {
    public boolean success;
    public String message;
  }

  class Printer {
    String id;
    String name;
    String displayName;
    String description;
    String ownerId;
    String ownerName;
  }

  class Ticket {
    final String version = "1.0";
    final TicketPrint submit = new TicketPrint();
  }

  class TicketPrint {
    final TicketPrintColor color = new TicketPrintColor();
    final TicketPrintCopies copies = new TicketPrintCopies();
    final TicketMediaSize media_size = new TicketMediaSize();
    final TicketDpi dpi = new TicketDpi();
  }

  class TicketPrintColor {
    enum ColorType {STANDARD_COLOR, STANDARD_MONOCHROME}

    ColorType type = ColorType.STANDARD_COLOR;
  }

  // Defaults to max resolution of Selphy CP1300
  class TicketDpi {
    int horizontal_dpi = 300;
    int vertical_dpi = 300;
  }

  class TicketPrintCopies {
    int copies = 1;
  }

  // Defaults to card size, KC-36IP
  class TicketMediaSize {
    int width_microns = 54000;
    int height_microns = 86000;
    boolean is_continuous_feed = false;
  }
}
