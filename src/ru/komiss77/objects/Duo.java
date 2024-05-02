package ru.komiss77.objects;

public record Duo<F, S>(F key, S val) {
  @Override
  public String toString() {
    return key.toString() + ":" + val.toString();
  }
}
