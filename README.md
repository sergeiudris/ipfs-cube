# ipfs-torrent-search
a decentralized app to search torrents

## goal

- distributed automated index: every node crawles a part of torrent network, forming a part of the index
- query the whole index: nodes exchange index parts so that every node has almost the whoile index to query
- index is just data in db: extracts torrent info and shows seed/leech
- live additions/removals over pubsub: when user adds a torrent to a torrent client, app sends msg over pubsub - so all nodes online instantly add torrent to index (or increment/decrement seed/leech)
- just a program: simple, entirely free, open source, no barriers ratio-limits ads rewards economies etc. - just a program that does its thing, like transmission on linux