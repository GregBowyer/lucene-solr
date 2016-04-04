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

import org.apache.lucene.codecs.lucene50.Lucene50PostingsFormat;
import org.apache.lucene.index.CorruptIndexException;
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
  private static final byte ALL_VALUES_EQUAL = 0;
  private static final byte UNSORTED_POSTINGS = 1;
  private static final byte DELTA_POSTINGS = 2;
  private static final byte SMALL_BLOCK = 3;

  // The number of values beyond which packing a masked vbyte block becomes expensive
  private static final int JNI_OVERHEAD = 8;

  // TODO - These numbers are totally made up!
  public static final int BLOCK_SIZE = Lucene50PostingsFormat.BLOCK_SIZE;
  public static final int MAX_DATA_SIZE = BLOCK_SIZE * 4;
  public static final int MAX_ENCODED_SIZE = MAX_DATA_SIZE * 8;

  public static native void vbyteDecode(byte[] bytes, int[] out, int length) throws IOException;
  public static native void vbyteDecodeDelta(byte[] bytes, int[] out, int length, int delta) throws IOException;
  public static native int vbyteEncode(int[] postings, int valueCount, byte[] buffer) throws IOException;
  public static native int vbyteEncodeDelta(int[] postings, int valueCount, int deltaOffset, byte[] buffer) throws IOException;

  public static void readBlock(IndexInput docIn, byte[] encoded, int[] docDeltaBuffer) throws IOException {
    byte postingsType = docIn.readByte();
    int valueOrBytes = docIn.readVInt();
    switch (postingsType) {
      case ALL_VALUES_EQUAL:
        Arrays.fill(docDeltaBuffer, 0, docDeltaBuffer.length, valueOrBytes);
        return;
      case UNSORTED_POSTINGS:
        docIn.readBytes(encoded, 0, valueOrBytes);
        vbyteDecode(encoded, docDeltaBuffer, valueOrBytes);
        return;
      case DELTA_POSTINGS:
        docIn.readBytes(encoded, 0, valueOrBytes);
        int delta = docIn.readVInt();
        vbyteDecodeDelta(encoded, docDeltaBuffer, valueOrBytes, delta);
        return;
      case SMALL_BLOCK:
        byte values = docIn.readByte();
        for (int i = 0; i < values; i++) {
          docDeltaBuffer[i] = docIn.readVInt();
        }
      default:
        throw new CorruptIndexException("Unknown intrinsic block format", docIn);
    }
  }

  public static void skipBlock(IndexInput in) throws IOException {
    int numBytes = in.readVInt();
    if (numBytes == ALL_VALUES_EQUAL) {
      in.readVInt();
      return;
    }
    in.seek(in.getFilePointer() + numBytes);
  }

  // There is no SIMD vectorisation on the write path, as such we dont need to slow indexing
  // by performing the write via JNI, as such this is a reimplementation of the code that is in
  // the original MaskedVByte repo in pure java
  public static void writeDeltaBlock(int[] values, int valueCount, byte[] encoded, int deltaOffset, IndexOutput out) throws IOException {
    if (isAllEqual(values)) {
      out.writeByte(ALL_VALUES_EQUAL);
      out.writeVInt(values[0]);
      return;
    }

    if (isSmallBlock(values)) {
      out.writeByte(SMALL_BLOCK);
      out.writeByte((byte) values.length);
      for (int value : values) {
        out.writeVInt(value);
      }
    }

    int numBytes = -1;
    if (deltaOffset >= 0) {
      numBytes = vbyteEncodeDelta(values, valueCount, deltaOffset, encoded);
      out.writeByte(DELTA_POSTINGS);
    } else {
      numBytes = vbyteEncode(values, valueCount, encoded);
      out.writeByte(UNSORTED_POSTINGS);
    }

    assert numBytes > 0;
    out.writeVInt(numBytes);

    if (deltaOffset >= 0) {
      out.writeVInt(deltaOffset);
    }

    out.writeBytes(encoded, numBytes);
  }

  private static boolean isSmallBlock(int[] values) {
    return values.length <= JNI_OVERHEAD;
  }

  public static void writeBlock(int[] values, int valueCount, byte[] encoded, IndexOutput out) throws IOException {
    writeDeltaBlock(values, valueCount, encoded, -1, out);
  }

  private static boolean isAllEqual(final int[] data) {
    final int v = data[0];
    for (int i = 1; i < data.length; ++i) {
      if (data[i] != v) {
        return false;
      }
    }
    return true;
  }
}
