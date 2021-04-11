# ipfs-index-search
global decentralized app to create and search indexes (for example, torrents)

## goal

- add and serach indexes: peers can create/update/fork/search indexes (for example, index of torrent files, files themselves are stored on torrent network)
- choose indexes: peer chooses which other indexes to download and store on their machine
- index is data in db: index is just data in an existing high level database that can be queried
- app is global: there is a global index of users and index of their indexes that is stored on every peer machine in the database
- never stale: db is continuously synced via pubsub (for example, torrent index will be continuously updated as users add/remove entries)
- opt-in filters per dataset: peer can choose to allow everyone to write to index, but with filters applied to not store trash
- search is a query: search is simply db query, with all its power
- decentralized identity: global index of users stores their DIDs, except for private key owned by peer
- like repos on github: indexes can be forked
- just a program: simple, free, open source, no barriers ratio-limits ads rewards economies etc. - just a program that does its thing, like transmission on linux