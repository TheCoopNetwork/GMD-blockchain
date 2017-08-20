----
# Nxt Blockchain Creation Kit #

This package is intended to allow easy creation of new blockchain projects
based on Nxt, satisfying the requirements of the Jelurida Public License
version 1.0 for the Nxt Public Blockchain Platform.

This is a starter kit for developers, not for end users. If you just install
it and run it, you will get a blockchain with no tokens, no accounts, and no
peers configured. To actually start a new blockchain using this kit, at least
the genesis block parameters and accounts must be defined in
conf/data/genesisParameters.json and conf/data/genesisAccounts.json.

Edit the genesisParameters.json file to define the timestamp of the genesis
block for your new blockchain (epochBeginning), and the public key of the
genesis account (genesisPublicKey).

The genesisAccounts.json file, and genesisAccounts-testnet.json for testnet,
should contain the list of accounts to be created in the genesis block of the
new blockchain, and their corresponding balances and public keys. The supplied
genesisAccounts.json file is empty. To generate such a file containing both
new user accounts, and the accounts of NXT holders from the Nxt public
blockchain, you must use the JPLSnapshot utility from the Nxt Reference
Software (NRS) v1.11.8 or later.

----
### Using the JPLSnapshot NRS add-on ###

Download and install the latest Nxt package from the Jelurida repository:

https://bitbucket.org/Jelurida/nxt/downloads

Enable the JPLSnapshot add-on in conf/nxt.properties by setting:

nxt.addOns=nxt.addons.JPLSnapshot

Make sure your node is configured as full node (not light client), and let it
download the full blockchain.

The add-on downloadJPLSnapshot API should be available under:

http://localhost:7876/test?requestTag=ADDONS


Below is the documentation for how to use this API:

----
The downloadJPLSnapshot API can be used to generate a genesis block JSON for a
clone to satisfy the JPL 10% sharedrop requirement to existing NXT holders.

This utility takes a snapshot of account balances and public keys on the Nxt
blockchain as of the specified height, scales down the balance of each account
proportionately so that the total of balances of sharedrop accounts is equal to
10% of the total of all balances, and merges this data with the supplied new
genesis accounts and balances.

Note that using a height more than 800 blocks in the past will normally require
a blockchain rescan, which takes a few hours to complete. Do not interrupt this
process.

Request parameters

    newGenesisAccounts - a JSON formatted file containing all new account
    public keys and balances to be included in the clone genesis block
    
    height - the Nxt blockchain height at which to take the snapshot

Response

    A JSON formatted file, genesisAccounts.json, containing all public keys,
    new accounts and sharedrop accounts, and their initial balances, which
    should be placed in the conf/data directory of the clone blockchain.


Input file format

The input file should contain a map of account numbers to coin balances, and a
list of account public keys. Account numbers can be specified in either numeric
or RS format. Supplying the public key for each account is optional, but
recommended. Here is an example input file, which allocates 300M each to the
accounts with passwords "0", "1" and "2", for a total of 900M to new accounts,
resulting in 100M automatically allocated to existing NXT holders:

```
{
    "balances": {
         "NXT-NZKH-MZRE-2CTT-98NPZ": 30000000000000000,
         "NXT-X5JH-TJKJ-DVGC-5T2V8": 30000000000000000,
         "NXT-LTR8-GMHB-YG56-4NWSE": 30000000000000000
     },
     "publicKeys": [
         "bf0ced0472d8ba3df9e21808e98e61b34404aad737e2bae1778cebc698b40f37",
         "39dc2e813bb45ff063a376e316b10cd0addd7306555ca0dd2890194d37960152",
         "011889a0988ccbed7f488878c62c020587de23ebbbae9ba56dd67fd9f432f808"
     ]
 }
```

----

The generated genesisAccounts.json file should be placed in the conf/data
directory for the new blockchain package, replacing the existing empty file.

There are multiple other customizations that should be made for the newly
created Nxt clone, such as changing the default peer ports in
nxt/peer/Peer.java, defining default peers in nxt-default.properties, changing
the coin name and software name in nxt/Nxt.java, customizing the UI, etc.
Such customization work should be done by a competent developer, and is beyond
the scope of this document.

