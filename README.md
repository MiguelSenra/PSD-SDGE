# PSD/SDGE Project

## Authors

- Dinis Gonçalves Estrada - pg53770 - [@DinisEstrada](https://github.com/DinisEstrada)
- Emanuel Lopes Monteiro da Silva - pg53802 - [@EmanuelsGiT](https://github.com/EmanuelsGiT)
- Miguel Ângelo Silva Senra - pg54093 - [@MiguelSenra](https://github.com/MiguelSenra)
- Simão Pedro Cunha Matos - pg54239 - [@simaom21](https://github.com/simaom21)

## Summary

A distributed album storage system with an Erlang backend and Java clients.

- `main_server.erl`: central Erlang TCP server
- `login_manager.erl`: user auth and session management
- `albuns.erl`: album creation, access, and editing control
- `server_data.erl`: data server registry and hash zones
- `Client/`: Java CLI client
- `Data_Server/`: Java gRPC data server

## Requirements

- Erlang
- Java 21
- Maven

## Run

1. Start Erlang backend:

```sh
cd /home/miguel/PSD-SDGE
erl
```

In Erlang shell:

```erlang
c(main_server).
c(login_manager).
c(albuns).
c(server_data).
main_server:start(12345).
```

2. Start data server:

```sh
cd /home/miguel/PSD-SDGE/Data_Server
mvn compile
mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=org.example.DataServer1
```

3. Start client:

```sh
cd /home/miguel/PSD-SDGE/Client
mvn compile
mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=org.example.Main
```

## Notes

- Client talks to Erlang for auth and album control.
- Data servers join the network and handle file transfer.
