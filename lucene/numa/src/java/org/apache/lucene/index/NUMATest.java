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

import xerial.jnuma.Numa;

/**
 * Created by greg on 3/30/16.
 */
public class NUMATest {

  public static void main(String[] args) {
    System.out.println("Has Numa: " + Numa.isAvailable());
    System.out.println("Num CPUS: " + Numa.numCPUs());
    System.out.println("Num nodes: " + Numa.numNodes());
    long[] distances = Numa.nodeToCpus(0);
    for (int i=0; i<distances.length; i++) {
      System.out.printf("Node(0) to CPU(%d) distance: (%d)%n", i, distances[i]);
    }
  }
}
