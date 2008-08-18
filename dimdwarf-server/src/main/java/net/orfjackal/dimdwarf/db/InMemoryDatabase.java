/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.db;

import net.orfjackal.dimdwarf.tx.Transaction;
import net.orfjackal.dimdwarf.tx.TransactionParticipant;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class InMemoryDatabase {

    private final Map<Blob, SortedMap<Integer, Blob>> values = new ConcurrentHashMap<Blob, SortedMap<Integer, Blob>>();
    private volatile int latestRevision = 1;

    public Database openConnection(Transaction tx) {
        TransactionalDatabase db = new TransactionalDatabase(latestRevision);
        tx.join(db);
        return db;
    }

    private Blob readCommitted(Blob key, int revision) {
        SortedMap<Integer, Blob> revs = allRevisions(key);
        return specificRevision(revision, revs);
    }

    private void commitModifications(Map<Blob, Blob> updates) {
        latestRevision = latestRevision + 1;
        for (Map.Entry<Blob, Blob> entry : updates.entrySet()) {
            writeRevision(entry.getKey(), entry.getValue(), latestRevision);
        }
    }

    private void writeRevision(Blob key, Blob value, int revision) {
        allRevisions(key).put(revision, value);
    }

    private SortedMap<Integer, Blob> allRevisions(Blob key) {
        SortedMap<Integer, Blob> revs = values.get(key);
        if (revs == null) {
            revs = new TreeMap<Integer, Blob>();
            values.put(key, revs);
        }
        return revs;
    }

    private static Blob specificRevision(int revision, SortedMap<Integer, Blob> revs) {
        Blob value = null;
        for (Map.Entry<Integer, Blob> e : revs.entrySet()) {
            if (e.getKey() <= revision) {
                value = e.getValue();
            }
        }
        return value;
    }

    private class TransactionalDatabase implements Database, TransactionParticipant {

        private final Map<Blob, Blob> updates = new ConcurrentHashMap<Blob, Blob>();
        private final int visibleRevision;
        private Transaction tx;

        public TransactionalDatabase(int visibleRevision) {
            this.visibleRevision = visibleRevision;
        }

        public void joinedTransaction(Transaction tx) {
            if (this.tx != null) {
                throw new IllegalStateException();
            }
            this.tx = tx;
        }

        public void prepare(Transaction tx) throws Throwable {
        }

        public void commit(Transaction tx) {
            commitModifications(updates);
        }

        public void rollback(Transaction tx) {
        }

        public Blob read(Blob key) {
            Blob blob = updates.get(key);
            if (blob == null) {
                blob = readCommitted(key, visibleRevision);
            }
            if (blob == null) {
                blob = Blob.EMPTY_BLOB;
            }
            return blob;
        }

        public void update(Blob key, Blob value) {
            updates.put(key, value);
        }

        public void delete(Blob key) {
            updates.remove(key);
        }
    }
}
