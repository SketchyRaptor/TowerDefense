// src/io/Saveable.java
package io;

public interface Saveable {
    String toSaveString();
    void fromSaveString(String data);
}