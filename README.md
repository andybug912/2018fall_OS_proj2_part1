# 2018fall_OS_proj2_part1

COMPILATION:
javac *.java

HOW TO RUN:

1. Decide ips & ports
    1) Decide server ip & port
    2) Decide Helper ips & ports, put them in server_list.txt. Follow the original format.
    3) Decide the three Reducers' ips & ports, put them in reducer_list.txt. Follow the original format and only replace ips & ports.

2. Turn on server
java TinyGoogleServer.java [serverPort]

3. Turn on Client
java TinyGoogleClient.java [serverIP] [serverPort]

4. Turn on Helpers. HelperIndex starts from 0. 
java Helper.java [helperIndex] [OPTIONAL: maxNumOfMapperThreads] [OPTIONAL: maxNumOfQueryThreads]

5. Turn on Reducers. RelperIndex starts from 0.
java Reducer.java [reducerIndex]

6. Go to client and start to index or query.
