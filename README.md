# Java UUID v1 Generator
This library provides an implementation strategy for version 1 UUID generator.
This implementation considers a 'node' to be an instance within a Java process.
The node is generator by using environment variables, hashing them, and use the first 6 characters, as suggested by the UUID RFC.
 
