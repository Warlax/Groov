package calex.groov.worker;

import android.os.Environment;
import android.support.annotation.NonNull;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.Worker;
import calex.groov.app.GroovApplication;
import calex.groov.constant.Keys;
import calex.groov.data.RepSet;
import calex.groov.model.GroovRepository;

public class ExportWorker extends Worker {

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);

  @Inject GroovRepository repository;

  @NonNull
  @Override
  public Result doWork() {
    ((GroovApplication) getApplicationContext()).getComponent().inject(this);
    List<RepSet> sets = repository.blockingAllSets();
    List<String[]> lines = new ArrayList<>(sets.size());
    for (RepSet set : sets) {
      lines.add(new String[] {
          DATE_FORMAT.format(set.getDate()),
          Integer.toString(set.getReps()),
      });
    }

    String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        .getAbsolutePath();
    String fileName = "groov.csv";
    String filePath = baseDir + File.separator + fileName;
    File file = new File(filePath);
    if (!file.exists()) {
      try {
        file.getParentFile().mkdirs();
        if (!file.createNewFile()) {
          return Result.FAILURE;
        }
      } catch (IOException e) {
        e.printStackTrace();
        return Result.FAILURE;
      }
    }
    CSVWriter writer = null;
    try {
      writer = new CSVWriter(new FileWriter(file));
      writer.writeAll(lines);
    } catch (IOException e) {
      e.printStackTrace();
      return Result.FAILURE;
    } finally {
      try {
        if (writer != null) {
          writer.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    setOutputData(new Data.Builder().putString(Keys.PATH, file.getAbsolutePath()).build());
    return Result.SUCCESS;
  }
}
