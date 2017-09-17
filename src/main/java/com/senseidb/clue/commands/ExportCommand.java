package com.senseidb.clue.commands;

import com.senseidb.clue.ClueContext;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.PrintStream;
import java.nio.file.Paths;

public class ExportCommand extends ClueCommand {

    public ExportCommand(ClueContext ctx) {
        super(ctx);
    }

    @Override
    public String getName() {
        return "export";
    }

    @Override
    public String help() {
        return "export index to readable text files";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws Exception {
        if (args.length < 1) {
            out.println("usage: export output bin/text (default text)");
            return;
        }

        boolean isExportToText;

        try {
            isExportToText = !"bin".equals(args[1]);
        } catch (Exception e) {
            isExportToText = true;
        }

        if (isExportToText) {
            System.out.println("exporting index to text");
        } else {
            System.out.println("exporting index to binary");
        }

        FSDirectory fsExportDirectory = FSDirectory.open(Paths.get(args[0]));

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig();
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        if (isExportToText) {
            indexWriterConfig.setCodec(new SimpleTextCodec());
        }

        try (IndexWriter writer = new IndexWriter(fsExportDirectory, indexWriterConfig)) {

            //alternative way to export index, much slower, but works with SimpleTextCodec
//            IndexReader indexReader = ctx.getIndexReader();
//            CodecReader leaves[] = new CodecReader[indexReader.leaves().size()];
//            int i = 0;
//            for (LeafReaderContext leaf : indexReader.leaves()) {
//                leaves[i++] = SlowCodecReaderWrapper.wrap(leaf.reader());
//            }
//            writer.addIndexes(leaves);

            // workaround, without adding new document SimpleTextCodec is not used.
            // bug in lucene?
            writer.addDocument(new Document());

            writer.addIndexes(ctx.getDirectory());
            writer.forceMerge(1);
        }
    }

}
