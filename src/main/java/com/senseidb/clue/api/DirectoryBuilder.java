package com.senseidb.clue.api;

import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.net.URI;

public interface DirectoryBuilder {
    Directory build(URI idxUri) throws IOException;
}
