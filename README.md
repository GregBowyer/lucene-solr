SIMD accelerated VByte decoding experiment
==========================================

The following is a lucene hackjob that uses [MaskedVbytes](http://maskedvbyte.org/) instead of `FORUtils` to perform vint encode and decode.
It is likely to _never_ make it into core lucene proper as it is very targeted at x86 CPUs, although maybe it can be used as a springboard for other ideas.

Performance
-----------

Current benchmark results on `wikimedium10m`
`B` is baseline and `A` is the candidate

```
                    Task       QPS B      StdDev       QPS A      StdDev                Pct diff
              AndHighLow      707.80     (12.8%)      309.54      (3.6%)  -56.3% ( -64% -  -45%)
            OrNotHighLow     1033.00     (12.8%)      471.47      (4.0%)  -54.4% ( -63% -  -43%)
                 Respell       56.65     (13.3%)       42.24      (6.8%)  -25.4% ( -40% -   -6%)
                  Fuzzy2       26.57     (10.9%)       21.38      (8.5%)  -19.5% ( -35% -    0%)
                  Fuzzy1       42.47     (13.1%)       34.57     (10.1%)  -18.6% ( -36% -    5%)
                 Prefix3      177.03     (11.8%)      156.68      (6.9%)  -11.5% ( -27% -    8%)
                 LowTerm      526.38     (11.3%)      491.10      (7.4%)   -6.7% ( -22% -   13%)
              AndHighMed      280.10     (13.0%)      262.86      (6.5%)   -6.2% ( -22% -   15%)
                Wildcard       51.04     (14.7%)       50.22      (9.5%)   -1.6% ( -22% -   26%)
               MedPhrase      142.94     (14.9%)      145.38      (9.4%)    1.7% ( -19% -   30%)
                 MedTerm      206.65     (13.8%)      216.49      (9.2%)    4.8% ( -16% -   32%)
                  IntNRQ       10.18     (18.0%)       10.77     (13.9%)    5.8% ( -22% -   46%)
                PKLookup      232.69     (13.9%)      249.13     (10.2%)    7.1% ( -14% -   36%)
               OrHighMed       45.07     (14.2%)       48.44      (7.4%)    7.5% ( -12% -   33%)
            OrHighNotLow      105.18     (14.3%)      113.37     (11.8%)    7.8% ( -16% -   39%)
         LowSloppyPhrase       65.96     (13.8%)       71.10      (9.1%)    7.8% ( -13% -   35%)
              OrHighHigh       25.90     (13.7%)       28.14      (8.0%)    8.7% ( -11% -   35%)
               OrHighLow       90.08     (14.9%)       97.93      (9.5%)    8.7% ( -13% -   39%)
         MedSloppyPhrase       31.23     (14.4%)       34.06      (9.6%)    9.0% ( -13% -   38%)
           OrNotHighHigh       57.09     (12.0%)       62.66      (8.7%)    9.7% (  -9% -   34%)
                HighTerm       75.64     (16.5%)       83.42     (11.1%)   10.3% ( -14% -   45%)
             MedSpanNear       53.78     (14.2%)       59.56      (8.8%)   10.7% ( -10% -   39%)
              HighPhrase        8.88     (13.6%)        9.86      (9.1%)   11.0% ( -10% -   38%)
        HighSloppyPhrase       22.21     (15.6%)       25.07     (11.0%)   12.8% ( -11% -   46%)
            HighSpanNear       14.21     (14.4%)       16.14     (10.4%)   13.6% (  -9% -   44%)
             LowSpanNear       45.58     (15.6%)       52.33      (9.7%)   14.8% (  -9% -   47%)
           OrHighNotHigh       38.24     (13.1%)       43.95     (10.4%)   15.0% (  -7% -   44%)
               LowPhrase       28.13     (12.8%)       32.72     (10.0%)   16.3% (  -5% -   44%)
            OrHighNotMed       47.59     (15.6%)       55.93     (12.9%)   17.5% (  -9% -   54%)
            OrNotHighMed       61.58     (13.9%)       77.47      (9.9%)   25.8% (   1% -   57%)
             AndHighHigh       33.44     (11.4%)       44.37      (9.4%)   32.7% (  10% -   60%)
```

Index size difference
---------------------

```
du -hc projects/lucene-benchmark/indices/

3.6G    projects/lucene-benchmark/indices/wikimedium10m.lucene_candidate.IntrinsicLucene50.Memory.nd10M
3.3G    projects/lucene-benchmark/indices/wikimedium10m.lucene_baseline.Lucene50.Memory.nd10M
```


Todos
-----
* Rewrite to be cleaner and more modular against lucene codecs
* Look into if small int lists should not be encoded with maskedvbyte, but handled in naive java
* Figure out why some queries are slower
