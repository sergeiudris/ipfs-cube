# find
torrent and IPFS client, with search

## program

- decentralized peer-to-peer program to search and download IPFS/torrent files
- automated index - program crawles ipfs/torrent network, forming the index of files
- query the whole index - programs exchange indexes every program has the whole index to query
- index is just data in db - extracts file info and shows seeds (pins)
- automatic index echange - find programs discover each other and exchange the list of files
- DHT - program uses existing DHT, not pubsub, to find other programs to share index (additional field to DHT's ping to discover/ask and ut_metadata-like extension to then download index)
- extension - program is an extension of existing Bittorrent and IPFS networks
- filters - peers can choose which filters to apply to the index they store
- can download files - it's a torrent client and IPFS client
- installation - desktop program
- binaries - repo has no binary releases, only source code and "build from source" in readme, build is done with Makefile, like linux, tor browser
- runtime - program runs on JVM, will not be GraalVM compiled, it's not on Jesus level
- interface - cljfx
- database - datahike
- build - should happen without docker, on OS, all binaries needed for build should be downloaded into aa-bin
- repository - should be at github.com/ipfs-shipyard/find
- docs - no : only program and readme with two sections - goal and build from source
- tests - no
- just a program - simple, entirely free, open source, no barriers ratio-limits ads donations rewards economies - just a program that does its thing, like transmission on linux, but with search