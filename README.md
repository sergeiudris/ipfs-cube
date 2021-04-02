# ipfs-torrent-search
a desktop app to search torrents, torrent index stored locally and on ipfs

## goal

- should be an uberjar with browser interface
- distributed db that syncs with other peers in the background
- bottomline: like ipfs node, but with search
- user opens UI and can find torrents
- torrents are doownload from multiple existing sources (constatntly, by every node) and nodes also sync
- each node has a full db of torrents (shouldn't be too large), so when we search for a torrent the query is local (but db non-stop syncs with ohter nodes and torrent source)

it's a disgrace - all this torrent search ad-polluted sites and torrents are hard to find
should be easy
