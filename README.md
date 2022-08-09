# Blockchain-test-project
Blockchain test project

## Description
This program imitates single blockchain with miners and clients that do some transactions,
each miner gets 100 VC(virtual coin) for each block, first block must be empty,
the number of zeros at the beginning of the hash of each block is automatically adjusted
depending on how fast the previous block was generated.
The number of blocks is fixed, so the program will run until it generates them all,
at the end it prints all the blocks and the ledger.

An example of a 7-block blockchain:

![7-block generation](https://user-images.githubusercontent.com/71446610/183585466-bdbf11f5-c4e2-4146-afac-2f938dd59478.gif)


## Instructions
In config (./src/blockchain/config/BlockchainConfig.class) you can configure blockchain length,
block reward, serialize path, block max/min creation milliseconds depending on which 
the number of zeros at the beginning of the hash of each block is automatically adjusted
