package com.modmaker;

import java.io.File;

public interface ModElement {
    String getName();
    File getTextureFile();
    boolean isBlock();
    boolean isItem();
    boolean isEntity();
    String getDisplayString();
}
