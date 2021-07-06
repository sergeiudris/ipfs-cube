# find
torrent and IPFS client, with search

## program

- decentralized peer-to-peer program to search and download IPFS/torrent files
- <s>global decentralized peer-to-peer - program is entirely on users' (peers') machines: computers that have app currently running form the global app</s>
- <s>distributed</s> automated index - program crawles ipfs/torrent network, forming the index of files
- query the whole index - programs exchange indexes <s>parts so that</s> every program has the whole index to query
- index is just data in db - extracts file info and shows seeds (pins)
- <s>live additions/removals over pubsub: when user adds a file to ipfs node, app sends msg over pubsub - so all nodes online instantly add file to index (or increment/decrement seeds/pins) - if file is popular (defined by how many seeds)</s>
- DHT - program uses existing DHT, not pubsub, to find other programs to share index (additional field to DHT's ping to discover/ask and ut_metadata-like extension to then download index)
- filters - peers can choose which filters to apply to the index they store
- can download files - it's a torrent client and IPFS client
- installation - desktop program
- binaries - distributed separately from source code repo, readme has "build from source", build is done with Makefile, like linux, tor browser
- runtime - program runs on JVM, will not be GraalVM compiled
- interface - <s>cljfx</s> will use existing programs as base
- database - datahike
- build - should happen without docker, on OS, all binaries needed for build should be downloaded into aa-bin
- repository - should be at github.com/ipfs-shipyard/find
- docs - no : only program and readme with two sections - goal and build from source
- tests - no
- just a program - simple, entirely free, open source, no barriers ratio-limits ads rewards economies etc. - just a program that does its thing, like transmission on linux, but with search