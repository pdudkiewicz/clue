package com.senseidb.clue.api;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

public class DefaultDirectoryBuilder implements DirectoryBuilder {

    @Override
    public Directory build(URI idxUri) throws IOException {
        return FSDirectory.open(Paths.get(idxUri.getPath()));
    }

}
