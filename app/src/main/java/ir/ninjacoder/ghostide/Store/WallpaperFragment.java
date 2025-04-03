package ir.ninjacoder.ghostide.Store;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import androidx.core.content.FileProvider;
import com.google.android.material.color.MaterialColors;
import ir.ninjacoder.ghostide.marco.FileShareManager;
import ir.ninjacoder.ghostide.utils.ObjectUtils;
import java.io.File;
import android.os.Environment;
import java.io.FileOutputStream;
import android.media.MediaScannerConnection;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.downloader.databinding.LayoutCustomimagepreviewBinding;
import com.downloader.databinding.LayoutWallsBinding;
import com.downloader.databinding.OneRvBinding;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import ir.ninjacoder.ghostide.RequestNetwork;
import ir.ninjacoder.ghostide.RequestNetworkController;
import ir.ninjacoder.ghostide.glidecompat.GlideCompat;
import ir.ninjacoder.ghostide.utils.AnimUtils;
import ir.ninjacoder.ghostide.utils.FileUtil;
import ir.ninjacoder.prograsssheet.CustomSheet;
import java.io.InputStreamReader;
import java.util.ArrayList;
import ir.ninjacoder.ghostide.R;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WallpaperFragment extends Fragment {
  private OneRvBinding bi;
  private List<Map<String, String>> listAllImage = new ArrayList<>();
  private RequestNetwork setDataForGithub;
  private RequestNetwork.RequestListener callback;
  private LayoutCustomimagepreviewBinding imgbind;

  @Override
  @MainThread
  @Nullable
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    bi = OneRvBinding.inflate(inflater, container, false);
    return bi.getRoot();
  }

  @Override
  @MainThread
  public void onViewCreated(View view, Bundle arg1) {
    super.onViewCreated(view, arg1);
    setDataForGithub = new RequestNetwork((Activity) getContext());

    callback =
        new RequestNetwork.RequestListener() {
          @Override
          public void onResponse(String tt, String response, HashMap<String, Object> ma) {

            try {
              listAllImage =
                  new Gson()
                      .fromJson(response, new TypeToken<List<Map<String, String>>>() {}.getType());
              bi.rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
              bi.rv.setAdapter(new WallAd(listAllImage));
            } catch (Exception e) {
              Toast.makeText(getContext(), e.getMessage(), 2).show();
            }
          }

          @Override
          public void onErrorResponse(String param1, String param2) {
            Toast.makeText(getContext(), "Error you offilne please see netWork", 2).show();
          }
        };
    setDataForGithub.startRequestNetwork(
        RequestNetworkController.GET,
        "https://raw.githubusercontent.com/HanzoDev1375/ghostidewallpapaer/refs/heads/main/github_images.json",
        "",
        callback);
  }

  class WallAd extends RecyclerView.Adapter<WallAd.VH> {
    private List<Map<String, String>> list;

    public WallAd(List<Map<String, String>> list) {
      this.list = list;
    }

    class VH extends RecyclerView.ViewHolder {
      LayoutWallsBinding binding; // اتصال داده‌ها به ViewHolder

      public VH(LayoutWallsBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
      }
    }

    @Override
    public int getItemCount() {
      return list.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
      // ایجاد binding جدید برای هر ViewHolder
      LayoutWallsBinding binding =
          LayoutWallsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
      return new VH(binding);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
      // دسترسی به binding مخصوص این ViewHolder
      LayoutWallsBinding binding = holder.binding;

      Glide.with(binding.icon.getContext())
          .load(list.get(position).get("image")) // نیازی به Uri.parse نیست اگر لینک است
          .placeholder(R.drawable.share)
          .error(R.drawable.errorxml)
          .fitCenter()
          .diskCacheStrategy(DiskCacheStrategy.ALL) // بهتر است کش شود
          .into(binding.icon);

      binding
          .getRoot()
          .setOnClickListener(
              v -> {
                setSheet(holder.getAdapterPosition());
              });
    }

    void setSheet(int pos) {
      imgbind = LayoutCustomimagepreviewBinding.inflate(LayoutInflater.from(getContext()));
      var sheet = new Sheet(getContext());
      sheet.show();
      sheet.setFullScreen();
      Glide.with(imgbind.backgroundImage.getContext())
          .load(list.get(pos).get("image"))
          .diskCacheStrategy(DiskCacheStrategy.NONE)
          .into(imgbind.backgroundImage);
      imgbind.fab1.setImageResource(R.drawable.save);
      imgbind.fab2.setImageResource(R.drawable.ic_material_image);
      imgbind.fab2.setColorFilter(
          MaterialColors.getColor(imgbind.fab2, ObjectUtils.ColorFilter, 0));
      imgbind.fab1.setColorFilter(
          MaterialColors.getColor(imgbind.fab1, ObjectUtils.ColorFilter, 0));
      imgbind.fab3.setIconResource(R.drawable.share);
      imgbind.fab3.setIconTint(
          ColorStateList.valueOf(
              MaterialColors.getColor(imgbind.fab3, ObjectUtils.ColorFilter, 0)));

      imgbind.fab3.setOnClickListener(
          v -> {
            String imageUrl = list.get(pos).get("image");
            String fileName = getFileNameFromUrl(imageUrl);
            Drawable drawable = imgbind.backgroundImage.getDrawable();

            if (drawable instanceof BitmapDrawable) {
              Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
              if (bitmap != null) {
                saveAndShareImage(bitmap, fileName);
              }
            }
          });
      imgbind.fab1.setOnClickListener(
          v -> {
            String imageUrl = list.get(pos).get("image");
            String fileName = getFileNameFromUrl(imageUrl);
            Drawable drawable = imgbind.backgroundImage.getDrawable();
            if (drawable instanceof BitmapDrawable) {
              Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
              if (bitmap != null) {
                saveBitmapWithCustomName(bitmap, fileName);
              }
            }
          });

      imgbind.fab2.setOnClickListener(
          v -> {
            String imageUrl = list.get(pos).get("image");
            String fileName = getFileNameFromUrl(imageUrl);

            Drawable drawable = imgbind.backgroundImage.getDrawable();
            if (drawable instanceof BitmapDrawable) {
              Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
              if (bitmap != null) {
                saveAndSetWallpaper(bitmap, fileName);
              }
            }
          });
    }
  }

  class Sheet extends CustomSheet {
    public Sheet(Context c) {
      super(c);
    }

    @Override
    public View getView() {
      return imgbind.getRoot();
    }
  }

  private String getFileNameFromUrl(String url) {
    try {

      String baseName = url.substring(url.lastIndexOf('/') + 1);

      String fileName = baseName.split("\\?")[0];
      if (fileName.matches(".+\\.(jpg|jpeg|png|webp|gif)$")) {
        return fileName;
      } else {
        return "wallpaper_" + System.currentTimeMillis() + ".png";
      }
    } catch (Exception e) {
      return "wallpaper_" + System.currentTimeMillis() + ".png";
    }
  }

  private void saveBitmapWithCustomName(Bitmap bitmap, String fileName) {
    File dir =
        new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "GhostIDE");

    if (!dir.exists()) {
      dir.mkdirs();
    }

    File outputFile = new File(dir, fileName);

    try (FileOutputStream out = new FileOutputStream(outputFile)) {
      
      Bitmap.CompressFormat format;
      if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
        format = Bitmap.CompressFormat.JPEG;
      } else if (fileName.toLowerCase().endsWith(".webp")) {
        format = Bitmap.CompressFormat.WEBP;
      } else {
        format = Bitmap.CompressFormat.PNG; // پیش‌فرض
      }

      bitmap.compress(format, 100, out);
      Toast.makeText(getContext(), "ذخیره شد: " + outputFile.getName(), Toast.LENGTH_LONG).show();

      MediaScannerConnection.scanFile(
          getContext(),
          new String[] {outputFile.getAbsolutePath()},
          new String[] {getMimeType(fileName)},
          null);
    } catch (Exception e) {
      Toast.makeText(getContext(), "خطا: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  // تشخیص Mime Type بر اساس پسوند فایل
  private String getMimeType(String fileName) {
    if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (fileName.toLowerCase().endsWith(".png")) {
      return "image/png";
    } else if (fileName.toLowerCase().endsWith(".webp")) {
      return "image/webp";
    } else if (fileName.toLowerCase().endsWith(".gif")) {
      return "image/gif";
    }
    return "image/*";
  }

  private void saveAndSetWallpaper(Bitmap bitmap, String fileName) {
    ThreadUtils.executeByIo(
        new ThreadUtils.SimpleTask<File>() {
          @Override
          public File doInBackground() throws Throwable {
            File dir =
                new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "GhostIDE/Wallpapers");

            if (!dir.exists()) {
              dir.mkdirs();
            }

            File outputFile = new File(dir, fileName);

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
              bitmap.compress(getFormatFromName(fileName), 100, out);
              return outputFile;
            }
          }

          @Override
          public void onSuccess(File result) {
            
            SharedPreferences pref =
                getContext().getSharedPreferences("getvb", Context.MODE_PRIVATE);
            pref.edit().putString("dir", result.getAbsolutePath()).apply();

            ThreadUtils.runOnUiThread(
                () -> {
                  Toast.makeText(getContext(), "والپیپر ذخیره و تنظیم شد", Toast.LENGTH_SHORT)
                      .show();
                });
          }

          @Override
          public void onFail(Throwable t) {
            ThreadUtils.runOnUiThread(
                () ->
                    Toast.makeText(getContext(), "خطا: " + t.getMessage(), Toast.LENGTH_LONG)
                        .show());
          }
        });
  }

  private Bitmap.CompressFormat getFormatFromName(String fileName) {

    String lowerCaseName = fileName.toLowerCase();

    if (lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg")) {
      return Bitmap.CompressFormat.JPEG;
    } else if (lowerCaseName.endsWith(".webp")) {
      return Bitmap.CompressFormat.WEBP;
    } else if (lowerCaseName.endsWith(".png")) {
      return Bitmap.CompressFormat.PNG;
    } else {
      return Bitmap.CompressFormat.JPEG;
    }
  }

  private void saveAndShareImage(Bitmap bitmap, String fileName) {
    ThreadUtils.executeByIo(
        new ThreadUtils.SimpleTask<File>() {
          @Override
          public File doInBackground() throws Throwable {
            File cacheDir = new File(getContext().getCacheDir(), "share_images");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File outputFile = new File(cacheDir, fileName);
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
              bitmap.compress(getFormatFromName(fileName), 100, out);
              return outputFile;
            }
          }

          @Override
          public void onSuccess(File result) {
            FileShareManager share = new FileShareManager(getContext());
            share.shareFile(result);
          }

          @Override
          public void onFail(Throwable t) {
            ThreadUtils.runOnUiThread(
                () ->
                    Toast.makeText(getContext(), "خطا در اشتراک گذاری", Toast.LENGTH_SHORT).show());
          }
        });
  }
}
