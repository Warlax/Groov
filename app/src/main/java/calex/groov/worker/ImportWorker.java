package calex.groov.worker;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.work.Worker;
import calex.groov.app.GroovApplication;
import calex.groov.constant.Keys;
import calex.groov.data.RepSet;
import calex.groov.model.GroovRepository;

public class ImportWorker extends Worker {

  @Inject GroovRepository repository;

  @NonNull
  @Override
  public Result doWork() {
    ((GroovApplication) getApplicationContext()).getComponent().inject(this);

    String path = getInputData().getString(Keys.PATH, null);
    if (path == null) {
      return Result.FAILURE;
    }

    Uri uri = Uri.parse(path);
    try {
      CSVReader csvReader = new CSVReader(
          new InputStreamReader(getApplicationContext().getContentResolver().openInputStream(uri)));
      List<String[]> lines = csvReader.readAll();
      List<RepSet> sets = new ArrayList<>(lines.size());
      for (String[] line : lines) {
        RepSet set = new RepSet();
        set.setDate(Date.from(Instant.ofEpochMilli(Long.parseLong(line[0]))));
        set.setReps(Integer.parseInt(line[1]));
        sets.add(set);
      }
      repository.insertSets(sets);
      return Result.SUCCESS;
    } catch (IOException e) {
      e.printStackTrace();
      return Result.FAILURE;
    }
  }
}
