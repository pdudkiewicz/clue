package com.senseidb.clue.commands;

import com.senseidb.clue.ClueContext;
import com.senseidb.clue.util.MatchSomeDocsQuery;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;

import java.io.PrintStream;
import java.util.Random;

public class IndexTrimCommand extends ClueCommand {

    public IndexTrimCommand(ClueContext ctx) {
        super(ctx);
    }

    private static Query buildDeleteQuery(final int percentToDelete) {
        assert percentToDelete >= 0 && percentToDelete <= 100;
        final Random rand = new Random();
        return new MatchSomeDocsQuery() {

            @Override
            public String toString(String field) {
                return null;
            }

            @Override
            protected boolean match(int docId) {
                int guess = rand.nextInt(100);
                return guess < percentToDelete;
            }
        };

    }

    @Override
    public String getName() {
        return "trim";
    }

    @Override
    public String help() {
        return "trims the index, <TRIM PERCENTAGE>";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws Exception {
        if (args.length < 1) {
            out.println("usage: <TRIM PERCENTAGE>");
            return;
        }

        int trimPercent = Integer.parseInt(args[0]);

        if (trimPercent < 0 || trimPercent > 100) {
            throw new IllegalArgumentException("invalid percent: " + trimPercent);
        }

        IndexWriter writer = ctx.getIndexWriter();
        if (writer != null) {
            IndexReader reader = ctx.getIndexReader();

            writer.deleteDocuments(buildDeleteQuery(trimPercent));
            writer.commit();
            ctx.refreshReader();
            reader = ctx.getIndexReader();
            out.println("trim successful, index now contains: " + reader.numDocs() + " docs.");
        } else {
            out.println("unable to open writer, index is in readonly mode");
        }
    }

}
