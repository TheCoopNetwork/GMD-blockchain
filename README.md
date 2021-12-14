----
# Welcome to GMD! #


----
## What is GMD? ##
Geoma DAO (GMD) is a cryptocurrency to make the world a better place.


----
## How to use it? ##
One of the following options must be chosen in order to use it (more details about each option on next paragraph):
1. Download the code from this repository, compile it on Linux or MacOS and run it on Windows, Linux or MacOS.
2. Download the compiled node and run it on your Windows, Linux or MacOS. 
3. Use one of the trusted nodes hosted by TheCoopNetwork.io (no installation needed for this option)



As zero trust is required to run this blockchain, we recommend using option 1 whenever possible.


----
### Option 1: Get code, compile and run it ###
  - *dependencies*:
		- to compile you need Java 11 or newer JDK on Linux or MacOS.  
			- *Ubuntu*/*Debian* - sudo apt install default-jdk
  - ./compile.sh --skip-desktop
  - ./run.sh
  - Open in your browser http://localhost:6876/

### Option 2: Download built node  ###

#### For Windows and MacOS - use Desktop app ####
  - Download desktop app installer, install it and run it.  
  - Instructions and download link: 
    - [Instructions and download link for Windows](https://github.com/CoopNetwork/GMD-node-app#instructions-for-windows-x64)
    - [Instructions and download link for MacOS](https://github.com/CoopNetwork/GMD-node-app#instructions-for-macos)
  - Source code and instructions here: [GMD node app](https://github.com/CoopNetwork/GMD-node-app)  

#### For Linux ####
  - *dependencies*:
  - to run you need Java 11 or newer JRE (java must be in PATH. You can check with command `java --version`)
  - Download zip file:  
      - https://node.thecoopnetwork.io:8443/gmd-node-standalone.zip (IPv6 and IPv4)  
      Or from other mirrors:  
      - https://node10.thecoopnetwork.io/gmd-node-standalone.zip (IPv6 and IPv4)
      - https://node8.thecoopnetwork.io/gmd-node-standalone.zip (IPv4 only)
      - https://node6.thecoopnetwork.io/gmd-node-standalone.zip (IPv4 only)
      - https://node12.thecoopnetwork.io/gmd-node-standalone.zip (IPv4 only)
      - https://node13.thecoopnetwork.io/gmd-node-standalone.zip (IPv6 only)
      - https://node16.thecoopnetwork.io/gmd-node-standalone.zip (IPv6 only)
  - Extract contains and run start.sh.
  - Open in your browser http://localhost:6876/
  - [Optional] Version can be checked http://localhost:6876/version
	
### Option 3: use trusted node  ###
   - open in your browser one of the nodes
 - IPv6 and IPv4:
   - https://node10.thecoopnetwork.io
 - IPv4 Only:
   - https://node1.thecoopnetwork.io 
   - https://node2.thecoopnetwork.io 
   - https://node3.thecoopnetwork.io 
   - https://node4.thecoopnetwork.io 
   - https://node5.thecoopnetwork.io 
   - https://node6.thecoopnetwork.io
   - https://node7.thecoopnetwork.io
   - https://node8.thecoopnetwork.io
   - https://node11.thecoopnetwork.io
   - https://node12.thecoopnetwork.io
- IPv6 only:
   - https://node9.thecoopnetwork.io
   - https://node13.thecoopnetwork.io
   - https://node14.thecoopnetwork.io
   - https://node15.thecoopnetwork.io
   - https://node16.thecoopnetwork.io
   - https://node17.thecoopnetwork.io
   
   
Please make sure you never use your private passphrase on any site unless you trust the node (e.g. is running on your own machine).
In case you use remote (over the internet node) please make sure https is enabled and certificate is valid.


Nodes are tested and run well over both IPv4 and IPv6 networks.


## Testnet ##
Testnet is available on the following links. Do not use real wallet on testnet - create new one.
- https://node1.thecoopnetwork.io:6877
- https://node2.thecoopnetwork.io:6877
- https://node6.thecoopnetwork.io:6877
- https://node8.thecoopnetwork.io:6877
- https://node10.thecoopnetwork.io:6877
- https://node12.thecoopnetwork.io:6877

# Important!!! #
- Never send your secret phrase to an unknown server. Always use your own node or if not possible a node that is provided by coopnetwork.io (i.e. url is in the format https://<nodename>.thecoopnetwork.io and browser shows "secure connection" icon in the address bar)  
- If you plan to make your node reachable over internet, make sure that you expose an "https" connection, not "http".
	- In order to make it secure you need to edit the file conf/nxt.properties similar to this example:
```
nxt.apiSSL=true
nxt.apiServerSSLPort=7876
nxt.apiServerPort=7876
nxt.peerServerPort=6874
nxt.keyStoreType=PKCS12
nxt.keyStorePath=/some/path/keystore.p12
nxt.keyStorePassword=somepassword12345!
```
 The `keystore.p12` should be the keystore containing the server private key and public certificate. Keystore should be protected with a password defined in `nxt.keyStorePassword`



## License
* This program is distributed under the terms of the Jelurida Public License version 1.1 for the Ardor Public Blockchain Platform.
* This source code has been generated by CoinGenerator - https://coingenerator.sh
