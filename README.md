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
- binaries - repo has no binary releases, only source code and "build from source" in readme
- runtime - program runs on JVM, will not be GraalVM compiled, it's not on Jesus level
- interface - cljfx
- system tray - no, program runs in foreground
- database - datahike
- build - should happen without docker, on OS, JVM and needed binaries if missing will be downloaded into repo dir for use by build, build is done with one command
- no plugins, no extensions - program is exactly torrent and IPFS client, with search
- repository - should be at github.com/ipfs-shipyard/find
- issues - program repository has no issues or discussions, only code
- attribution headers in files - no, only code
- programmer names in readme - no, repo is code only
- license files - no, repo is only code
- authors - no, repo is only code
- authoring orgs, org links - no, repo is code only
- docs - no : only program and readme with two sections - goal and build from source
- tests - no
- just a program - simple, entirely free, open source, no barriers ratio-limits ads donations rewards economies links - just a program that does its thing, like transmission on linux, but with search