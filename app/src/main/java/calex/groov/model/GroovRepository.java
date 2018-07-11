package calex.groov.model;

import javax.inject.Inject;
import javax.inject.Singleton;

import calex.groov.data.GroovDatabase;

@Singleton
public class GroovRepository {

  private final GroovDatabase database;

  @Inject
  public GroovRepository(GroovDatabase database) {
    this.database = database;
  }
}
