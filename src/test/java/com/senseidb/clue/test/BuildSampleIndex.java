package com.senseidb.clue.test;

import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BuildSampleIndex {

    private static void addMetaString(Document doc, String field, String value) {
        if (value != null) {
            doc.add(new SortedDocValuesField(field, new BytesRef(value)));
            doc.add(new StringField(field + "_indexed", value, Store.YES));
        }
    }

    private static Document buildDoc(JSONObject json) {
        Document doc = new Document();

        doc.add(new NumericDocValuesField("id", json.getLong("id")));
        doc.add(new DoubleDocValuesField("price", json.optDouble("price")));
        doc.add(new TextField("contents", json.optString("contents"), Store.NO));
        doc.add(new NumericDocValuesField("year", json.optInt("year")));
        doc.add(new NumericDocValuesField("mileage", json.optInt("mileage")));

        addMetaString(doc, "color", json.optString("color"));
        addMetaString(doc, "category", json.optString("category"));
        addMetaString(doc, "makemodel", json.optString("makemodel"));
        addMetaString(doc, "city", json.optString("city"));

        String tagsString = json.optString("tags");
        if (tagsString != null) {
            String[] parts = tagsString.split(",");
            if (parts.length > 0) {
                for (String part : parts) {
                    doc.add(new SortedSetDocValuesField("tags", new BytesRef(part)));
                    doc.add(new StringField("tags_indexed", part, Store.NO));
                }
            }

            // store everything
            FieldType ft = new FieldType();
            ft.setOmitNorms(false);
            ft.setTokenized(true);
            ft.setStoreTermVectors(true);
            ft.setStoreTermVectorOffsets(true);
            ft.setStoreTermVectorPayloads(true);
            ft.setStoreTermVectorPositions(true);
            ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

            PayloadTokenizer tokenStream;
            try {
                tokenStream = new PayloadTokenizer(tagsString);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            Field tagPayload = new Field("tags_payload", tokenStream, ft);
            doc.add(tagPayload);
        }

        doc.add(new BinaryDocValuesField("json", new BytesRef(json.toString())));

        return doc;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("usage: source_file index_dir");
        }

        IndexWriterConfig idxWriterConfig = new IndexWriterConfig();
        Directory dir = FSDirectory.open(Paths.get(args[1]));
        try (IndexWriter writer = new IndexWriter(dir, idxWriterConfig)) {
            List<String> documentLines = Files.readAllLines(Paths.get(args[0]));
            documentLines
                    .stream()
                    .map(JSONObject::new)
                    .map(BuildSampleIndex::buildDoc)
                    .forEach(document -> {
                        try {
                            writer.addDocument(document);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

            System.out.println(documentLines.size() + " docs indexed");

        }
    }

}
