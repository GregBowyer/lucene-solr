package org.apache.lucene.index;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.lucene.store.Directory;

/**
 * Created by greg on 3/30/16.
 */
public class NUMAAwareIndexWriter extends IndexWriter {

  /**
   * Constructs a new IndexWriter per the settings given in <code>conf</code>.
   * If you want to make "live" changes to this writer instance, use
   * {@link #getConfig()}.
   * <p>
   * <p>
   * <b>NOTE:</b> after ths writer is created, the given configuration instance
   * cannot be passed to another writer. If you intend to do so, you should
   * {@link IndexWriterConfig#clone() clone} it beforehand.
   *
   * @param d    the index directory. The index is either created or appended
   *             according <code>conf.getOpenMode()</code>.
   * @param conf the configuration settings according to which IndexWriter should
   *             be initialized.
   * @throws IOException if the directory cannot be read/written to, or if it does not
   *                     exist and <code>conf.getOpenMode()</code> is
   *                     <code>OpenMode.APPEND</code> or if there is any other low-level
   *                     IO error
   */
  public NUMAAwareIndexWriter(Directory dir, IndexWriterConfig conf) throws IOException {
    super(dir, conf);
  }
}
