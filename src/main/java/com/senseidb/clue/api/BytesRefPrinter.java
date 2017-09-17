package com.senseidb.clue.api;

import org.apache.lucene.util.BytesRef;

public interface BytesRefPrinter {
    BytesRefPrinter UTFPrinter = BytesRef::utf8ToString;
    BytesRefPrinter RawBytesPrinter = BytesRef::toString;

    String print(BytesRef bytesRef);
}
