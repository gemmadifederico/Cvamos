# C-vAMoS
## C-vAMoS: Event Abstraction via Motif Search Considering the Context

C-vAMos is a trace-based event abstraction technique, able to deal with the context during the identifications of the patterns.

## Features

- Recognize a set of motifs starting from a low-level event log, where events refer to sensor measurements (Step 1)
- Distinguish motifs based on contextual attributes. Contextual information is obtained using sensors that capture the state of the environment while the activity in the pattern is being performed. This information is already included in the event log, usually in the form of continuous sensors values (e.g. the temperature sensor)
- Abstract a low-level event log by filtering the set of identified motifs, replacing them by a label (Step 2)

## How it works

C-vAMoS is a [Java](https://www.oracle.com/) application and is composed by two main classes, i.e. the two steps of the approach:
- MotifsIdentifier.java for the identification of the motifs
- MotifsFilter.java for the filtering in the event log

### Identification Phase
To run the identification execute:

```java
java -jar MotifsIdentifier.jar input.xes output.xes costs-map.json attrib-map.json motifLength distance quorum
```
- input.xes is the path of the low-level event log
- output.xes is the path where the XES file containing the identified motifs is saved
- cost-map.json is the path of the cost map used by the algorithm
- attrib-map.json is the path of the attribute map used by the algorithm
- motifLength is the length of the motifs to be identified
- distance is the max distance error allowed when checking the similarity between a motif and a sub-trace
- quorum is the minimum percentage of traces in which the motif must appear

For example:
```java
java -jar MotifsIdentifier.jar rawLog.xes identifiedMotifs.xes costs-map.json attrib-map.json 10 2 0.7
```
Identifies motifs of length 10, with a max distance of 2, that have to appear in at least the 70% of the traces, by considering also the cost map cost-map and the attribute map attrib-map.

### Filtering Phase
To run the filtering execute:

```java
java -jar MotifsFilter.jar input.xes motifs.xes output.xes costs-map.json attrib-map.json distance label
```
- input.xes is the path of the low-level event log
- motifs.xes is the path of the file XES containing the identified motifs
- output.xes is the path where the abstracted XES file is saved
- cost-map.json is the path of the cost map used by the algorithm
- attrib-map.json is the path of the attribute map used by the algorithm
- distance is the max distance error allowed when checking the similarity between a motif and a sub-trace
- label is the label given to the motifs during the replacement

For example:
```java
java -jar MotifsFilter.jar rawLog.xes identifiedMotifs.xes abstractedLog.xes costs-map.json attrib-map.json 2 "Activity_1"
```
Filters the list of motifs contained in the file identifiedMotifs.xes on the log rawLog.xes. The motifs are checked up to a distance value of 2, and the labeled assigned during the replacement is "Activity_1". The abstracted log is saved in the file abstractedLog.xes.

### The cost map
The cost map is a [Json](https://www.json.org/) file and defines the interchange cost between activities.
The file is structured as follows:
```json 
    [
        {"act1": act1
	    "act2": : act2
	    "cost": cost}, ...
    ]
```
- act1, act2 are activity identifiers
- cost is the cost value between them

### The attribute map
The attribute map is a [Json](https://www.json.org/) file and defines list of identifiers to be considerd as attributes, the tolerance value used in the verification, and the aggregation operator used to aggregate the attributes for each motif.

The file is structured as follows:
```json 
    [
        {"name": actID,
        "tolerance": value,
        "operation" : operator}, ...
    ]
```
- actID is the activity identifier
- tolerance is a scalar value
- operator can be a value between {MEAN, MIN, MAX, EQUALS}
