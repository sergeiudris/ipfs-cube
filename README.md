# ipfs-find
decentralized peer-to-peer app to search and download IPFS files

## goal

- like torrent client, but for ipfs, with search
- global decentralized peer-to-peer: volunteer peer-to-peer self-forming cluster of server nodes for computation, user laptops just connect - 100 nodes, 10000 users
- distributed automated index: every node crawles a part of ipfs network, forming a part of the index
- query the whole index: nodes exchange index parts so that every node has almost the whoile index to query
- index is just data in db: extracts file info and shows seeds (pins)
- live additions/removals over pubsub: when user adds a file to ipfs node, app sends msg over pubsub - so all nodes online instantly add file to index (or increment/decrement seeds/pins) - if file is popular (defined by how many seeds)
- filters: peers can choose which filters to apply to the index they store
- installation: download uberjar from github releases + docker
- just a program: simple, entirely free, open source, no barriers ratio-limits ads rewards economies etc. - just a program that does its thing, like transmission on linux