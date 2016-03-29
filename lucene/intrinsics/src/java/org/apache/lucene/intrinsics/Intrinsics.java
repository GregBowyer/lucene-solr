package org.apache.lucene.intrinsics;

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
import java.util.Arrays;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

/**
 * Created by greg on 3/9/16.
 */
public class Intrinsics {

  static {
    System.loadLibrary("Intrinsics");
  }

  /**
   * Special number of bits per value used whenever all values to encode are equal.
   */
  private static final int ALL_VALUES_EQUAL = 0;

  // TODO - These numbers are totally made up!
  public static final int MAX_DATA_SIZE = 4 * 1024;
  public static final int MAX_ENCODED_SIZE = MAX_DATA_SIZE * 32;

  public static native void vbyteDecode(byte[] bytes, int[] out, int length) throws IOException;
  public static native int vbyteEncode(int[] postings, int valueCount, byte[] buffer) throws IOException;

  public static void readBlock(IndexInput docIn, byte[] encoded, int[] docDeltaBuffer) throws IOException {
    int numBytes = docIn.readInt();
    if (numBytes == ALL_VALUES_EQUAL) {
      final int value = docIn.readVInt();
      Arrays.fill(docDeltaBuffer, 0, MAX_DATA_SIZE, value);
      return;
    }
    docIn.readBytes(encoded, 0, numBytes);
    vbyteDecode(encoded, docDeltaBuffer, numBytes);
  }

  public static void skipBlock(IndexInput in) throws IOException {
    int numBytes = in.readInt();
    if (numBytes == ALL_VALUES_EQUAL) {
      in.readVInt();
      return;
    }
    in.seek(in.getFilePointer() + numBytes);
  }

  // There is no SIMD vectorisation on the write path, as such we dont need to slow indexing
  // by performing the write via JNI, as such this is a reimplementation of the code that is in
  // the original MaskedVByte repo in pure java
  public static void writeBlock(int[] values, int valueCount, byte[] encoded, IndexOutput out) throws IOException {
    if (isAllEqual(values)) {
      out.writeInt(ALL_VALUES_EQUAL);
      out.writeVInt(values[0]);
      return;
    }

    int numBytes = vbyteEncode(values, valueCount, encoded);
    out.writeInt(numBytes);
    out.writeBytes(encoded, numBytes);
  }

  private static boolean isAllEqual(final int[] data) {
    final int v = data[0];
    for (int i = 1; i < MAX_DATA_SIZE; ++i) {
      if (data[i] != v) {
        return false;
      }
    }
    return true;
  }
}
